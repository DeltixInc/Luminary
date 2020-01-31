package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnumerationMemberDef extends ItemDef {
    private Literal rawValue;
    private final EnumerationDef enumerationDef;
    private LiteralInteger value;

    EnumerationMemberDef(@NotNull EnumerationDef enumerationDef, @NotNull String name, @Nullable List<String> comments) {
        super(name, enumerationDef.getFullName() + "." + name, comments);
        this.enumerationDef = enumerationDef;

        enumerationDef.getMembers().add(this);
    }

    EnumerationMemberDef(@NotNull EnumerationDef enumerationDef, @NotNull String name) {
        this(enumerationDef, name, (List<String>)null);
    }

    public EnumerationMemberDef(@NotNull EnumerationDef enumerationDef, @NotNull String name,
                                @NotNull LiteralInteger value, @Nullable List<String> comments) {
        super(name, enumerationDef.getFullName() + "." + name, comments);
        this.enumerationDef = enumerationDef;
        this.value = value;

        enumerationDef.getMembers().add(this);
    }

    public EnumerationMemberDef(@NotNull EnumerationDef enumerationDef, @NotNull String name,
                                @NotNull LiteralInteger value) {
        this(enumerationDef, name, value, null);
    }

    @NotNull
    public EnumerationDef getOwner() {
        return enumerationDef;
    }

    @NotNull
    Literal getRawValue() {
        return rawValue;
    }

    void setRawValue(Literal rawValue) {
        this.rawValue = rawValue;
    }

    @NotNull
    public LiteralInteger getValue() {
        return value;
    }

    void setValue(LiteralInteger value) {
        this.value = value;
    }
}
