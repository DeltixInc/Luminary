package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InterfacePropertyDef extends PropertyDef<InterfaceDef> {
    private final boolean isOverride;
    private final boolean isReadable;
    private final boolean isWritable;

    public InterfacePropertyDef(@NotNull InterfaceDef interfaceDef, @NotNull String name, @NotNull Type type,
                                boolean isOverride, boolean isReadable, boolean isWritable,
                                @Nullable List<String> comments) {
        super(interfaceDef, name, type, comments);
        this.isOverride = isOverride;
        this.isReadable = isReadable;
        this.isWritable = isWritable;

        interfaceDef.getProperties().add(this);
    }

    public InterfacePropertyDef(@NotNull InterfaceDef interfaceDef, @NotNull String name, @NotNull Type type,
                                boolean isOverride, boolean isReadable, boolean isWritable) {
        this(interfaceDef, name, type, isOverride, isReadable, isWritable, null);
    }

    public boolean isOverride() {
        return isOverride;
    }

    public boolean isReadable() {
        return isReadable;
    }

    public boolean isWritable() {
        return isWritable;
    }
}
