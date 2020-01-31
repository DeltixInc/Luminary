package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class UInt32Def implements TypeDef<TypeUInt32>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private UInt32Def() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeUInt32.INSTANCE, new LiteralUInt32(0x00_00_00_00)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeUInt32.INSTANCE, new LiteralUInt32(0xFF_FF_FF_FF)));
    }

    public static final UInt32Def INSTANCE = new UInt32Def();

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
    public TypeUInt32 getType() {
        return TypeUInt32.INSTANCE;
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
