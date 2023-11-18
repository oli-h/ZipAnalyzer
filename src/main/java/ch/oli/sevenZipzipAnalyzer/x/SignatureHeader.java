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

    public SignatureHeader(SevenZipAccess sza) {
        signature           = sza.readBytes(6);
        archiveVersionMajor = sza.BYTE();
        archiveVersionMinor = sza.BYTE();
        startHeaderCRC      = sza.FIX_LENGTH_UINT32(); // over next 8+8+4 Bytes
        nextHeaderOffset    = sza.FIX_LENGTH_UINT64();
        nextHeaderSize      = sza.FIX_LENGTH_UINT64();
        nextHeaderCRC       = sza.FIX_LENGTH_UINT32();
    }
}
