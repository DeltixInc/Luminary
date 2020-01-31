package deltix.luminary.implementation;

import java.util.List;

public class FormalParameterNode extends PropertyNode {
    private final boolean isParameterArray;

    protected FormalParameterNode(String name, String type, boolean isParameterArray, List<String> comments) {
        super(name, type, comments);
        this.isParameterArray = isParameterArray;
    }

    public boolean isParameterArray() {
        return isParameterArray;
    }
}
