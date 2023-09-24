package ch.oli.zipAnalyzer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipAnalyzer {
    public static void main(String[] args) throws Exception {
        Path path = Path.of("test.zip");
//        createTestZip(path);

        // Linux ZIP-create commands
        // zip test.zip test_0.txt            This creates NO DataDescriptor
        // zip - test_0.txt > test.zip        Surprisingly this also creates NO DataDescriptor. Seem to 'seek' in stdout
        // zip - test_0.txt | cat > test.zip  This finally enforces creation a DataDescriptor
        // 7z a test.zip test_0.txt


        try (MyInputStream mis = new MyInputStream(new BufferedInputStream(Files.newInputStream(path), 1024))) {
            int pos = 0;
            boolean nextDataDescriptorIsZip64 = false;
            while (true) {
                mis.mark(4);
                int id = (int) mis.read4();
                mis.reset();
                ZipSig zipSig = ZipSig.of(id);

                if (zipSig == null) {
                    mis.skip(1);
                    pos++;
                    continue;
                }

                Decoder decoder = zipSig.decoderClass.getDeclaredConstructor().newInstance();
                if(decoder instanceof DecoderDataDescriptor && nextDataDescriptorIsZip64) {
                    ((DecoderDataDescriptor)decoder).parseAsZip64();
                }
                decoder.parse(mis);
                nextDataDescriptorIsZip64 = decoder.foundZip64Extra;

                System.out.format("%6x %-14s: ", pos, zipSig);
                System.out.print(decoder);
                System.out.println();
                System.out.println("                       " + decoder.rawAsString());
                System.out.println("                       " + decoder.fieldNamesAsString());

                pos += decoder.raw.length;
            }
        } catch (EOFException ex) {
            // silently ignore
        }
    }

    private static void createTestZip(Path path) throws IOException {
        byte[] dummy = new byte[1024];
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            for (int fileNum = 0; fileNum < 1; fileNum++) {
                ZipEntry ze = new ZipEntry("ä_test_" + fileNum + ".txt");
                ze.setSize(0xFFFF_FFFFL);
                zos.putNextEntry(ze);
                for (int i = 0; i < 4 * 1024 * 1024; i++) {
                    zos.write(dummy);
                }
                zos.write(1);
            }
        }
    }
}