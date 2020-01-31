package deltix.luminary;

public class LiteralText extends Literal {
    private String value;

    public LiteralText(String value) {
        super(LiteralKind.TEXT);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }
}
