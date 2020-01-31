package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class Int32Def implements TypeDef<TypeInt32>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private Int32Def() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeInt32.INSTANCE, new LiteralInt32(Integer.MIN_VALUE)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeInt32.INSTANCE, new LiteralInt32(Integer.MAX_VALUE)));
    }

    public static final Int32Def INSTANCE = new Int32Def();

    @Override
    public String getName() {
        return "UInt32";
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
    public TypeInt32 getType() {
        return TypeInt32.INSTANCE;
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
