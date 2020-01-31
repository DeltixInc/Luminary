package deltix.luminary;

public class NameValuePair {
    private final String name;
    private final Literal value;

    public NameValuePair(String name, Literal value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Literal getValue() {
        return value;
    }
}
