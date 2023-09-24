package ch.oli.zipAnalyzer.decoder;

import ch.oli.zipAnalyzer.util.ANSI;
import ch.oli.zipAnalyzer.util.MyInputStream;

import java.io.IOException;
import java.util.ArrayList;

public abstract class Decoder {

    private MyInputStream myIS;
    private ArrayList<Field> fields = new ArrayList<>();
    public boolean foundZip64Extra = false;

    public int signature;
    public byte[] raw;

    public static Decoder select(MyInputStream myIS) throws IOException {
        myIS.mark(4);
        int signatureCandidate = (int) myIS.read4();
        myIS.reset();

        return switch (signatureCandidate) {
            case 0x04034b50 -> new LocalFileHeader();
            case 0x08074b50 -> new DataDescriptor();
            case 0x08064b50 -> null; // chapter 4.3.11 Archive extra data record
            case 0x02014b50 -> new CentralFileHeader();
            case 0x05054b50 -> null; // chapter 4.3.13 Digital signature
            case 0x06064b50 -> new EndOfCentralDirectoryRecord64();
            case 0x07064b50 -> new EndOfCentralDirectoryLocator64();
            case 0x06054b50 -> new EndOfCentralDirectoryRecord();
            default -> null;
        };
    }

    public void parse(MyInputStream myIS) throws IOException {
        this.myIS = myIS;
        this.myIS.resetBuffer();
        this.signature = (int) read4("signatur");
        this.internalParse();
        this.raw = myIS.getAndResetBuffer();
        this.myIS = null;
    }

    public final String name() {
        return this.getClass().getSimpleName();
    }

    protected abstract void internalParse() throws IOException;

    protected byte[] read(String name, int len) throws IOException {
        if (len == 0) {
            return new byte[0];
        }
        var value = myIS.read(len);
        fields.add(new Field(name, len));
        return value;
    }

    protected long read8(String name) throws IOException {
        var value = myIS.read8();
        fields.add(new Field(name, 8));
        return value;
    }

    protected long read4(String name) throws IOException {
        var value = myIS.read4();
        fields.add(new Field(name, 4));
        return value;
    }

    protected int read2(String name) throws IOException {
        var value = myIS.read2();
        fields.add(new Field(name, 2));
        return value;
    }

    public String rawAsString() {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for (Field fd : fields) {
            sb.append(ANSI.UNDERLINE);
            sb.append(colorForFieldName(fd.name));
            for (int i = 0; i < fd.length; i++) {
                int value = raw[pos++] & 0xFF;
                sb.append(String.format("%02x", value));
            }
            int charsWritten = fd.length*2;
            int charsName = fd.name.length();
            for (int i = charsWritten; i < charsName; i++) {
                sb.append(" ");
            }
            sb.append(ANSI.RESET);
            sb.append(" ");
        }
        return sb.toString();
    }

    public String fieldNamesAsString() {
        StringBuilder sb = new StringBuilder();
        for (Field fd : fields) {
            if (fd.length > 0) {
                sb.append(colorForFieldName(fd.name));
                String format = String.format("%%-%ds ", fd.length * 2);
                sb.append(String.format(format, fd.name));
                sb.append(ANSI.RESET);
            }
        }
        return sb.toString();
    }

    private String colorForFieldName(String fieldName) {
        return switch (fieldName) {
            case "signatur"                                                       -> ANSI.BRIGHT_WHITE ;
            case "verM", "verN"                                                   -> ANSI.BLUE         ;
            case "time", "date", "extended-timestamp"                             -> ANSI.MAGENTA      ;
            case "crc", "sizeCompr", "size", "Zip64 extended info"                -> ANSI.YELLOW       ;
            case "nameL", "name"                                                  -> ANSI.BRIGHT_YELLOW;
            case "xtraL", "xtra", "xlen"                                          -> ANSI.RED          ;
            case "thisDiskNum", "diskNumCentral", "totalNumDisks", "diskNumStart" -> ANSI.GREEN        ;
            case "numEntriesDisk", "numEntriesTotal", ""                          -> ANSI.BRIGHT_CYAN  ;
            case "sizeOfCentral", "offsetOfCentral"                               -> ANSI.BRIGHT_GREEN ;
            default -> "";
        };
    }

    private record Field(String name, int length) {
    }
}
