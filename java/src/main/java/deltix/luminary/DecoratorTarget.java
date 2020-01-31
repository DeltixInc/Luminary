package deltix.luminary;

public enum DecoratorTarget {
    ENUMERATION(1),
    ENUMERATION_MEMBER(2),
    INTERFACE(3),
    INTERFACE_PROPERTY(4),
    CLASS(5),
    CLASS_PROPERTY(6),
    DECORATOR(7),
    DECORATOR_PROPERTY(8),
    INTERFACE_METHOD(9),
    FORMAL_PARAMETER(10);

    private final int number;

    DecoratorTarget(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
