package deltix.luminary;

public class TypeClass extends TypeCustom {
    private final ClassDef definition;

    public TypeClass(ClassDef definition) {
        super(TypeKind.CLASS);
        this.definition = definition;
    }

    public ClassDef getDefinition() {
        return definition;
    }

    @Override
    public boolean equals(Type other) {
        return other instanceof TypeClass && equals((TypeClass) other);
    }

    public boolean equals(TypeClass other) {
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
