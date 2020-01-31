package deltix.luminary;

import org.jetbrains.annotations.NotNull;

public class DecoratorPropertyValue {
    private final DecoratorPropertyDef definition;
    private final boolean isDefault;
    private Literal value;

    public DecoratorPropertyValue(@NotNull DecoratorPropertyDef definition, @NotNull Literal value, boolean isDefault) {
        this.definition = definition;
        this.value = value;
        this.isDefault = isDefault;
    }

    @NotNull
    public DecoratorPropertyDef getDefinition() {
        return definition;
    }

    @NotNull
    public Literal getValue() {
        return value;
    }

    public boolean isDefault() {
        return isDefault;
    }

    void setValue(Literal value) {
        this.value = value;
    }
}
