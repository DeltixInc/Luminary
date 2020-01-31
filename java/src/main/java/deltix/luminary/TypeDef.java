package deltix.luminary;

public interface TypeDef<T extends Type> {
    T getType();

    FileDef getFile();

    TypeScope getParent();
}
