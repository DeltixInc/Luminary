package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class Int64Def implements TypeDef<TypeInt64>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private Int64Def() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeInt64.INSTANCE, new LiteralInt64(Long.MIN_VALUE)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeInt64.INSTANCE, new LiteralInt64(Long.MAX_VALUE)));
    }

    public static final Int64Def INSTANCE = new Int64Def();

    @Override
    public String getName() {
        return "UInt64";
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
    public TypeInt64 getType() {
        return TypeInt64.INSTANCE;
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
