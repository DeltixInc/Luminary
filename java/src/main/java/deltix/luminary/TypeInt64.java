package deltix.luminary;

public class TypeInt64 extends Type {
    private TypeInt64() {
        super(TypeKind.INT64);
    }

    public static final TypeInt64 INSTANCE = new TypeInt64();

    @Override
    public String toString() {
        return "Int64";
    }
}
