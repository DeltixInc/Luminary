package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class Float64Def implements TypeDef<TypeFloat64>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private Float64Def() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeFloat64.INSTANCE, new LiteralFloat64(Double.MIN_VALUE)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeFloat64.INSTANCE, new LiteralFloat64(Double.MAX_VALUE)));
        definedConstants.add(new ConstantDef(this, "NaN", TypeFloat64.INSTANCE, new LiteralFloat64(Double.NaN)));
        definedConstants.add(new ConstantDef(this, "POSITIVE_INFINITY", TypeFloat64.INSTANCE, new LiteralFloat64(Double.POSITIVE_INFINITY)));
        definedConstants.add(new ConstantDef(this, "NEGATIVE_INFINITY", TypeFloat64.INSTANCE, new LiteralFloat64(Double.NEGATIVE_INFINITY)));
    }

    public static final Float64Def INSTANCE = new Float64Def();

    @Override
    public String getName() {
        return "Float64";
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
    public TypeFloat64 getType() {
        return TypeFloat64.INSTANCE;
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
