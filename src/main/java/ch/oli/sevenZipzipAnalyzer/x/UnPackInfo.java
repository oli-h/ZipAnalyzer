package ch.oli.sevenZipzipAnalyzer.x;

import ch.oli.sevenZipzipAnalyzer.SevenZipAccess;

public class UnPackInfo {

    public final int numFolders;
    public final int external;
    public final Folder[] folders;
    public final int[] unPackDigests;

    public UnPackInfo(SevenZipAccess sza) {
        int propId = sza.BYTE();
        if (propId != 0x0B) {
            throw new RuntimeException("Upps " + propId);
        }
        numFolders = (int) sza.UINT64();
        external = sza.BYTE();
        if (external == 0) {
            folders = new Folder[numFolders];
            for (int i = 0; i < numFolders; i++) {
                folders[i] = new Folder(sza);
            }
        } else if (external == 1) {
            folders = null;
            long dataStreamIndex = sza.UINT64();
            throw new RuntimeException("external not yet supported");
        } else {
            throw new RuntimeException("Upps" + external);
        }

        propId = sza.BYTE();
        if (propId != 0x0C) {
            throw new RuntimeException("Upps " + propId);
        }
        for (int i = 0; i < numFolders; i++) {
            Folder folder = folders[i];
            for (int s = 0; s < folder.numOutStreamsTotal; s++) {
                long unPackSize = sza.UINT64();
            }
        }

        propId = sza.BYTE();
        if (propId == 0x0A) {
            unPackDigests = sza.readDigests(numFolders);
            propId = sza.BYTE();
        } else {
            unPackDigests = null;
        }

        if (propId != 0x00) {
            throw new RuntimeException("Upps " + propId);
        }
    }

}
