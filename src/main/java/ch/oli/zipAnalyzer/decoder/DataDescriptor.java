package ch.oli.zipAnalyzer.decoder;

import java.io.IOException;

/**
 * chapter 4.3.9  Data descriptor (but also 8.5.3 Spanned/Split archives)
 */
public class DataDescriptor extends Decoder {
    public long crc        ;
    public long sizeCompr  ;
    public long sizeUncompr;
    private boolean pasreAsZip64;

    public void parseAsZip64() {
        pasreAsZip64 = true;
    }

    @Override
    protected void internalParse() throws IOException {
        crc         = read4("crc" );
        if (pasreAsZip64) {
            sizeCompr   = read8("sizeCompr");
            sizeUncompr = read8("size");
        } else {
            sizeCompr   = read4("sizeCompr");
            sizeUncompr = read4("size");
        }
    }

    @Override
    public String toString() {
        return pasreAsZip64 ? "parsed as ZIP64 as there was a Zip64-Extra-Field in immediately preceding LocalHeader" : "parsed normally";
    }

}
