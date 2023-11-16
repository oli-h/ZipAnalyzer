package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipInputStream;

public class Coder {

    final byte[] codecId;
    final boolean isComplexCoder;
    final boolean thereAreAttributes;

    final int numInStreams;
    final int numOutStreams;

    final byte[] properties;

    Coder(SevenZipInputStream is) {
        int tmp = is.BYTE();
        int codecIdSize = tmp & 15;
        isComplexCoder = (tmp & 0x10) > 0;
        thereAreAttributes = (tmp & 0x20) > 0;
        codecId = is.readBytes(codecIdSize);

        if (isComplexCoder) {
            numInStreams = (int) is.UINT64();
            numOutStreams = (int) is.UINT64();
        } else {
            numInStreams = 1;
            numOutStreams = 1;
        }

        if (thereAreAttributes) {
            int propertiesSize = (int) is.UINT64();
            properties = is.readBytes(propertiesSize);
        } else {
            properties = null;
        }
    }
}
