package deltix.luminary;

public class TypeEnumeration extends TypeCustom {
    private final EnumerationDef definition;

    public TypeEnumeration(EnumerationDef definition) {
        super(TypeKind.ENUMERATION);
        this.definition = definition;
    }

    public EnumerationDef getDefinition() {
        return definition;
    }

    @Override
    public boolean equals(Type other) {
        return other instanceof TypeEnumeration && equals((TypeEnumeration) other);
    }

    public boolean equals(TypeEnumeration other) {
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
