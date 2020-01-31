package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeDef<TProperty> extends ItemDef {
    private final List<TProperty> properties = new ArrayList<>();

    protected CompositeDef(@NotNull String name, @NotNull String fullName, @Nullable List<String> comments) {
        super(name, fullName, comments);
    }

    /**
     * List of properties belonging to composite type.
     *
     * @return List of properties.
     */
    @NotNull
    public List<TProperty> getProperties() {
        return properties;
    }
}
