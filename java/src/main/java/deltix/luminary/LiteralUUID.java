package deltix.luminary;

import java.util.UUID;

public class LiteralUUID extends Literal {
    private final UUID value;
    private final String string;

    public LiteralUUID(UUID value) {
        super(LiteralKind.UUID);
        this.value = value;
        this.string = value.toString().toUpperCase();
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public String toString() {
        return string;
    }

    public static final UUID MIN_VALUE = new UUID(0L, 0L);

    public static final UUID MAX_VALUE = new UUID(-1L, -1L);
}
