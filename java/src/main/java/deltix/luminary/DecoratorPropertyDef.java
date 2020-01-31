package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DecoratorPropertyDef extends PropertyDef<DecoratorDef> implements Defaultable {
    private Literal defaultValue;

    public DecoratorPropertyDef(@NotNull DecoratorDef owner, @NotNull String name, @NotNull Type type,
                                @Nullable Literal defaultValue, @Nullable List<String> comments) {
        super(owner, name, type, comments);
        this.defaultValue = defaultValue;
        owner.getProperties().add(this);
    }

    public DecoratorPropertyDef(@NotNull DecoratorDef owner, @NotNull String name, @NotNull Type type,
                                @Nullable Literal defaultValue) {
        this(owner, name, type, defaultValue, null);
    }

    @Nullable
    @Override
    public Literal getDefault() {
        return defaultValue;
    }

    void setDefault(Literal defaultValue) {
        this.defaultValue = defaultValue;
    }
}
