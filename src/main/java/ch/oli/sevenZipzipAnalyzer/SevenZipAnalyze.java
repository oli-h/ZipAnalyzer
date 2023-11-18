package ch.oli.sevenZipzipAnalyzer;

import ch.oli.sevenZipzipAnalyzer.x.HeaderInfo;
import ch.oli.sevenZipzipAnalyzer.x.SignatureHeader;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class SevenZipAnalyze {
    public static void main(String[] args) throws Exception {
        Path path = Path.of("test.7z");
        try (SevenZipAccess sza = new SevenZipAccess(path)) {
            SignatureHeader signatureHeader = new SignatureHeader(sza);
            sza.skip(signatureHeader.nextHeaderOffset);
            int propId = sza.BYTE();
            if (propId != 0x17) {
                throw new RuntimeException("Upps " + propId);
            }
            HeaderInfo headerInfo = new HeaderInfo(sza);
            System.out.println(headerInfo);

            sza.seek(20 + headerInfo.streamInfo.packInfo.packPos);
            byte[] pack = sza.readBytes(headerInfo.streamInfo.packInfo.packSizes[0]);
            Files.write(Path.of("pack.lzma"), pack);
        }
    }

}
