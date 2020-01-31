package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class TimeDef implements TypeDef<TypeTime>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private TimeDef() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeTime.INSTANCE, new LiteralTime(LiteralTime.MIN_VALUE_AS_STRING)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeTime.INSTANCE, new LiteralTime(LiteralTime.MAX_VALUE_AS_STRING)));
    }

    public static final TimeDef INSTANCE = new TimeDef();

    @Override
    public String getName() {
        return "Time";
    }

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public List<ConstantDef> getDefinedConstants() {
        return definedConstants;
    }

    @Override
    public TypeTime getType() {
        return TypeTime.INSTANCE;
    }

    @Override
    public FileDef getFile() {
        return null;
    }

    @Override
    public TypeScope getParent() {
        return null;
    }
}
