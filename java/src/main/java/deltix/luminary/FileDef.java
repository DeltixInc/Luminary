package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FileDef implements TypeScope {
    private final String fileName;
    private final NamespaceDef namespace;
    private final List<DecoratorDef> definedDecorators = new ArrayList<>();
    private final List<ClassDef> definedClasses = new ArrayList<>();
    private final List<InterfaceDef> definedInterfaces = new ArrayList<>();
    private final List<EnumerationDef> definedEnumerations = new ArrayList<>();
    private final List<ImportDef> imports = new ArrayList<>();
    private final List<NameValuePair> options = new ArrayList<>();

    public FileDef(@NotNull NamespaceDef namespace, @Nullable String fileName) {
        this.namespace = namespace;
        this.fileName = fileName;

        if (fileName != null)
            namespace.getFiles().put(fileName, this);
    }

    @Nullable
    public String getFileName() {
        return fileName;
    }

    @NotNull
    public NamespaceDef getNamespace() {
        return namespace;
    }

    @NotNull
    public List<DecoratorDef> getDefinedDecorators() {
        return definedDecorators;
    }

    @NotNull
    public List<ClassDef> getDefinedClasses() {
        return definedClasses;
    }

    @NotNull
    public List<InterfaceDef> getDefinedInterfaces() {
        return definedInterfaces;
    }

    @NotNull
    public List<EnumerationDef> getDefinedEnumerations() {
        return definedEnumerations;
    }

    @NotNull
    public FileDef getFile() {
        return this;
    }

    public TypeScope getParent() {
        return null;
    }

    @NotNull
    public List<ImportDef> getImports() {
        return imports;
    }

    @NotNull
    public List<NameValuePair> getOptions() {
        return options;
    }
}
