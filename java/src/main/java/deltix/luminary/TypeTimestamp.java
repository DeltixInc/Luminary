package deltix.luminary;

public class TypeTimestamp extends Type {
    private TypeTimestamp() {
        super(TypeKind.TIMESTAMP);
    }

    public static final TypeTimestamp INSTANCE = new TypeTimestamp();

    @Override
    public String toString() {
        return "Timestamp";
    }
}
