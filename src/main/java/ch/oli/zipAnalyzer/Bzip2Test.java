package ch.oli.zipAnalyzer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ThreadLocalRandom;

public class Bzip2Test {

    public static void main(String[] args) throws Exception {
        byte[] buffer = new byte[1024];
        for (int i = 0; i < 1; i++) {
            buffer[i] = (byte) ThreadLocalRandom.current().nextInt();
        }

        Process bzip2process = new ProcessBuilder("bzip2").start();

        NullOutputStream nullOS = new NullOutputStream();

//        BZip2CompressorOutputStream os = new BZip2CompressorOutputStream(nullOS);
        OutputStream os = bzip2process.getOutputStream();
        long t0 = System.nanoTime();
        long uncompressedSize = 0;

        // capture bzip2 process's stdout
        Thread stdoutCaptureThread = new Thread(() -> {
            try {
                bzip2process.getInputStream().transferTo(nullOS);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        stdoutCaptureThread.start();

        for (int i = 0; i < 1024 * 1024; i++) {
            os.write(buffer);
            uncompressedSize += buffer.length;
        }
        os.close();
        stdoutCaptureThread.join();

        double percent = nullOS.count * 100. / uncompressedSize;
        System.out.printf("%10.3f millis. Reduce to %10.7f%%\n", (System.nanoTime() - t0) / 1_000_000., percent);
    }
}

class NullOutputStream extends OutputStream {
    public volatile int count = 0;

    @Override
    public void write(int b) {
        count++;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        count += len;
    }
}
