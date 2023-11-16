package ch.oli.sevenZipzipAnalyzer;

import ch.oli.sevenZipzipAnalyzer.x.HeaderInfo;
import ch.oli.sevenZipzipAnalyzer.x.SignatureHeader;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class SevenZipAnalyze {
    public static void main(String[] args) throws Exception {
        Path path = Path.of("test.7z");
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
            SevenZipAccess is = new SevenZipAccess(raf);
            SignatureHeader signatureHeader = new SignatureHeader(is);
            is.skip(signatureHeader.nextHeaderOffset);
            int propId = is.BYTE();
            if (propId != 0x17) {
                throw new RuntimeException("Upps " + propId);
            }
            HeaderInfo headerInfo = new HeaderInfo(is);
            System.out.println(headerInfo);

            raf.seek(20 + headerInfo.streamInfo.packInfo.packPos);
            byte[] pack = new byte[(int) headerInfo.streamInfo.packInfo.packSizes[0]];
            raf.readFully(pack);
            Files.write(Path.of("pack.lzma"),pack);
        }
    }

}
