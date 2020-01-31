package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImportTypeDef extends ImportDef {
    private TypeCustom target;
    private String alias;

    protected ImportTypeDef(@NotNull TypeCustom target, @Nullable String alias) {
        super(ImportKind.TYPE);
        this.target = target;
        this.alias = alias;
    }

    @NotNull
    public TypeCustom getTarget() {
        return target;
    }

    @Nullable
    public String getAlias() {
        return alias;
    }
}
