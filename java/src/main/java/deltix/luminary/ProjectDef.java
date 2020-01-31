package deltix.luminary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Describes the entire structure of the projects.
 */
public class ProjectDef {
    private final Location directory;
    private final Map<String, ProjectDef> references = new HashMap<>();
    private final SortedMap<String, NamespaceDef> namespaces = new TreeMap<>();
    private final String fileName;

    public ProjectDef(@Nullable Location directory, @Nullable String fileName) {
        this.directory = directory;
        this.fileName = fileName;
    }

    /**
     * Path to the project's root directory.
     *
     * @return Path to the project's root directory.
     */
    @Nullable
    public Location getDirectory() {
        return directory;
    }

    /**
     * Name of the project file.
     *
     * @return Name of the project file.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Map of other projects referenced by this project.
     *
     * @return List of referenced objects.
     */
    @NotNull
    public Map<String, ProjectDef> getReferences() {
        return references;
    }

    /**
     * List of namespaces defined within this project.
     *
     * @return List of namespaces.
     */
    @NotNull
    public SortedMap<String, NamespaceDef> getNamespaces() {
        return namespaces;
    }
}
