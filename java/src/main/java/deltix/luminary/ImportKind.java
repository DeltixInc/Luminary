package deltix.luminary;

public enum ImportKind {
    NAMESPACE(1),
    TYPE(2);

    private final int number;

    ImportKind(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
