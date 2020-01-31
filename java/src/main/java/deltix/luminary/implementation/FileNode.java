package deltix.luminary.implementation;

import deltix.luminary.Location;
import deltix.luminary.NameValuePair;

import java.util.ArrayList;
import java.util.List;

public class FileNode extends TypeContainerNode {
    private final List<ImportNode> imports = new ArrayList<>();
    private final List<NameValuePair> options = new ArrayList<>();

    private Location location;
    private String namespace;

    public FileNode() {}

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<ImportNode> getImports() {
        return imports;
    }

    public List<NameValuePair> getOptions() {
        return options;
    }
}
