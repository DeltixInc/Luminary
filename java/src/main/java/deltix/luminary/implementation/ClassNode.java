package deltix.luminary.implementation;

import java.util.ArrayList;
import java.util.List;

public class ClassNode extends TypeContainerNode {
    private final String name;
    private final List<DecoratorValueNode> decorators = new ArrayList<>();
    private final List<String> comments;
    private final List<ClassPropertyNode> properties = new ArrayList<>();
    private final List<String> supertypes;
    private final List<ConstantNode> definedConstants = new ArrayList<>();
    private boolean isFinal;

    public ClassNode(String name, boolean isFinal, List<String> supertypes, List<String> comments) {
        super();
        this.name = name;
        this.isFinal = isFinal;
        this.comments = comments != null ? comments : new ArrayList<String>();
        this.supertypes = supertypes;
    }

    public List<ClassPropertyNode> getProperties() {
        return properties;
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

    public List<ConstantNode> getDefinedConstants() {
        return definedConstants;
    }

    public boolean isFinal() {
        return isFinal;
    }
}
