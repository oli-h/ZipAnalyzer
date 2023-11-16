package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipAccess;

public class PackInfo {

    public final long packPos;
    public final int numPackStreams;
    public final long[] packSizes;
    public final int[] packStreamDigests;

    PackInfo(SevenZipAccess is) {
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
