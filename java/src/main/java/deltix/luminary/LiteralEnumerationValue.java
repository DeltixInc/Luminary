package deltix.luminary;

public class LiteralEnumerationValue extends Literal {
    private final EnumerationMemberDef value;

    public LiteralEnumerationValue(EnumerationMemberDef value) {
        super(LiteralKind.ENUMERATION_VALUE);
        this.value = value;
    }

    public EnumerationMemberDef getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.getOwner().getName() + "." + value.getName();
    }
}
