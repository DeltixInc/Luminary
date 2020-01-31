package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class UUIDDef implements TypeDef<TypeUUID>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private UUIDDef() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeUUID.INSTANCE, new LiteralUUID(LiteralUUID.MIN_VALUE)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeUUID.INSTANCE, new LiteralUUID(LiteralUUID.MAX_VALUE)));
    }

    public static final UUIDDef INSTANCE = new UUIDDef();

    @Override
    public String getName() {
        return "UUID";
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
    public TypeUUID getType() {
        return TypeUUID.INSTANCE;
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
