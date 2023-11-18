package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipAccess;

public class Folder {

    final Coder[] coders;
    final int numInStreamsTotal;
    final int numOutStreamsTotal;

    public Folder(SevenZipAccess sza) {
        int numCoders = (int) sza.UINT64();
        coders = new Coder[numCoders];
        int numInStreamsTotal = 0;
        int numOutStreamsTotal = 0;
        for (int i = 0; i < numCoders; i++) {
            coders[i] = new Coder(sza);
            numInStreamsTotal += coders[i].numInStreams;
            numOutStreamsTotal += coders[i].numOutStreams;
        }
        this.numInStreamsTotal = numInStreamsTotal;
        this.numOutStreamsTotal = numOutStreamsTotal;
        int numBindPairs = numOutStreamsTotal - 1;
        for (int i = 0; i < numBindPairs; i++) {
            long inIndex = sza.UINT64();
            long outIndex = sza.UINT64();
        }

        int numPackedStreams = numInStreamsTotal - numBindPairs;
        if (numPackedStreams > 1) {
            for (int i = 0; i < numPackedStreams; i++) {
                long index = sza.UINT64();
            }
        }

    }

}
