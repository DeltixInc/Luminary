package deltix.luminary;

public class TypeDuration extends Type {
    private TypeDuration() {
        super(TypeKind.DURATION);
    }

    public static final TypeDuration INSTANCE = new TypeDuration();

    @Override
    public String toString() {
        return "Duration";
    }
}
