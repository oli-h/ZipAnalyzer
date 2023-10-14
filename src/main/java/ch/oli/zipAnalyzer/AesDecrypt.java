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
        byte[] salt            = is.readNBytes(sizeSalt      );
        byte[] pwVerification1 = is.readNBytes(sizePwVerifier);
        byte[] cipherText      = is.readNBytes(sizeChiffre   );
        byte[] hmacHeader      = is.readNBytes(sizeHmac      );

        // see https://github.com/lclevy/unarcrypto/blob/master/ziparchive.py
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, (32 + 32 + 2) * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] derivedKeys = skf.generateSecret(spec).getEncoded();
        byte[] aesKey          = Arrays.copyOfRange(derivedKeys,  0, 32);
        byte[] macKey          = Arrays.copyOfRange(derivedKeys, 32, 64);
        byte[] pwVerification2 = Arrays.copyOfRange(derivedKeys, 64, 66);

        // check password: those 16 bits must be identical
        System.out.println("PW-Verification from password: " + HexFormat.of().formatHex(pwVerification1));
        System.out.println("PW-Verification from file    : " + HexFormat.of().formatHex(pwVerification2));
        System.out.println("---------------------------------------------------------------");

        // ZIP uses "Big-Endian" Counter
        // --> use of "AES/CTR/NoPadding" impossible as appropriate Java-Crypto-Implementation is hard coded to a Little-Endian counter
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec secretKey = new SecretKeySpec(aesKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] counter = new byte[16];
        for (int block = 0; block <= cipherText.length / 16; block++) {
            for (int n = 0; (n < counter.length) && (++counter[n] == 0); n++) {}
            byte[] cipherCounter = cipher.doFinal(counter);

            for (int i = 0; i < 16 && i + block * 16 < cipherText.length; i++) {
                byte x = cipherText[block * 16 + i];
                byte y = cipherCounter[i];
                byte clearText = (byte) (x^y);
                System.out.print((char)clearText);
            }
        }

        System.out.println("---------------------------------------------------------------");
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec macSecretKey = new SecretKeySpec(macKey, "HmacSHA1");
        mac.init(macSecretKey);
        byte[] hmacCalculated = mac.doFinal(cipherText);
        // check password: first 10 bytes (80 bits) bits must be identical
        System.out.println("HMAC from header   : "+HexFormat.of().formatHex(hmacHeader));
        System.out.println("HMAC encrypted data: "+HexFormat.of().formatHex(hmacCalculated));
    }


    private static void listAlgos() {
        for (Provider provider: Security.getProviders()) {
            System.out.println(provider.getName());
            for (String key: provider.stringPropertyNames())
                System.out.println("\t" + key + "\t" + provider.getProperty(key));
        }
    }
}
