package deltix.luminary.implementation;

import java.util.ArrayList;
import java.util.List;

public class EnumerationNode extends ItemNode {
    private final String underlyingType;
    private final List<EnumerationMemberNode> members = new ArrayList<>();

    public EnumerationNode(String name, String underlyingType, List<String> comments) {
        super(name, comments);
        this.underlyingType = underlyingType;
    }

    public String getUnderlyingType() {
        return underlyingType;
    }

    public List<EnumerationMemberNode> getMembers() {
        return members;
    }
}
