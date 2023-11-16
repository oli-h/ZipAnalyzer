package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipInputStream;

public class HeaderInfo {
    public final StreamsInfo streamInfo;

    public HeaderInfo(SevenZipInputStream is) {
        streamInfo=new StreamsInfo(is);
    }
}
