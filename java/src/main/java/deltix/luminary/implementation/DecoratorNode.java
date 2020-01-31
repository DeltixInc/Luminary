package deltix.luminary.implementation;

import java.util.ArrayList;
import java.util.List;

public class DecoratorNode extends TypeContainerNode {
    private final String name;
    private final List<DecoratorValueNode> decorators;
    private final List<String> comments;
    private final List<DecoratorPropertyNode> properties;
    private final List<ConstantNode> definedConstants;

    public DecoratorNode(String name, List<String> comments) {
        super();
        this.name = name;
        this.comments = comments;
        this.decorators = new ArrayList<>();
        this.properties = new ArrayList<>();
        definedConstants = new ArrayList<>();
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

    public List<DecoratorPropertyNode> getProperties() {
        return properties;
    }

    public List<ConstantNode> getDefinedConstants() {
        return definedConstants;
    }
}
