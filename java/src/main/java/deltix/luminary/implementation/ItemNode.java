package deltix.luminary.implementation;

import java.util.ArrayList;
import java.util.List;

abstract class ItemNode {
    private final String name;
    private final List<DecoratorValueNode> decorators = new ArrayList<>();
    private final List<String> comments;

    protected ItemNode(String name, List<String> comments) {
        this.name = name;
        this.comments = comments != null ? comments : new ArrayList<String>();
    }

    public String getName() {
        return name;
    }

    public List<DecoratorValueNode> getDecorators() {
        return decorators;
    }

    public List<String> getComments() {
        return comments;
    }
}
