package deltix.luminary.implementation;

import java.util.ArrayList;
import java.util.List;

public abstract class ClassOrInterfaceNode extends TypeContainerNode {
    private final String name;
    private final List<DecoratorValueNode> decorators;
    private final List<String> comments;

    protected ClassOrInterfaceNode(String name, List<String> supertypes, List<String> comments) {
        super();
        this.name = name;
        this.comments = comments;
        this.decorators = new ArrayList<>();
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
