package deltix.luminary.implementation;

import java.util.List;

public class InterfacePropertyNode extends PropertyNode {
    private final boolean isOverride;
    private final boolean isGettable;
    private final boolean isSettable;

    public InterfacePropertyNode(String name, String type, boolean isOverride, boolean isGettable, boolean isSettable,
                                 List<String> comments) {
        super(name, type, comments);
        this.isOverride = isOverride;
        this.isGettable = isGettable;
        this.isSettable = isSettable;
    }

    public boolean isOverride() {
        return isOverride;
    }

    public boolean isGettable() {
        return isGettable;
    }

    public boolean isSettable() {
        return isSettable;
    }
}
