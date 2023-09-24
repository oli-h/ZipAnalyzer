package ch.oli.zipAnalyzer;

import ch.oli.zipAnalyzer.decoder.DataDescriptor;
import ch.oli.zipAnalyzer.decoder.Decoder;
import ch.oli.zipAnalyzer.util.ANSI;
import ch.oli.zipAnalyzer.util.MyInputStream;

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


        try (MyInputStream myIS = new MyInputStream(new BufferedInputStream(Files.newInputStream(path), 1024))) {
            int pos = 0;
            boolean nextDataDescriptorIsZip64 = false;
            while (true) {
                Decoder decoder = Decoder.select(myIS);
                if (decoder == null) {
                    myIS.skip(1);
                    pos++;
                    continue;
                }

                if (decoder instanceof DataDescriptor && nextDataDescriptorIsZip64) {
                    ((DataDescriptor) decoder).parseAsZip64();
                }
                decoder.parse(myIS);
                nextDataDescriptorIsZip64 = decoder.foundZip64Extra;

                System.out.format(ANSI.BOLD + "%7x %s: ", pos, decoder.name());
                System.out.print(decoder);
                System.out.println(ANSI.RESET);
                System.out.println("        " + decoder.rawAsString());
                System.out.println("        " + decoder.fieldNamesAsString());

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