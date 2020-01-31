package deltix.luminary;

import com.google.common.base.Strings;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class CodeWriter {
    private final StringBuilder buffer = new StringBuilder(0x1000);
    private final String indentString;
    private int indentLevel = 0;
    private boolean isNewLine = true;

    public CodeWriter() {
        indentString = "\t";
    }

    public CodeWriter(int tabSize) {
        if (tabSize < 2 || tabSize > 16)
            throw new IllegalArgumentException("Tabulation size should be within range 2..16");
        indentString = Strings.repeat(" ", tabSize);
    }

    public void indent() {
        indentLevel += 1;
    }

    public void dedent() {
        if (indentLevel == 0)
            throw new IllegalStateException("Cannot decrease indentation anymore.");
        indentLevel -= 1;
    }

    public void newLine() {
        buffer.append(System.lineSeparator());
        isNewLine = true;
    }

    public void write(String text) {
        if (text.contains("\r"))
            throw new IllegalArgumentException("Text should not contain '\\r' character.");

        final String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i += 1) {
            final String line = lines[i].replace("\n", "");

            if (line.length() > 0 && isNewLine)
                for (int j = 0; j < indentLevel; j += 1)
                    buffer.append(indentString);

            buffer.append(line.replace("\t", indentString));
            if (i + 1 < lines.length)
                buffer.append("\n");
            isNewLine = ((i + 1) < lines.length) || (line.length() == 0);
        }
    }

    public void writeLine(String text) {
        write(text);
        newLine();
    }

    public void saveTo(OutputStreamWriter stream) throws IOException {
        if (!isNewLine)
            newLine();
        stream.write(buffer.toString());
    }
}
