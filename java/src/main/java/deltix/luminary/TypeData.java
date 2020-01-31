package deltix.luminary;

public class TypeData extends Type {
    private TypeData() {
        super(TypeKind.DATA);
    }

    public static final TypeData INSTANCE = new TypeData();

    @Override
    public String toString() {
        return "Data";
    }
}
