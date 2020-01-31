package deltix.luminary.implementation;

import deltix.luminary.Literal;

import java.util.List;

public class EnumerationMemberNode extends ItemNode {
    private Literal value;

    public EnumerationMemberNode(String name, List<String> comments) {
        super(name, comments);
    }

    public Literal getValue() {
        return value;
    }

    void setValue(Literal value) {
        this.value = value;
    }
}
