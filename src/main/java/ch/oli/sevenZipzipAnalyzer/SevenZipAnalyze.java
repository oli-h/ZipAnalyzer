package ch.oli.sevenZipzipAnalyzer;

import ch.oli.sevenZipzipAnalyzer.x.HeaderInfo;
import ch.oli.sevenZipzipAnalyzer.x.SignatureHeader;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SevenZipAnalyze {
    public static void main(String[] args) throws Exception {
        Path path = Path.of("test.7z");
        try (InputStream fis = Files.newInputStream(path)) {
            SevenZipInputStream is = new SevenZipInputStream(fis);
            SignatureHeader signatureHeader = new SignatureHeader(is);
            is.skip(signatureHeader.nextHeaderOffset);
            int propId = is.BYTE();
            if (propId == 0x17) {
                HeaderInfo headerInfo = new HeaderInfo(is);
                System.out.println(headerInfo);
            } else {
                throw new RuntimeException("Upps " + propId);
            }
        }
    }

}
