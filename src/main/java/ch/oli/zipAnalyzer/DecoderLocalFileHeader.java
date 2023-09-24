package ch.oli.zipAnalyzer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class DecoderLocalFileHeader extends Decoder {
    public int    versionNeededToExtract;
    public int    flags                 ;
    public int    method                ;
    public int    fileTime              ;
    public int    fileDate              ;
    public long   crc                   ;
    public long   sizeCompr             ;
    public long   sizeUncompr           ;
    public int    fileNameLen           ;
    public int    extraFieldLen         ;
    public String filename              ;

    @Override
    protected void internalParse() throws IOException {
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

        Charset cs;
        if (((flags >> 11) & 1) == 1) {
            cs = StandardCharsets.UTF_8;
        } else {
            cs = Charset.forName("IBM437");
        }
        filename = new String(read("name",fileNameLen), cs);
        decodeExtras();
    }

    protected void decodeExtras() throws IOException {
        for (int remain = extraFieldLen; remain >= 4; ) {
            int id = read2("xtra");
            int len = read2("xlen");
            remain -= 4;
            String name = switch (id) {
                case 0x0001 -> "Zip64 extended info";
                case 0x000a -> "NTFS";
                case 0x5455 -> "extended-timestamp";
                case 0x7875 -> "newer UID/GID";
                default -> "?";
            };
            if (id == 0x0001) {
                foundZip64Extra = true;
            }
            read(name, len);
            remain -= len;
        }
    }

    @Override
    public String toString() {
        String s = filename;
        if(foundZip64Extra) {
            s+=" (Zip64)";
        }
        if (((flags >> 3) & 1) == 1) {
            s += " (with data descriptor)";
        }
        if ((flags & 1) == 1) {
            s += " (encrypted)";
        }
        return s;
    }
}
