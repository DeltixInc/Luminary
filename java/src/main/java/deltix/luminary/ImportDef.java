package deltix.luminary;

public abstract class ImportDef {
    private final ImportKind kind;

    protected ImportDef(ImportKind kind) {
        this.kind = kind;
    }

    public ImportKind getKind() {
        return kind;
    }

}
