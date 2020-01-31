package deltix.luminary.implementation;

import deltix.luminary.Literal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClassPropertyNode extends PropertyNode {
    private final boolean isOverride;
    private Literal defaultValue = null;
    private final boolean isFinal;

    public ClassPropertyNode(String name, String type, boolean isOverride, boolean isFinal, List<String> comments) {
        super(name, type, comments);
        this.isOverride = isOverride;
        this.isFinal = isFinal;
    }

    public boolean isOverride() {
        return isOverride;
    }

    @Nullable
    public Literal getDefault() {
        return defaultValue;
    }

    void setDefault(@NotNull Literal literal) {
        defaultValue = literal;
    }

    public boolean isFinal() {
        return isFinal;
    }
}
