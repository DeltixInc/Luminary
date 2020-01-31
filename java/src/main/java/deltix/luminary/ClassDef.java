package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the definition of a class in Luminary.
 */
public class ClassDef extends ClassOrInterfaceDef<ClassPropertyDef, TypeClass> implements ConstantAndTypeScope {
    private final String fullName;
    private final boolean isFinal;
    private final TypeClass type;
    private ClassDef superClass = null;
    private final List<ConstantDef> definedConstants = new ArrayList<>();

    public ClassDef(@NotNull TypeScope parent, @NotNull String name, boolean isFinal, @Nullable List<String> comments) {
        super(parent, name, comments);
        this.type = new TypeClass(this);
        this.fullName = parent instanceof FileDef ? name : ((ItemDef) parent).getFullName() + "." + name;
        this.isFinal = isFinal;
        parent.getDefinedClasses().add(this);
    }

    public ClassDef(@NotNull TypeScope parent, @NotNull String name, boolean isFinal) {
        this(parent, name, isFinal, null);
    }

    /**
     * Type object associated with this class definition.
     *
     * @return Type object.
     */
    @NotNull
    @Override
    public TypeClass getType() {
        return type;
    }

    /**
     * Optional superclass of this class.
     *
     * @return Superclass definition.
     */
    @Nullable
    public ClassDef getSuperClass() {
        return superClass;
    }

    void setSuperClass(@Nullable ClassDef superClass) {
        this.superClass = superClass;
    }

    /**
     * Indicates whether this class if final (cannot be subclassed) or not.
     *
     * @return {@code true} if this class is final; otherwise - {@code false}.
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Full class name that is composed of outer class name (if there is any) and current class name.
     *
     * @return Full class name.
     */
    @Override
    public String getFullName() {
        return fullName;
    }

    /**
     * Collection of constants defined within this class.
     *
     * @return Collection of constants.
     */
    @Override
    public List<ConstantDef> getDefinedConstants() {
        return definedConstants;
    }
}
