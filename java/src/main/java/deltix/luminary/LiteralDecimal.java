package deltix.luminary;

import deltix.dfp.Decimal;
import deltix.dfp.Decimal64Utils;

public class LiteralDecimal extends Literal {
    private final long value;

    public LiteralDecimal(@Decimal long value) {
        super(LiteralKind.DECIMAL);
        this.value = value;
    }

    @Decimal
    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (Decimal64Utils.isNaN(value))
            return "Decimal.NaN";
        if (Decimal64Utils.isPositiveInfinity(value))
            return "Decimal.POSITIVE_INFINITY";
        if (Decimal64Utils.isNegativeInfinity(value))
            return "Decimal.NEGATIVE_INFINITY";
        return Decimal64Utils.toString(value) + "d64";
    }
}
