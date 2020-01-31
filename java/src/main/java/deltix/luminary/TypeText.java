package deltix.luminary;

public class TypeText extends Type {
    private TypeText() {
        super(TypeKind.TEXT);
    }

    public static final TypeText INSTANCE = new TypeText();

    @Override
    public String toString() {
        return "Text";
    }
}
