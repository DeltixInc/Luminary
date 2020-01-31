package deltix.luminary;

public final class TypeType extends Type {
    private TypeType() {
        super(TypeKind.TYPE);
    }

    public static final TypeType INSTANCE = new TypeType();

    @Override
    public String toString() {
        return "Type";
    }
}
