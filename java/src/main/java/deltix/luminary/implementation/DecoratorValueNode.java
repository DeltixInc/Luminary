package deltix.luminary.implementation;

import deltix.luminary.NameValuePair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DecoratorValueNode {
    private final String name;
    private final List<NameValuePair> arguments;

    public DecoratorValueNode(String name, List<NameValuePair> arguments) {
        this.name = name;
        this.arguments = arguments != null ? arguments : new ArrayList<NameValuePair>();
    }

    public String getName() {
        return name;
    }

    @NotNull
    public List<NameValuePair> getArguments() {
        return arguments;
    }
}
