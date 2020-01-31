package deltix.luminary.implementation;

import deltix.luminary.Literal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConstantNode extends ItemNode {
    private final String type;
    private final Literal value;

    ConstantNode(@NotNull String name, @NotNull String type, @NotNull Literal value, @Nullable List<String> comments) {
        super(name, comments);
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public Literal getValue() {
        return value;
    }
}
