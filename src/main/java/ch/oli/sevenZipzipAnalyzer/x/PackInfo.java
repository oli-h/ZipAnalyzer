package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipInputStream;

public class PackInfo {

    final long packPos;
    final int numPackStreams;
    final long[] packSizes;
    final int[] packStreamDigests;

    PackInfo(SevenZipInputStream is) {
        packPos = is.UINT64();
        numPackStreams = (int) is.UINT64();

        int propId = is.BYTE();
        if (propId == 0x09) {
            packSizes = new long[numPackStreams];
            for (int i = 0; i < numPackStreams; i++) {
                packSizes[i] = is.UINT64();
            }
            propId = is.BYTE();
        } else {
            packSizes = null;
        }

        if (propId == 0x0A) {
            packStreamDigests=is.readDigests(numPackStreams);
            propId = is.BYTE();
        } else {
            packStreamDigests = null;
        }

        if (propId != 0x00) {
            throw new RuntimeException("Upps " + propId);
        }
    }

}
