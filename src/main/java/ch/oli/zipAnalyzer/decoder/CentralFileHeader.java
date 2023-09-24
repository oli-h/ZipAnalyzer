package ch.oli.zipAnalyzer.decoder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * chapter 4.3.12 Central directory structure
 */
public class CentralFileHeader extends LocalFileHeader {
    // only additional attributes compared to LocalFileHeader
    public int    versionMadeBy    ;
    public int    fileCommentLen   ;
    public int    diskNumStart     ;
    public int    internalFileAttr ;
    public long   externalFileAttr ;
    public long   offsetLocalHeader;
    public String comment          ;

    @Override
    protected void internalParse() throws IOException {
        versionMadeBy          = read2("verM");
        versionNeededToExtract = read2("verN");
        flags                  = read2("flag");
        method                 = read2("mthd");
        fileTime               = read2("time");
        fileDate               = read2("date");
        crc                    = read4("crc" );
        sizeCompr              = read4("sizeCompr");
        sizeUncompr            = read4("size");
        fileNameLen            = read2("nameL");
        extraFieldLen          = read2("xtraL");
        fileCommentLen         = read2("commentL");
        diskNumStart           = read2("diskNumStart");
        internalFileAttr       = read2("iAttr");
        externalFileAttr       = read4("eAttr");
        offsetLocalHeader      = read4("offsetOfLocal");

        Charset cs;
        if (((flags >> 11) & 1) == 1) {
            cs = StandardCharsets.UTF_8;
        } else {
            cs = Charset.forName("IBM437");
        }
        filename = new String(read("name", fileNameLen), cs);
        super.decodeExtras();
        comment = new String(read("comment", fileCommentLen), cs);
    }
}
