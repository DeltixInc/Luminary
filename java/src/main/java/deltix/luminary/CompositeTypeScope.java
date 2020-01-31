package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeTypeScope<TProperty, TType extends Type> extends CompositeDef<TProperty> implements TypeScope, TypeDef<TType> {
    private final FileDef file;
    private final TypeScope parent;
    private List<ClassDef> definedClasses = new ArrayList<>();
    private List<EnumerationDef> definedEnumerations = new ArrayList<>();
    private List<InterfaceDef> definedInterfaces = new ArrayList<>();
    private List<DecoratorDef> definedDecorators = new ArrayList<>();

    protected CompositeTypeScope(@NotNull TypeScope parent, @NotNull String name, @Nullable List<String> comments) {
        super(name, parent instanceof FileDef ? name : ((ItemDef) parent).getFullName() + "." + name, comments);
        this.parent = parent;
        this.file = parent.getFile();
    }

    @Override
    @NotNull
    public abstract TType getType();

    @Override
    @NotNull
    public List<ClassDef> getDefinedClasses() {
        return definedClasses;
    }

    @Override
    @NotNull
    public List<EnumerationDef> getDefinedEnumerations() {
        return definedEnumerations;
    }

    @Override
    @NotNull
    public List<InterfaceDef> getDefinedInterfaces() {
        return definedInterfaces;
    }

    @Override
    @NotNull
    public List<DecoratorDef> getDefinedDecorators() {
        return definedDecorators;
    }

    @Override
    @NotNull
    public TypeScope getParent() {
        return parent;
    }

    @Override
    @NotNull
    public FileDef getFile() {
        return file;
    }
}
