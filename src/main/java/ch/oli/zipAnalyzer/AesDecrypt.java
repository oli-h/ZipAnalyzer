package ch.oli.zipAnalyzer;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.HexFormat;

public class AesDecrypt {

    public static void main(String[] args) throws Exception {
//        listAlgos();

        Path path = Path.of("crypt-aes.zip");
        String password = "GEHEIM";

        int compressedSize = 174613; // from local header
        int sizeSalt       = 16; // for AES256: 128 bits = 16 bytes
        int sizePwVerifier =  2;
        int sizeHmac       = 10;
        int sizeChiffre    = compressedSize - sizeSalt - sizePwVerifier - sizeHmac;

        InputStream is = Files.newInputStream(path);
        is.skip(52);
        byte[] salt               = is.readNBytes(sizeSalt      );
        byte[] pwVerifierInHeader = is.readNBytes(sizePwVerifier);
        byte[] data               = is.readNBytes(sizeChiffre   );
        byte[] hmacInHeader       = is.readNBytes(sizeHmac      );

        // see https://github.com/lclevy/unarcrypto/blob/master/ziparchive.py
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, (32 + 32 + 2) * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] derivedKeys = skf.generateSecret(spec).getEncoded();
        byte[] aesKey          = Arrays.copyOfRange(derivedKeys,  0, 32);
        byte[] macKey          = Arrays.copyOfRange(derivedKeys, 32, 64);
        byte[] pwVerifier      = Arrays.copyOfRange(derivedKeys, 64, 66);

        // check password: those 16 bits must be identical (i.e.: chance 1:65536 to have a wrong password and to decrypt to rubbish)
        System.out.println("PW-Verification in header    : " + HexFormat.of().formatHex(pwVerifierInHeader));
        System.out.println("PW-Verification from password: " + HexFormat.of().formatHex(pwVerifier));
        System.out.println("---------------------------------------------------------------");

        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec macSecretKey = new SecretKeySpec(macKey, "HmacSHA1");
        mac.init(macSecretKey);

        // ZIP uses a "Little"-Endian counter-mode (CTR)
        // Java's CounterMode is hard coded to "Big"-endian (see com.sun.crypto.provider.CounterMode#increment)
        // --> we can't use "AES/CTR/NoPadding" and we need to do our own CTR-Mode
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec secretKey = new SecretKeySpec(aesKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] counter          = new byte[16];
        byte[] encryptedCounter = new byte[16];
        for (int i = 0; i < data.length; ) {
            // 1. count the counter
            int n = 0;
            while ((n < counter.length) && (++counter[n] == 0)) {
                n++;
            }
            // 2. encrypt the counter
            cipher.doFinal(counter, 0, 16, encryptedCounter);
            // 3. XOR byte-by-byte
            for (int j = 0; j < 16 && i < data.length; j++, i++) {
                mac.update(data[i]);
                data[i] ^= encryptedCounter[j];
            }
        }
        System.out.println(new String(data));

        System.out.println("---------------------------------------------------------------");
        // check HMAC: first 10 bytes (80 bits) bits must be identical
        System.out.println("HMAC in header     : " + HexFormat.of().formatHex(hmacInHeader));
        System.out.println("HMAC encrypted data: " + HexFormat.of().formatHex(mac.doFinal()));
    }


    private static void listAlgos() {
        for (Provider provider: Security.getProviders()) {
            System.out.println(provider.getName());
            for (String key: provider.stringPropertyNames())
                System.out.println("\t" + key + "\t" + provider.getProperty(key));
        }
    }
}
