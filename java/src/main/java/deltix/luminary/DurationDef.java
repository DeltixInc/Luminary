package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class DurationDef implements TypeDef<TypeDuration>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private DurationDef() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeDuration.INSTANCE, new LiteralDuration(LiteralDuration.MIN_VALUE_AS_STRING)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeDuration.INSTANCE, new LiteralDuration(LiteralDuration.MAX_VALUE_AS_STRING)));
    }

    public static final DurationDef INSTANCE = new DurationDef();

    @Override
    public String getName() {
        return "Duration";
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
    public TypeDuration getType() {
        return TypeDuration.INSTANCE;
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
