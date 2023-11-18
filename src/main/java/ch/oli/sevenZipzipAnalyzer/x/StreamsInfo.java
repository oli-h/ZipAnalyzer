package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipAccess;

public class StreamsInfo {
    public final PackInfo packInfo;
    public final UnPackInfo unPackInfo;
    public final SubStreamsInfo subStreamsInfo;

    public StreamsInfo(SevenZipAccess sza) {
        int propId = sza.BYTE();

        if (propId == 0x06) {
            packInfo = new PackInfo(sza);
            propId = sza.BYTE();
        } else {
            packInfo = null;
        }

        if (propId == 0x07) {
            unPackInfo = new UnPackInfo(sza);
            propId = sza.BYTE();
        } else {
            unPackInfo = null;
        }

        if (propId == 0x08) {
            subStreamsInfo = new SubStreamsInfo(sza);
            propId = sza.BYTE();
        } else {
            subStreamsInfo = null;
        }

        if (propId != 0x00) {
            throw new RuntimeException("Upps " + propId);
        }
    }
}
