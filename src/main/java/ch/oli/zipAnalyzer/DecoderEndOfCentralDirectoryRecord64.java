package ch.oli.zipAnalyzer;

import java.io.IOException;

public class DecoderEndOfCentralDirectoryRecord64 extends Decoder {
    public long thisSize;
    public int  versionMadeBy;
    public int  versionNeedTpExtract;
    public long thisDiskNum;
    public long diskNumCentralDir;
    public long numCentralDirEntriesThisDisk;
    public long numCentralDirEntriesTotal;
    public long sizeOfCentralDir;
    public long centralDirOffset;

    @Override
    protected void internalParse() throws IOException {
        thisSize                     = read8("thisRecordSize");
        versionMadeBy                = read2("verM");
        versionNeedTpExtract         = read2("verN");
        thisDiskNum                  = read4("thisDiskNum");
        diskNumCentralDir            = read4("diskNumCentral");
        numCentralDirEntriesThisDisk = read8("numEntriesDisk" );
        numCentralDirEntriesTotal    = read8("numEntriesTotal");
        sizeOfCentralDir             = read8("sizeOfCentral");
        centralDirOffset             = read8("offsetOfCentral");
    }

    @Override
    public String toString() {
        return "";
    }
}
