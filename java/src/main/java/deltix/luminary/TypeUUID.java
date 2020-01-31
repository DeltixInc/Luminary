package deltix.luminary;

public class TypeUUID extends Type {
    private TypeUUID() {
        super(TypeKind.UUID);
    }

    public static final TypeUUID INSTANCE = new TypeUUID();

    @Override
    public String toString() {
        return "UUID";
    }
}
