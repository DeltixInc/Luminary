package deltix.luminary;

public class TypeFloat32 extends Type {
    private TypeFloat32() {
        super(TypeKind.FLOAT32);
    }

    public static final TypeFloat32 INSTANCE = new TypeFloat32();

    @Override
    public String toString() {
        return "Float32";
    }
}
