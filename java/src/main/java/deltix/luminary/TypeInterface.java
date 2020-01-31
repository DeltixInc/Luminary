package deltix.luminary;

public class TypeInterface extends TypeCustom {
    private final InterfaceDef definition;

    public TypeInterface(InterfaceDef definition) {
        super(TypeKind.INTERFACE);
        this.definition = definition;
    }

    public InterfaceDef getDefinition() {
        return definition;
    }

    @Override
    public boolean equals(Type other) {
        return other instanceof TypeInterface && equals((TypeInterface) other);
    }

    public boolean equals(TypeInterface other) {
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
    public String getName() {
        return definition.getName();
    }

    @Override
    public String getFullName() {
        return definition.getFullName();
    }
}
