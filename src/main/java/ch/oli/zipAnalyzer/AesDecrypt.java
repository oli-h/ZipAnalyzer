package ch.oli.zipAnalyzer;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Provider;
import java.security.Security;
import java.util.HexFormat;

public class AesDecrypt {

    public static void main(String[] args) throws Exception {
//        listAlgos();

        // First: create an AES-Encrypted ZIP-file with 7-Zip-Command-Line-Tool:
        //      7z a -pGEHEIM -mem=AES256 -mx=0 crypt-aes.zip APPNOTE.TXT
        // -pGEHEIM    : sets encryption password
        // -mem=AES256 : sets encryption method
        // -mx=0       : no compression (i.e. 'STORE') so we can easily print the decrypted APPNOTE.TXT to console here
        Path path = Path.of("crypt-aes.zip");
        String password = "GEHEIM";
        InputStream is = Files.newInputStream(path);
        is.skip(52);                       // hard coded for now
        final int compressedSize = 174613; // hard coded for now, too: found in from local header

        // see https://www.winzip.com/en/support/aes-encryption/#file-format1
        int sizeSalt       = 16; // for AES256: 128 bits = 16 bytes
        int sizePwVerifier =  2;
        int sizeData       = compressedSize - sizeSalt - sizePwVerifier - 10;
        int sizeHmac       = 10;
        byte[] salt               = is.readNBytes(sizeSalt      );
        byte[] pwVerifierInHeader = is.readNBytes(sizePwVerifier);
        byte[] data               = is.readNBytes(sizeData      );
        byte[] hmacInHeader       = is.readNBytes(sizeHmac      );

        // see https://www.winzip.com/en/support/aes-encryption/#key-generation
        // see also https://github.com/lclevy/unarcrypto/blob/master/ziparchive.py
        int keySize = 32; // 32 bytes = 256 bits for AES256
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, (2 * keySize + 2) * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] derivedKeys = skf.generateSecret(spec).getEncoded();
        ByteArrayInputStream bais = new ByteArrayInputStream(derivedKeys);
        byte[] aesKey     = bais.readNBytes(keySize); // the "encryption key"
        byte[] macKey     = bais.readNBytes(keySize); // the "authentication key"
        byte[] pwVerifier = bais.readNBytes(2);       // the "password verification value"

        // check password: those 16 bits must be identical (i.e.: chance 1:65536 to have a wrong password and to decrypt to rubbish)
        System.out.println("PW-Verification in header    : " + HexFormat.of().formatHex(pwVerifierInHeader));
        System.out.println("PW-Verification from password: " + HexFormat.of().formatHex(pwVerifier));
        System.out.println("---------------------------------------------------------------");

        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec macSecretKey = new SecretKeySpec(macKey, "HmacSHA1");
        mac.init(macSecretKey);
        byte[] hmacOfEncryptedData = mac.doFinal(data);
        // check HMAC: first 10 bytes (80 bits) bits must be identical as it's a HMAC-SHA1-80
        System.out.println("HMAC in header     : " + HexFormat.of().formatHex(hmacInHeader));
        System.out.println("HMAC encrypted data: " + HexFormat.of().formatHex(hmacOfEncryptedData));
        System.out.println("---------------------------------------------------------------");

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
                data[i] ^= encryptedCounter[j];
            }
        }
        System.out.println(new String(data));
    }


    private static void listAlgos() {
        for (Provider provider: Security.getProviders()) {
            System.out.println(provider.getName());
            for (String key: provider.stringPropertyNames())
                System.out.println("\t" + key + "\t" + provider.getProperty(key));
        }
    }
}
