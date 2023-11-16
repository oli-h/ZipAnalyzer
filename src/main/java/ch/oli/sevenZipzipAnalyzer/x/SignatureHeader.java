package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipAccess;

public class SignatureHeader {

    public final byte[] signature;
    public final int archiveVersionMajor;
    public final int archiveVersionMinor;
    public final int startHeaderCRC;
    // next three are the StartHeader
    public final long nextHeaderOffset;
    public final long nextHeaderSize;
    public final int nextHeaderCRC;

    public SignatureHeader(SevenZipAccess is) {
        signature           = is.readBytes(6);
        archiveVersionMajor = is.BYTE();
        archiveVersionMinor = is.BYTE();
        startHeaderCRC      = is.FIX_LENGTH_UINT32(); // over next 8+8+4 Bytes
        nextHeaderOffset    = is.FIX_LENGTH_UINT64();
        nextHeaderSize      = is.FIX_LENGTH_UINT64();
        nextHeaderCRC       = is.FIX_LENGTH_UINT32();
    }
}
