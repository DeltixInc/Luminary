package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class UInt8Def implements TypeDef<TypeUInt8>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private UInt8Def() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeUInt8.INSTANCE, new LiteralUInt8((byte) 0x00)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeUInt8.INSTANCE, new LiteralUInt8((byte) 0xFF)));
    }

    public static final UInt8Def INSTANCE = new UInt8Def();

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
    public TypeUInt8 getType() {
        return TypeUInt8.INSTANCE;
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
