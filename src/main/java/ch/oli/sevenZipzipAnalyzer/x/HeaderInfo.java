package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipAccess;

public class HeaderInfo {
    public final StreamsInfo streamInfo;

    public HeaderInfo(SevenZipAccess is) {
        streamInfo=new StreamsInfo(is);
    }
}
