package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TypeScope {
    @NotNull
    List<ClassDef> getDefinedClasses();

    @NotNull
    List<EnumerationDef> getDefinedEnumerations();

    @NotNull
    List<InterfaceDef> getDefinedInterfaces();

    @NotNull
    List<DecoratorDef> getDefinedDecorators();

    @Nullable
    TypeScope getParent();

    @NotNull
    FileDef getFile();
}
