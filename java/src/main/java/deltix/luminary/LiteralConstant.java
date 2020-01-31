package deltix.luminary;

public class LiteralConstant extends Literal {
    private final ConstantDef value;

    public LiteralConstant(ConstantDef value) {
        super(LiteralKind.CONSTANT);
        this.value = value;
    }

    public ConstantDef getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.getOwner().getName() + "." + value.getName();
    }
}
