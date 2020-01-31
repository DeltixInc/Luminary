package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class DecoratorUsage {
    private final List<DecoratorTarget> validOn = new ArrayList<>();
    private boolean repeatable = false;

    public List<DecoratorTarget> getValidOn() {
        return validOn;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public static DecoratorUsage byDefault() {
        final DecoratorUsage usage = new DecoratorUsage();
        usage.getValidOn().add(DecoratorTarget.ENUMERATION);
        usage.getValidOn().add(DecoratorTarget.ENUMERATION_MEMBER);
        usage.getValidOn().add(DecoratorTarget.INTERFACE);
        usage.getValidOn().add(DecoratorTarget.INTERFACE_PROPERTY);
        usage.getValidOn().add(DecoratorTarget.CLASS);
        usage.getValidOn().add(DecoratorTarget.CLASS_PROPERTY);
        usage.getValidOn().add(DecoratorTarget.DECORATOR);
        usage.getValidOn().add(DecoratorTarget.DECORATOR_PROPERTY);
        usage.getValidOn().add(DecoratorTarget.INTERFACE_METHOD);
        usage.getValidOn().add(DecoratorTarget.FORMAL_PARAMETER);
        return usage;
    }
}
