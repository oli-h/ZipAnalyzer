package ch.oli.zipAnalyzer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class DecoderCentralFileHeader extends DecoderLocalFileHeader {
    public int    versionMadeBy         ;
//    public int    versionNeededToExtract;
//    public int    flags                 ;
//    public int    method                ;
//    public int    fileTime              ;
//    public int    fileDate              ;
//    public long   crc                   ;
//    public long   sizeCompr             ;
//    public long   sizeUncompr           ;
//    public int    fileNameLen           ;
//    public int    extraFieldLen         ;
    public int    fileCommentLen        ;
    public int    diskNumStart          ;
    public int    internalFileAttr      ;
    public long   externalFileAttr      ;
    public long   offsetLocalHeader     ;
//    public String filename              ;
    public String comment               ;

    @Override
    protected void internalParse() throws IOException {
        versionMadeBy          = read2("verM");
        versionNeededToExtract = read2("verN");
        flags                  = read2("flag");
        method                 = read2("mthd");
        fileTime               = read2("time");
        fileDate               = read2("date");
        crc                    = read4("crc" );
        sizeCompr              = read4("sizeCompressed");
        sizeUncompr            = read4("size");
        fileNameLen            = read2("nameLen");
        extraFieldLen          = read2("xtraLen");
        fileCommentLen         = read2("commentLen" );
        diskNumStart           = read2("diskNumStart");
        internalFileAttr       = read2("iAttr"   );
        externalFileAttr       = read4("eAttr"   );
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
