package ch.oli.zipAnalyzer;

import java.io.IOException;

public class DecoderEndOfCentralDirectoryRecord extends Decoder {
    public int    thisDiskNum                 ;
    public int    diskNumCentralDir           ;
    public int    numCentralDirEntriesThisDisk;
    public int    numCentralDirEntriesTotal   ;
    public long   sizeOfCentralDir            ;
    public long   centralDirOffset            ;
    public int    commentLen                  ;
    public String comment                     ;

    @Override
    protected void internalParse() throws IOException {
        thisDiskNum                  = read2("thisDiskNum");
        diskNumCentralDir            = read2("diskNumCentral");
        numCentralDirEntriesThisDisk = read2("numEntriesDisk");
        numCentralDirEntriesTotal    = read2("numEntriesTotal");
        sizeOfCentralDir             = read4("sizeOfCentral");
        centralDirOffset             = read4("offsetOfCentral");
        commentLen                   = read2("commentLen" );
        comment = new String(read("comment",commentLen));
    }

    @Override
    public String toString() {
        return "comment=" + comment;
    }

}
