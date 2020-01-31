package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class Int8Def implements TypeDef<TypeInt8>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private Int8Def() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeInt8.INSTANCE, new LiteralInt8(Byte.MIN_VALUE)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeInt8.INSTANCE, new LiteralInt8(Byte.MAX_VALUE)));
    }

    public static final Int8Def INSTANCE = new Int8Def();

    @Override
    public String getName() {
        return "UInt8";
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
    public TypeInt8 getType() {
        return TypeInt8.INSTANCE;
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
