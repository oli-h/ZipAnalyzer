package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipAccess;

public class UnPackInfo {

    public final int numFolders;
    public final int external;
    public final Folder[] folders;
    public final int[] unPackDigests;

    public UnPackInfo(SevenZipAccess is) {
        int propId = is.BYTE();
        if (propId != 0x0B) {
            throw new RuntimeException("Upps " + propId);
        }
        numFolders = (int) is.UINT64();
        external = is.BYTE();
        if (external == 0) {
            folders = new Folder[numFolders];
            for (int i = 0; i < numFolders; i++) {
                folders[i] = new Folder(is);
            }
        } else if (external == 1) {
            folders = null;
            long dataStreamIndex = is.UINT64();
            throw new RuntimeException("external not yet supported");
        } else {
            throw new RuntimeException("Upps" + external);
        }

        propId = is.BYTE();
        if (propId != 0x0C) {
            throw new RuntimeException("Upps " + propId);
        }
        for (int i = 0; i < numFolders; i++) {
            Folder folder = folders[i];
            for (int s = 0; s < folder.numOutStreamsTotal; s++) {
                long unPackSize = is.UINT64();
            }
        }

        propId = is.BYTE();
        if (propId == 0x0A) {
            unPackDigests = is.readDigests(numFolders);
            propId = is.BYTE();
        } else {
            unPackDigests = null;
        }

        if (propId != 0x00) {
            throw new RuntimeException("Upps " + propId);
        }
    }

}
