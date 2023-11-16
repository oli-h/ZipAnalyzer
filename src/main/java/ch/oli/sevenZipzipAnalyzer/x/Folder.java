package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipInputStream;

public class Folder {

    final Coder[] coders;
    final int numInStreamsTotal;
    final int numOutStreamsTotal;

    public Folder(SevenZipInputStream is) {
        int numCoders = (int) is.UINT64();
        coders = new Coder[numCoders];
        int numInStreamsTotal = 0;
        int numOutStreamsTotal = 0;
        for (int i = 0; i < numCoders; i++) {
            coders[i] = new Coder(is);
            numInStreamsTotal += coders[i].numInStreams;
            numOutStreamsTotal += coders[i].numOutStreams;
        }
        this.numInStreamsTotal = numInStreamsTotal;
        this.numOutStreamsTotal = numOutStreamsTotal;
        int numBindPairs = numOutStreamsTotal - 1;
        for (int i = 0; i < numBindPairs; i++) {
            long inIndex = is.UINT64();
            long outIndex = is.UINT64();
        }

        int numPackedStreams = numInStreamsTotal - numBindPairs;
        if (numPackedStreams > 1) {
            for (int i = 0; i < numPackedStreams; i++) {
                long index = is.UINT64();
            }
        }

    }

}
