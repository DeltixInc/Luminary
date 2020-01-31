package deltix.luminary;

import deltix.luminary.implementation.LiteralUnresolved;

public abstract class Literal {
    private final LiteralKind kind;

    protected Literal(LiteralKind kind) {
        this.kind = kind;
    }

    public LiteralKind getKind() {
        return kind;
    }

    public Object getValueAsObject() {
        switch (kind) {
            case TYPE:
                if (this instanceof LiteralUnresolved)
                    throw new IllegalStateException("Cannot get value of unresolved literal.");
                return ((LiteralType) this).getValue();

            case BOOLEAN:
                return ((LiteralBoolean) this).getValue();

            case TEXT:
                return ((LiteralText) this).getValue();

            case INT8:
                return ((LiteralInt8) this).getValue();

            case UINT8:
                return ((LiteralUInt8) this).getValue();

            case INT16:
                return ((LiteralInt16) this).getValue();

            case UINT16:
                return ((LiteralUInt16) this).getValue();

            case INT32:
                return ((LiteralInt32) this).getValue();

            case UINT32:
                return ((LiteralUInt32) this).getValue();

            case INT64:
                return ((LiteralInt64) this).getValue();

            case UINT64:
                return ((LiteralUInt64) this).getValue();

            case FLOAT32:
                return ((LiteralFloat32) this).getValue();

            case FLOAT64:
                return ((LiteralFloat64) this).getValue();

            case DECIMAL:
                return ((LiteralDecimal) this).getValue();

            case NULL:
                return ((LiteralNull) this).getValue();

            case ENUMERATION_VALUE:
                if (this instanceof LiteralUnresolved)
                    throw new IllegalStateException("Cannot get value of unresolved literal.");
                return ((LiteralEnumerationValue) this).getValue();

            case LIST:
                return ((LiteralList) this).getValue();

            default:
                throw new IllegalStateException("Literal kind is unknown.");
        }
    }
}
