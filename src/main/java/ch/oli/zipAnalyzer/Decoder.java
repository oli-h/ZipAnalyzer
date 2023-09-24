package ch.oli.zipAnalyzer;

import java.io.IOException;
import java.util.ArrayList;

public class Decoder {

    private MyInputStream mis;
    private ArrayList<FieldDescr> fields = new ArrayList<>();
    public boolean foundZip64Extra = false;

    public int signature;
    public byte[] raw;

    public void parse(MyInputStream mis) throws IOException {
        this.mis = mis;
        this.mis.resetBuffer();
        this.fields.clear();
        this.signature = (int) read4("signatur");
        this.internalParse();
        this.raw = mis.getAndResetBuffer();
        this.mis = null;
    }

    protected void internalParse() throws IOException {
        throw new RuntimeException("Upps");
    }

    protected byte[] read(String name, int len) throws IOException {
        if (len == 0) {
            return new byte[0];
        }
        var value = mis.read(len);
        fields.add(new FieldDescr(name, len));
        return value;
    }

    protected long read8(String name) throws IOException {
        var value = mis.read8();
        fields.add(new FieldDescr(name, 8));
        return value;
    }

    protected long read4(String name) throws IOException {
        var value = mis.read4();
        fields.add(new FieldDescr(name, 4));
        return value;
    }

    protected int read2(String name) throws IOException {
        var value = mis.read2();
        fields.add(new FieldDescr(name, 2));
        return value;
    }

    private static final String ANSI_BLACK            = "\u001b[30m";
    private static final String ANSI_RED              = "\u001b[31m";
    private static final String ANSI_GREEN            = "\u001b[32m";
    private static final String ANSI_YELLOW           = "\u001b[33m";
    private static final String ANSI_BLUE             = "\u001b[34m";
    private static final String ANSI_MAGENTA          = "\u001b[35m";
    private static final String ANSI_CYAN             = "\u001b[36m";
    private static final String ANSI_WHITE            = "\u001b[37m";
    private static final String ANSI_DEFAULT          = "\u001b[39m";
    private static final String ANSI_BRIGHT_BLACK     = "\u001b[90m";
    private static final String ANSI_BRIGHT_RED       = "\u001b[91m";
    private static final String ANSI_BRIGHT_GREEN     = "\u001b[92m";
    private static final String ANSI_BRIGHT_YELLOW    = "\u001b[93m";
    private static final String ANSI_BRIGHT_BLUE      = "\u001b[94m";
    private static final String ANSI_BRIGHT_MAGENTA   = "\u001b[95m";
    private static final String ANSI_BRIGHT_CYAN      = "\u001b[96m";
    private static final String ANSI_BRIGHT_WHITE     = "\u001b[97m";
    private static final String ANSI_UNDERLINE        = "\u001b[4m";
    private static final String ANSI_RESET            = "\u001b[0m";

    public String rawAsString() {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for (FieldDescr fd : fields) {
            sb.append(ANSI_UNDERLINE);
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
            sb.append(ANSI_RESET);
            sb.append(" ");
        }
        return sb.toString();
    }

    public String fieldNamesAsString() {
        StringBuilder sb = new StringBuilder();
        for (FieldDescr fd : fields) {
            if (fd.length > 0) {
                sb.append(colorForFieldName(fd.name));
                String format = String.format("%%-%ds ", fd.length * 2);
                sb.append(String.format(format, fd.name));
                sb.append(ANSI_RESET);
            }
        }
        return sb.toString();
    }

    private String colorForFieldName(String fieldName) {
        return switch (fieldName){
            case "signatur" -> ANSI_YELLOW;
            case "xtraLen", "xtra", "xlen" -> ANSI_BRIGHT_BLACK;
            case "verM", "verN" -> ANSI_BLUE;
            case "time", "date", "extended-timestamp" -> ANSI_MAGENTA;
            case "thisDiskNum", "diskNumCentral", "totalNumDisks", "diskNumStart" -> ANSI_GREEN;
            case "numEntriesDisk", "numEntriesTotal", "" -> ANSI_BRIGHT_CYAN;
            case "sizeOfCentral", "offsetOfCentral" -> ANSI_RED;
            case "nameLen", "name" -> ANSI_BRIGHT_YELLOW;
            default -> "";
        };
    }

    private record FieldDescr(String name, int length) {
    }
}
