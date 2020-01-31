package deltix.luminary;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ConstantScope {
    @NotNull
    String getName();

    @NotNull
    String getFullName();

    @NotNull
    List<ConstantDef> getDefinedConstants();
}
