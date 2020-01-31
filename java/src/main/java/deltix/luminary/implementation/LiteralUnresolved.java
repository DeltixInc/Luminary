package deltix.luminary.implementation;

import deltix.luminary.Literal;
import deltix.luminary.LiteralKind;

public class LiteralUnresolved extends Literal {
    private final String value;

    protected LiteralUnresolved(LiteralKind kind, String value) {
        super(kind);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
