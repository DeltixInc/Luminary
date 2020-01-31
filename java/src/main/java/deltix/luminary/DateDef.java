package deltix.luminary;

import java.util.ArrayList;
import java.util.List;

public class DateDef implements TypeDef<TypeDate>, ConstantScope {
    private List<ConstantDef> definedConstants;

    private DateDef() {
        definedConstants = new ArrayList<>();
        definedConstants.add(new ConstantDef(this, "MIN_VALUE", TypeDate.INSTANCE, new LiteralDate(LiteralDate.MIN_VALUE_AS_STRING)));
        definedConstants.add(new ConstantDef(this, "MAX_VALUE", TypeDate.INSTANCE, new LiteralDate(LiteralDate.MAX_VALUE_AS_STRING)));
    }

    public static final DateDef INSTANCE = new DateDef();

    @Override
    public String getName() {
        return "Date";
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
    public TypeDate getType() {
        return TypeDate.INSTANCE;
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
