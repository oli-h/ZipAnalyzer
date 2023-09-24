package ch.oli.zipAnalyzer.util;

import java.io.*;

public class MyInputStream extends FilterInputStream {

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public MyInputStream(InputStream is) {
        super(is);
    }

    public void resetBuffer() {
        baos.reset();
    }

    public byte[] getAndResetBuffer() {
        byte[] buf = baos.toByteArray();
        baos.reset();
        return buf;
    }

    public byte[] read(int n) throws IOException {
        byte[] data = super.readNBytes(n);
        baos.write(data);
        return data;
    }

    public long read8() throws IOException {
        long d1 = read4();
        long d2 = read4();
        return (d2 << 32) | d1;
    }

    public long read4() throws IOException {
        long w1 = read2();
        long w2 = read2();
        return (w2 << 16) | w1;
    }

    public int read2() throws IOException {
        int b1 = super.read();
        int b2 = super.read();
        if (b1 < 0 || b2 < 0) {
            throw new EOFException();
        }
        baos.write(b1);
        baos.write(b2);
        return (b2 << 8) | b1;
    }

}
