package deltix.luminary;

public class TypeDecorator extends TypeCustom {
    private final DecoratorDef definition;

    public TypeDecorator(DecoratorDef decoratorDef) {
        super(TypeKind.DECORATOR);
        definition = decoratorDef;
    }

    public DecoratorDef getDefinition() {
        return definition;
    }

    @Override
    public boolean equals(Type other) {
        return other instanceof TypeDecorator && equals((TypeDecorator) other);
    }

    public boolean equals(TypeDecorator other) {
        return other != null && other.definition.equals(definition);
    }

    @Override
    public String toString() {
        return definition.getFullName();
    }

    @Override
    public FileDef getFile() {
        return definition.getFile();
    }

    @Override
    public String getFullName() {
        return definition.getFullName();
    }

    @Override
    public String getName() {
        return definition.getName();
    }
}
