package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipAccess;

public class HeaderInfo {
    public final StreamsInfo streamInfo;

    public HeaderInfo(SevenZipAccess sza) {
        streamInfo=new StreamsInfo(sza);
    }
}
