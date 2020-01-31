package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InterfaceDef extends ClassOrInterfaceDef<InterfacePropertyDef, TypeInterface> {
    private final TypeInterface type;
    private final List<InterfaceMethodDef> methods = new ArrayList<>();

    public InterfaceDef(@NotNull TypeScope parent, @NotNull String name, @Nullable List<String> comments) {
        super(parent, name, comments);
        this.type = new TypeInterface(this);

        parent.getDefinedInterfaces().add(this);
    }

    public InterfaceDef(@NotNull TypeScope parent, @NotNull String name) {
        this(parent, name, null);
    }

    @NotNull
    @Override
    public TypeInterface getType() {
        return type;
    }

    @NotNull
    public List<InterfaceMethodDef> getMethods() {
        return methods;
    }
}
