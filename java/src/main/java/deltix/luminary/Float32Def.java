package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class Float32Def implements TypeDef<TypeFloat32>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private Float32Def() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeFloat32.INSTANCE, new LiteralFloat32(Float.MIN_VALUE)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeFloat32.INSTANCE, new LiteralFloat32(Float.MAX_VALUE)));
        definedConstants.add(new ConstantDef(this, "NaN", TypeFloat32.INSTANCE, new LiteralFloat32(Float.NaN)));
        definedConstants.add(new ConstantDef(this, "POSITIVE_INFINITY", TypeFloat32.INSTANCE, new LiteralFloat32(Float.POSITIVE_INFINITY)));
        definedConstants.add(new ConstantDef(this, "NEGATIVE_INFINITY", TypeFloat32.INSTANCE, new LiteralFloat32(Float.NEGATIVE_INFINITY)));
    }

    public static final Float32Def INSTANCE = new Float32Def();

    @Override
    public String getName() {
        return "Float32";
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
    public TypeFloat32 getType() {
        return TypeFloat32.INSTANCE;
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

