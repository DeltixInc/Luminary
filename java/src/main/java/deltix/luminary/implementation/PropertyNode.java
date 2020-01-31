package deltix.luminary.implementation;

import java.util.List;

public class PropertyNode extends ItemNode {
    private final String type;

    protected PropertyNode(String name, String type, List<String> comments) {
        super(name, comments);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
