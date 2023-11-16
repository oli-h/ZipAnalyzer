package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipInputStream;

public class StreamsInfo {
    public final PackInfo packInfo;
    public final UnPackInfo unPackInfo;
    public final SubStreamsInfo subStreamsInfo;

    public StreamsInfo(SevenZipInputStream is) {
        int propId = is.BYTE();

        if (propId == 0x06) {
            packInfo = new PackInfo(is);
            propId = is.BYTE();
        } else {
            packInfo = null;
        }

        if (propId == 0x07) {
            unPackInfo = new UnPackInfo(is);
            propId = is.BYTE();
        } else {
            unPackInfo = null;
        }

        if (propId == 0x08) {
            subStreamsInfo = new SubStreamsInfo(is);
            propId = is.BYTE();
        } else {
            subStreamsInfo = null;
        }

        if (propId != 0x00) {
            throw new RuntimeException("Upps " + propId);
        }
    }
}
