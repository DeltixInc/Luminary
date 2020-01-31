package deltix.luminary.implementation;

import java.util.ArrayList;
import java.util.List;

public class InterfaceNode extends TypeContainerNode {
    private final String name;
    private final List<DecoratorValueNode> decorators;
    private final List<String> comments;
    private final List<InterfacePropertyNode> properties;
    private final List<InterfaceMethodNode> methods;
    private final List<String> supertypes;

    protected InterfaceNode(String name, List<String> supertypes, List<String> comments) {
        super();
        this.name = name;
        this.comments = comments;
        this.decorators = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.supertypes = supertypes;
    }

    public List<InterfacePropertyNode> getProperties() {
        return properties;
    }

    public List<InterfaceMethodNode> getMethods() {
        return methods;
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

    public List<String> getSupertypes() {
        return supertypes;
    }
}
