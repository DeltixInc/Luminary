package deltix.luminary;

public class TypeDecimal extends Type {
    private TypeDecimal() {
        super(TypeKind.DECIMAL);
    }

    public static final TypeDecimal INSTANCE = new TypeDecimal();

    @Override
    public String toString() {
        return "Decimal";
    }
}
