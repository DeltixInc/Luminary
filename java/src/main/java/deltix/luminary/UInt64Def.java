package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class UInt64Def implements TypeDef<TypeUInt64>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private UInt64Def() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeUInt64.INSTANCE, new LiteralUInt64(0x00_00_00_00_00_00_00_00L)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeUInt64.INSTANCE, new LiteralUInt64(0xFF_FF_FF_FF_FF_FF_FF_FFL)));
    }

    public static final UInt64Def INSTANCE = new UInt64Def();

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
    public TypeUInt64 getType() {
        return TypeUInt64.INSTANCE;
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
