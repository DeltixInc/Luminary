package deltix.luminary;

import org.jetbrains.annotations.NotNull;

public class ImportNamespaceDef extends ImportDef {
    private final NamespaceDef target;

    public ImportNamespaceDef(@NotNull NamespaceDef target) {
        super(ImportKind.NAMESPACE);
        this.target = target;
    }

    @NotNull
    public NamespaceDef getTarget() {
        return target;
    }
}
