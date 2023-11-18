package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipAccess;

public class Coder {

    public final byte[] codecId;
    public final boolean isComplexCoder;
    public final boolean thereAreAttributes;
    public final int numInStreams;
    public final int numOutStreams;
    public final byte[] properties;

    Coder(SevenZipAccess sza) {
        int tmp = sza.BYTE();
        int codecIdSize = tmp & 15;
        isComplexCoder = (tmp & 0x10) > 0;
        thereAreAttributes = (tmp & 0x20) > 0;
        codecId = sza.readBytes(codecIdSize);

        if (isComplexCoder) {
            numInStreams = (int) sza.UINT64();
            numOutStreams = (int) sza.UINT64();
        } else {
            numInStreams = 1;
            numOutStreams = 1;
        }

        if (thereAreAttributes) {
            int propertiesSize = (int) sza.UINT64();
            properties = sza.readBytes(propertiesSize);
        } else {
            properties = null;
        }
    }
}
