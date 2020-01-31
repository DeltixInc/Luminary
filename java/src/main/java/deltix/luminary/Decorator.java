package deltix.luminary;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Decorator {
    private final DecoratorDef definition;
    private final List<DecoratorPropertyValue> arguments;

    public Decorator(DecoratorDef definition, List<DecoratorPropertyValue> arguments) {
        this.definition = definition;
        this.arguments = arguments != null ? arguments : new ArrayList<DecoratorPropertyValue>();
    }

    @NotNull
    public DecoratorDef getDefinition() {
        return definition;
    }

    @NotNull
    public List<DecoratorPropertyValue> getArguments() {
        return arguments;
    }
}
