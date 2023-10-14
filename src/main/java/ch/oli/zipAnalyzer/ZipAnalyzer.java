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
//        createTestZip(Path.of("test.zip");

//        Path path = Path.of("testOneLargeFileCreatedWithJava.zip");
//        Path path = Path.of("testOneLargeFileCreatedWithInfoZIP.zip");
//        Path path = Path.of("testOneLargeFileCreatedWithInfoZIPstreaming.zip");
//        Path path = Path.of("testSplit.z01");
//        Path path = Path.of("testBzip2.zip");
//        Path path = Path.of("testLZMA.zip");
//        Path path = Path.of("infoZip64DataDescriptor.zip");
        Path path = Path.of("crypt-aes.zip");

        // Linux ZIP-create commands
        // zip test.zip test_0.txt                      This creates NO DataDescriptor
        // zip - test_0.txt > test.zip                  Surprisingly this also creates NO DataDescriptor. Seem to 'seek' in stdout
        // zip - test_0.txt | cat > infoZip64DataDescriptor.zip This finally enforces DataDescriptor
        // zip -s 64k testSplit.zip test/*.txt          create Split (z01, z02, ..., zip)
        // zip -Zb testBzip2.zip test/large.bin         create ZIP with bzip2-compression-entries
        //
        // 7z a test.zip test_0.txt
        // 7z a -mm=LZMA testLZMA.zip test/large.bin
        // 7z a -pGEHEIM -mem=AES256 -mx=0 crypt-aes.zip APPNOTE.TXT // -mx=0 = STORE

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
                ZipEntry ze = new ZipEntry("test_" + fileNum + ".txt");
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