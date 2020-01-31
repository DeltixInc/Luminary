package deltix.luminary.implementation;

import java.util.ArrayList;
import java.util.List;

public class InterfaceMethodNode extends ItemNode {
    private final String returnType;
    private final List<FormalParameterNode> formalParameters;

    protected InterfaceMethodNode(String name, String returnType, List<String> comments) {
        super(name, comments);
        this.returnType = returnType;
        this.formalParameters = new ArrayList<>();
    }

    public String getReturnType() {
        return returnType;
    }

    public List<FormalParameterNode> getFormalParameters() {
        return formalParameters;
    }
}
