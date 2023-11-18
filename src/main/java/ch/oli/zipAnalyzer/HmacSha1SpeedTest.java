package ch.oli.zipAnalyzer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacSha1SpeedTest {

    public static void main(String[] args) throws Exception {
        byte[] macKey = new byte[16];
        byte[] data = new byte[1024];

        long t0 = System.nanoTime();
        for (int i = 1; ; i++) {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(macKey, "HmacSHA1"));
            for (int x = 0; x < 1024; x++) {
                mac.update(data);
            }
            byte[] hmac2 = mac.doFinal();

            long t = System.nanoTime() - t0;
            System.out.printf("%.3f /s\n", i / (t / 1_000_000_000.));
        }
    }
}
