package ch.oli.zipAnalyzer;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HexFormat;

public class AesDecrypt {

    public static void main(String[] args) throws Exception {
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
        byte[] chipherText     = is.readNBytes(sizeChiffre   );
        byte[] hmac            = is.readNBytes(sizeHmac      );

        // see https://github.com/lclevy/unarcrypto/blob/master/ziparchive.py
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, (32 + 32 + 2) * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] derivedKeys = skf.generateSecret(spec).getEncoded();
        byte[] aesKey          = Arrays.copyOfRange(derivedKeys,  0, 32);
        byte[] macKey          = Arrays.copyOfRange(derivedKeys, 32, 64);
        byte[] pwVerification2 = Arrays.copyOfRange(derivedKeys, 64, 66);

        // must be identical
        System.out.println(HexFormat.of().formatHex(pwVerification1));
        System.out.println(HexFormat.of().formatHex(pwVerification2));

        SecretKeySpec secretKey = new SecretKeySpec(aesKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        // ZIP-AES uses Little-Endian-Counter but "AES/CTR/NoPadding" would increase counter as Bit-Endian
        // Therefore we need to count by ourselves and re-init the cipher for every single block :-/
        byte[] counter = new byte[16];
        for (int block = 0; block < chipherText.length / 16; block++) {
            long xxx = block + 1;
            for (int i = 0; xxx > 0; i++) {
                counter[i] = (byte) (xxx);
                xxx >>= 8;
            }
            IvParameterSpec iv = new IvParameterSpec(counter);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

            byte[] clearText = cipher.doFinal(Arrays.copyOfRange(chipherText, block * 16, block * 16 + 16));
            System.out.print(new String(clearText));
        }

//        for (Provider provider: Security.getProviders()) {
//            System.out.println(provider.getName());
//            for (String key: provider.stringPropertyNames())
//                System.out.println("\t" + key + "\t" + provider.getProperty(key));
//        }

//        int pos = 0;
//        while (true) {
//            System.out.printf("%04x:", pos);
//            for (int i = 0; i < 16; i++) {
//                int val = is.read();
//                if (val < 0) {
//                    return;
//                }
//                System.out.printf(" %02x", val & 0xFF);
//                pos++;
//            }
//            System.out.println();
//        }
    }

}
