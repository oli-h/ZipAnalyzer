package ch.oli.zipAnalyzer.decoder;

import java.io.IOException;

/**
 * chapter 4.3.15 Zip64 end of central directory locator
 */
public class EndOfCentralDirectoryLocator64 extends Decoder {
    public long diskNumCentralDir;
    public long offset;
    public long totalNumDisks;

    @Override
    protected void internalParse() throws IOException {
        diskNumCentralDir = read4("diskNum");
        offset            = read8("offs" );
        totalNumDisks     = read4("totalNumDisks");
    }

    @Override
    public String toString() {
        return "";
    }
}
