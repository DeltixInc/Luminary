package deltix.luminary.implementation;

public class ImportNode {
    private final String typeName;
    private final String alias;
    private final String namespace;

    private ImportNode(String namespace, String typeName, String alias) {
        this.typeName = typeName;
        this.alias = alias;
        this.namespace = namespace;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getAlias() {
        return alias;
    }

    public String getNamespace() {
        return namespace;
    }

    public static ImportNode type(String namespace, String name) {
        return new ImportNode(namespace, name, null);
    }

    public static ImportNode typeWithAlias(String namespace, String name, String alias) {
        return new ImportNode(namespace, name, alias);
    }

    public static ImportNode everything(String namespace) {
        return new ImportNode(namespace, null, null);
    }
}
