package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class TimestampDef implements TypeDef<TypeTimestamp>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private TimestampDef() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeTimestamp.INSTANCE, new LiteralTimestamp(LiteralTimestamp.MIN_VALUE_AS_STRING)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeTimestamp.INSTANCE, new LiteralTimestamp(LiteralTimestamp.MAX_VALUE_AS_STRING)));
    }

    public static final TimestampDef INSTANCE = new TimestampDef();

    @Override
    public String getName() {
        return "Timestamp";
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
    public TypeTimestamp getType() {
        return TypeTimestamp.INSTANCE;
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
