package ch.oli.zipAnalyzer;

import java.util.Arrays;
import java.util.HashMap;

public enum ZipSig {
    SIG_LOCAL     (0x04034b50, DecoderLocalFileHeader               .class), // chapter 4.3.7  Local file header
    SIG_DATA_DESCR(0x08074b50, DecoderDataDescriptor.class                ), // chapter 4.3.9  Data descriptor (but also 8.5.3 Spanned/Split archives)
    SIG_ARCH_EXTRA(0x08064b50, null                                       ), // chapter 4.3.11 Archive extra data record
    SIG_CENTRAL   (0x02014b50, DecoderCentralFileHeader             .class), // chapter 4.3.12 Central directory structure
    SIG_DIGISIGN  (0x05054b50, null                                       ), // chapter 4.3.13 Digital signature
    SIG_64EOCD_REC(0x06064b50, DecoderEndOfCentralDirectoryRecord64 .class), // chapter 4.3.14 Zip64 end of central directory record
    SIG_64EOCD_LOC(0x07064b50, DecoderEndOfCentralDirectoryLocator64.class), // chapter 4.3.15 Zip64 end of central directory locator
    SIG_EOCD_REC  (0x06054b50, DecoderEndOfCentralDirectoryRecord   .class); // chapter 4.3.16 End of central directory record

    public final int signature;
    public final  Class<? extends Decoder> decoderClass;

    private static final HashMap<Integer, ZipSig> MAP = new HashMap<>();
    static {
        Arrays.stream(ZipSig.values()).forEach(zs -> MAP.put(zs.signature, zs));
    }

    ZipSig(int signature, Class<? extends Decoder> decoderClass) {
        this.signature = signature;
        this.decoderClass = decoderClass;
    }

    public static ZipSig of(int signature) {
        return MAP.get(signature);
    }
}
