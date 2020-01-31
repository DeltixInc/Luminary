package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class Int16Def implements TypeDef<TypeInt16>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private Int16Def() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeInt16.INSTANCE, new LiteralInt16(Short.MIN_VALUE)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeInt16.INSTANCE, new LiteralInt16(Short.MAX_VALUE)));
    }

    public static final Int16Def INSTANCE = new Int16Def();

    @Override
    public String getName() {
        return "UInt16";
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
    public TypeInt16 getType() {
        return TypeInt16.INSTANCE;
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
