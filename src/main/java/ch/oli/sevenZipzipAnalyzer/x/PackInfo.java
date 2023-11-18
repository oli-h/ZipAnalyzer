package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipAccess;

public class PackInfo {

    public final long packPos;
    public final int numPackStreams;
    public final long[] packSizes;
    public final int[] packStreamDigests;

    PackInfo(SevenZipAccess sza) {
        packPos = sza.UINT64();
        numPackStreams = (int) sza.UINT64();

        int propId = sza.BYTE();
        if (propId == 0x09) {
            packSizes = new long[numPackStreams];
            for (int i = 0; i < numPackStreams; i++) {
                packSizes[i] = sza.UINT64();
            }
            propId = sza.BYTE();
        } else {
            packSizes = null;
        }

        if (propId == 0x0A) {
            packStreamDigests= sza.readDigests(numPackStreams);
            propId = sza.BYTE();
        } else {
            packStreamDigests = null;
        }

        if (propId != 0x00) {
            throw new RuntimeException("Upps " + propId);
        }
    }

}
