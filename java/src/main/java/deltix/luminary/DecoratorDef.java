package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DecoratorDef extends CompositeTypeScope<DecoratorPropertyDef, TypeDecorator> implements ConstantAndTypeScope {
    private final TypeDecorator type;
    private final List<ConstantDef> definedConstants = new ArrayList<>();

    public DecoratorDef(@NotNull TypeScope parent, @NotNull String name, @Nullable List<String> comments) {
        super(parent, name, comments);
        this.type = new TypeDecorator(this);

        parent.getDefinedDecorators().add(this);
    }

    public DecoratorDef(@NotNull TypeScope parent, @NotNull String name) {
        this(parent, name, null);
    }

    @Override
    @NotNull
    public TypeDecorator getType() {
        return type;
    }

    @Override
    public List<ConstantDef> getDefinedConstants() {
        return definedConstants;
    }
}
