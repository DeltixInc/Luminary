package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ClassOrInterfaceDef<TProperty extends PropertyDef<? extends ClassOrInterfaceDef>, TType extends Type> extends CompositeTypeScope<TProperty, TType> implements TypeScope {
    private final List<InterfaceDef> superInterfaces = new ArrayList<>();

    public ClassOrInterfaceDef(@NotNull TypeScope parent, @NotNull String name, @Nullable List<String> comments) {
        super(parent, name, comments);
    }

    /**
     * List of super-interfaces inherited by this class or interface.
     *
     * @return List of super-interfaces.
     */
    @NotNull
    public List<InterfaceDef> getSuperInterfaces() {
        return superInterfaces;
    }
}
