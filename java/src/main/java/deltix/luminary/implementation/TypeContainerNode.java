package deltix.luminary.implementation;

import java.util.ArrayList;
import java.util.List;

public class TypeContainerNode {
    private final List<EnumerationNode> definedEnumerations;
    private final List<DecoratorNode> definedDecorators;
    private final List<ClassNode> definedClasses;
    private final List<InterfaceNode> definedInterfaces;

    public TypeContainerNode() {
        definedEnumerations = new ArrayList<>();
        definedDecorators = new ArrayList<>();
        definedClasses = new ArrayList<>();
        definedInterfaces = new ArrayList<>();
    }

    public List<EnumerationNode> getDefinedEnumerations() {
        return definedEnumerations;
    }

    public List<ClassNode> getDefinedClasses() {
        return definedClasses;
    }

    public List<InterfaceNode> getDefinedInterfaces() {
        return definedInterfaces;
    }

    public List<DecoratorNode> getDefinedDecorators() {
        return definedDecorators;
    }
}
