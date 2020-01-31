package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConstantDef extends ItemDef {
    private final Type type;
    private final ConstantScope owner;
    private Literal value;

    public ConstantDef(@NotNull ConstantScope owner, @NotNull String name, @NotNull Type type, @NotNull Literal value,
                       @Nullable List<String> comments) {
        super(name, owner.getFullName() + "." + name, comments);
        this.type = type;
        this.value = value;
        this.owner = owner;

        owner.getDefinedConstants().add(this);
    }

    public ConstantDef(@NotNull ConstantScope owner, @NotNull String name, @NotNull Type type, @NotNull Literal value) {
        this(owner, name, type, value, null);
    }

    @NotNull
    public Type getType() {
        return type;
    }

    @NotNull
    public ConstantScope getOwner() {
        return owner;
    }

    @NotNull
    public Literal getValue() {
        return value;
    }

    void setValue(@NotNull Literal value) {
        this.value = value;
    }
}
