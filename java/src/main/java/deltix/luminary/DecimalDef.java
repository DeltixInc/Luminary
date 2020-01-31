package deltix.luminary;

import deltix.dfp.Decimal64Utils;

import java.util.ArrayList;
import java.util.List;

public class DecimalDef implements TypeDef<TypeDecimal>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private DecimalDef() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeDecimal.INSTANCE, new LiteralDecimal(Decimal64Utils.NEGATIVE_INFINITY)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeDecimal.INSTANCE, new LiteralDecimal(Decimal64Utils.POSITIVE_INFINITY)));
        definedConstants.add(new ConstantDef(this, "NaN", TypeDecimal.INSTANCE, new LiteralDecimal(Decimal64Utils.NaN)));
        definedConstants.add(new ConstantDef(this, "POSITIVE_INFINITY", TypeDecimal.INSTANCE, new LiteralDecimal(Decimal64Utils.POSITIVE_INFINITY)));
        definedConstants.add(new ConstantDef(this, "NEGATIVE_INFINITY", TypeDecimal.INSTANCE, new LiteralDecimal(Decimal64Utils.NEGATIVE_INFINITY)));
        definedConstants.add(new ConstantDef(this, "NULL", TypeDecimal.INSTANCE, new LiteralDecimal(Decimal64Utils.NULL)));
    }

    public static final DecimalDef INSTANCE = new DecimalDef();

    @Override
    public String getName() {
        return "Decimal";
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
    public TypeDecimal getType() {
        return TypeDecimal.INSTANCE;
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
