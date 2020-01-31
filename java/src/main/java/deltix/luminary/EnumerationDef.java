package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnumerationDef extends ItemDef implements TypeDef<TypeEnumeration> {
    private final IntegralType underlyingType;
    private final FileDef file;
    private final TypeScope parent;
    private final List<EnumerationMemberDef> members = new ArrayList<>();
    private final TypeEnumeration type;

    public EnumerationDef(@NotNull TypeScope parent, @NotNull String name, @NotNull IntegralType underlyingType,
                          @Nullable List<String> comments) {
        super(name, parent instanceof FileDef ? name : ((ItemDef) parent).getFullName() + "." + name, comments);
        this.underlyingType = underlyingType;
        this.parent = parent;
        this.file = parent.getFile();
        this.type = new TypeEnumeration(this);

        parent.getDefinedEnumerations().add(this);
    }

    public EnumerationDef(@NotNull TypeScope parent, @NotNull String name, @NotNull IntegralType underlyingType) {
        this(parent, name, underlyingType, null);
    }

    @NotNull
    public IntegralType getUnderlyingType() {
        return underlyingType;
    }

    @Override
    @NotNull
    public FileDef getFile() {
        return file;
    }

    @Override
    @NotNull
    public TypeScope getParent() {
        return parent;
    }

    @NotNull
    public List<EnumerationMemberDef> getMembers() {
        return members;
    }

    @Override
    @NotNull
    public TypeEnumeration getType() {
        return type;
    }
}
