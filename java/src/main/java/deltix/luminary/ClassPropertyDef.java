package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClassPropertyDef extends PropertyDef<ClassDef> implements Defaultable {
    private final boolean isOverride;
    private final boolean isFinal;
    private Literal defaultValue;

    public ClassPropertyDef(@NotNull ClassDef classDef, @NotNull String name, @NotNull Type type,
                            @Nullable Literal defaultValue, boolean isOverride, boolean isFinal,
                            @Nullable List<String> comments) {
        super(classDef, name, type, comments);
        this.isOverride = isOverride;
        this.isFinal = isFinal;
        this.defaultValue = defaultValue;

        classDef.getProperties().add(this);
    }

    public ClassPropertyDef(@NotNull ClassDef classDef, @NotNull String name, @NotNull Type type,
                            @Nullable Literal defaultValue, boolean isOverride, boolean isFinal) {
        this(classDef, name, type, defaultValue, isOverride, isFinal, null);
    }

    /**
     * Default value of the class' property.
     *
     * @return Default value.
     */
    @Nullable
    @Override
    public Literal getDefault() {
        return defaultValue;
    }

    void setDefault(Literal defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Indicates whether this property overrides some property from the base class.
     *
     * @return <code>true</code> if this property overrides some property from the base class;
     * <code>false</code> - otherwise.
     */
    public boolean isOverride() {
        return isOverride;
    }

    /**
     * Indicates whether this property is final (cannot be overriden).
     *
     * @return {@code true} if this property is final; otherwise - {@code false}.
     */
    public boolean isFinal() {
        return isFinal;
    }
}
