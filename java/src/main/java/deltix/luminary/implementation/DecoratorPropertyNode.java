package deltix.luminary.implementation;

import deltix.luminary.Literal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DecoratorPropertyNode extends PropertyNode {
    private Literal defaultValue = null;

    public DecoratorPropertyNode(String name, String type, List<String> comments) {
        super(name, type, comments);
    }

    @Nullable
    public Literal getDefault() {
        return defaultValue;
    }

    void setDefault(@NotNull Literal literal) {
        defaultValue = literal;
    }
}
