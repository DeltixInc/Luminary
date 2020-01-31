package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InterfaceMethodDef extends ItemDef {
    private final Type returnType;
    private final List<FormalParameterDef> formalParameters;
    private final InterfaceDef owner;

    protected InterfaceMethodDef(@NotNull InterfaceDef owner, @NotNull String name, @Nullable Type returnType, @Nullable List<String> comments) {
        super(name, owner.getFullName() + "." + name, comments);
        this.returnType = returnType;
        this.owner = owner;
        this.formalParameters = new ArrayList<>();
    }

    @Nullable
    public Type getReturnType() {
        return returnType;
    }

    @NotNull
    public List<FormalParameterDef> getFormalParameters() {
        return formalParameters;
    }

    @NotNull
    public InterfaceDef getOwner() {
        return owner;
    }
}
