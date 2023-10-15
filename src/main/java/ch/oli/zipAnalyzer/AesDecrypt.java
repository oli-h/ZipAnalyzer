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
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Set;

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
        int compressedSize = 174613; // hard coded for now, too: found in from local header
        byte[] compressedAndEncryptedFileData = is.readNBytes(compressedSize);

        byte[] decryptedFile = decryptAES256(compressedAndEncryptedFileData, password, 256);

        System.out.println(new String(decryptedFile));
    }

    /**
     * @param compressedAndEncryptedFileData bytes from the ZIP-File (the "file data" after Local Header)
     * @param password the encryption password
     * @param keySizeBits 128, 196 or 256 for AES128, AES196 or AES256
     */
    public static byte[] decryptAES256(byte[] compressedAndEncryptedFileData, String password, int keySizeBits) throws Exception {
        if (!Set.of(128, 196, 256).contains(keySizeBits)) {
            throw new RuntimeException("keySizeBits must be one of 128,196,256 but is " + keySizeBits);
        }
        final int sizeKey = keySizeBits / 8;

        // see https://www.winzip.com/en/support/aes-encryption/#file-format1
        final byte[] salt, pwVerifier, data, hmac;
        try (ByteArrayInputStream is = new ByteArrayInputStream(compressedAndEncryptedFileData)) {
            int sizeSalt       = sizeKey / 2; // e.g. 16 (128 bits) for AES256
            int sizePwVerifier =  2;          // size is fix
            int sizeHmac       = 10;          // size is fix
            int sizeData       = compressedAndEncryptedFileData.length - sizeSalt - sizePwVerifier - sizeHmac;
            salt       = is.readNBytes(sizeSalt      ); // the "Salt value"
            pwVerifier = is.readNBytes(sizePwVerifier); // the "Password verification value"
            data       = is.readNBytes(sizeData      ); // the "Encrypted file data"
            hmac       = is.readNBytes(sizeHmac      ); // the "Authentication code"
        }

        // see https://www.winzip.com/en/support/aes-encryption/#key-generation
        // see also https://github.com/lclevy/unarcrypto/blob/master/ziparchive.py
        final byte[] aesKey, macKey, pwVerifier2;
        {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, (2 * sizeKey + pwVerifier.length) * 8);
            byte[] derivedKeys = skf.generateSecret(spec).getEncoded();
            try (ByteArrayInputStream is = new ByteArrayInputStream(derivedKeys)) {
                aesKey      = is.readNBytes(sizeKey); // the "encryption key"
                macKey      = is.readNBytes(sizeKey); // the "authentication key"
                pwVerifier2 = is.readAllBytes()     ; // the "password verification value"
            }
        }

        System.out.println("PW-Verification in header    : " + HexFormat.of().formatHex(pwVerifier));
        System.out.println("PW-Verification from password: " + HexFormat.of().formatHex(pwVerifier2));
        // check password: all 16 bits must be identical
        // --> chance 1:65536 to accept wrong password - then we fail with check of HMAC-SHA1-80
        if (Arrays.compare(pwVerifier, pwVerifier2) != 0) {
            throw new RuntimeException("wrong passwort");
        }
        System.out.println("---------------------------------------------------------------");

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(macKey, "HmacSHA1"));
        byte[] hmac2 = mac.doFinal(data); // HMAC is calculated over _encrypted_ data
        // ZIP-AES uses HMAC-SHA1-80 - so compare only first 80 bits (=10 bytes)
        System.out.println("HMAC in header     : " + HexFormat.of().formatHex(hmac));
        System.out.println("HMAC encrypted data: " + HexFormat.of().formatHex(hmac2));
        if (Arrays.compare(hmac, 0, 10, hmac2, 0, 10) != 0) {
            throw new RuntimeException("authentication code mismatch");
        }
        System.out.println("---------------------------------------------------------------");

        // ZIP uses a "Little"-Endian counter-mode (CTR)
        // Unfortunately, Java's CounterMode is hard coded to "Big"-endian (see com.sun.crypto.provider.CounterMode#increment)
        // --> we can't use "AES/CTR/NoPadding" and we need to do our own CTR-Mode. Though, this is easy
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"));
        byte[] counter          = new byte[16];
        byte[] encryptedCounter = new byte[16];
        for (int i = 0; i < data.length; i++) {
            // for every block (= every 16 bytes): a) increase counter and b) encrypt counter
            if ((i & 15) == 0) {
                for (int n = 0; (n < counter.length) && (++counter[n] == 0); n++) ;
                cipher.update(counter, 0, 16, encryptedCounter);
            }
            data[i] ^= encryptedCounter[i & 15]; // decrypt by XORing
        }
        return data;
    }

    public static void listAlgos() {
        for (Provider provider: Security.getProviders()) {
            System.out.println(provider.getName());
            for (String key: provider.stringPropertyNames())
                System.out.println("\t" + key + "\t" + provider.getProperty(key));
        }
    }
}
