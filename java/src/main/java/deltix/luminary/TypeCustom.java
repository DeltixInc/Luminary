package deltix.luminary;

public abstract class TypeCustom extends Type {
    protected TypeCustom(TypeKind kind) {
        super(kind);
    }

    public abstract String getFullName();

    public abstract String getName();

    public abstract FileDef getFile();

    public NamespaceDef getNamespace() {
        return getFile().getNamespace();
    }

    public ProjectDef getProject() {
        return getNamespace().getProject();
    }
}
