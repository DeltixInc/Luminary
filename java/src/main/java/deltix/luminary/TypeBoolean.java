package deltix.luminary;

public class TypeBoolean extends Type {
    private TypeBoolean() {
        super(TypeKind.BOOLEAN);
    }

    public static final TypeBoolean INSTANCE = new TypeBoolean();

    @Override
    public String toString() {
        return "Boolean";
    }
}
