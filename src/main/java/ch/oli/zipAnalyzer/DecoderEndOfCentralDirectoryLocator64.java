package ch.oli.zipAnalyzer;

import java.io.IOException;

public class DecoderEndOfCentralDirectoryLocator64 extends Decoder {
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
