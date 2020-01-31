package deltix.luminary;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Describes the namespace within the project.
 */
public class NamespaceDef {
    private final String namespace;
    private final ProjectDef project;
    private final SortedMap<String, FileDef> files = new TreeMap<>();
    private final Map<String, TypeCustom> definedTypes = new HashMap<>();

    public NamespaceDef(@NotNull ProjectDef project, @NotNull String namespace) {
        this.project = project;
        this.namespace = namespace;

        project.getNamespaces().put(namespace, this);
    }

    /**
     * String namespace.
     *
     * @return String namespace.
     */
    @NotNull
    public String getNamespace() {
        return namespace;
    }

    /**
     * Project this namespace belongs to.
     *
     * @return Project this namespace belongs to.
     */
    @NotNull
    public ProjectDef getProject() {
        return project;
    }

    /**
     * List of files that contribute to this namespace.
     *
     * @return List of files that contribute to this namespace.
     */
    @NotNull
    public SortedMap<String, FileDef> getFiles() {
        return files;
    }

    /**
     * Map of all types defined within this namespace including nested types.
     *
     * @return Map of all types defined within this namespace.
     */
    @NotNull
    public Map<String, TypeCustom> getDefinedTypes() {
        return definedTypes;
    }
}
