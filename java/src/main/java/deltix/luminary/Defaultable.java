package deltix.luminary;

import org.jetbrains.annotations.Nullable;

public interface Defaultable {
    @Nullable
    Literal getDefault();
}
