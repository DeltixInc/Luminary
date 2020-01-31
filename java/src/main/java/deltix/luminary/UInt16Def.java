package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class UInt16Def implements TypeDef<TypeUInt16>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private UInt16Def() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeUInt16.INSTANCE, new LiteralUInt16((short) 0x00_00)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeUInt16.INSTANCE, new LiteralUInt16((short) 0xFF_FF)));
    }

    public static final UInt16Def INSTANCE = new UInt16Def();

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
    public TypeUInt16 getType() {
        return TypeUInt16.INSTANCE;
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
