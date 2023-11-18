package ch.oli.sevenZipzipAnalyzer;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

public class SevenZipAccess implements Closeable {

    private final RandomAccessFile raf;

    public SevenZipAccess(Path path) throws FileNotFoundException {
        this.raf = new RandomAccessFile(path.toFile(), "r");
    }

    public long UINT64() {
        long value = 0;
        long b = BYTE();
        for (int i = 0; i < 8; i++) {
            if ((b & (0x80 >> i)) == 0) {
                value |= (b & (0x7F >> i)) << (i * 8);
                break;
            }
            value |= ((long) BYTE() << (i * 8));
        }
        return value;
    }

    public long FIX_LENGTH_UINT64() {
        return readInt(8);
    }

    public int FIX_LENGTH_UINT32() {
        return (int) readInt(4);
    }

    public int BYTE() {
        return (int) readInt(1);
    }

    private long readInt(int numBytes) {
        try {
            long value = 0;
            for (int i = 0; i < numBytes; i++) {
                int b = raf.read();
                if (b < 0) {
                    throw new RuntimeException("EOF");
                }
                value |= ((long) b << (i * 8));
            }
            return value;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readBytes(long numBytes) {
        try {
            byte[] ret = new byte[Math.toIntExact(numBytes)];
            raf.readFully(ret);
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void skip(long numBytes) {
        try {
            raf.seek(raf.getFilePointer() + numBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int[] readDigests(int num) {
        int allAreDefined = BYTE();
        byte[] definedMask = null;
        if (allAreDefined == 0) {
            definedMask = readBytes((num + 7) / 8);
        }
        int[] digests = new int[num];
        for (int i = 0; i < num; i++) {
            boolean defined = true;
            if (definedMask != null) {
                defined = (definedMask[i / 8] << (i % 8)) > 0;
            }
            if (defined) {
                digests[i] = FIX_LENGTH_UINT32();
            }
        }
        return digests;
    }

    public void seek(long pos) throws IOException {
        raf.seek(pos);
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

}