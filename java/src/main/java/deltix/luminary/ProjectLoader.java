package deltix.luminary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import deltix.luminary.implementation.*;
import deltix.luminary.parser.LuminaryLexer;
import deltix.luminary.parser.LuminaryParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Allows to load, parse and verify the Luminary projects. Keeps all loaded projects cached thus saving time when
 * some projects have to be loaded several times (e.g. were being referenced by other projects multiple times).
 */
public class ProjectLoader {
    private static final DecoratorDef DECORATOR_USAGE_DEF;
    private static final HashMap<String, Type> PREDEFINED_TYPES = new HashMap<String, Type>() {
        {
            put("Type", TypeType.INSTANCE);
            put("Boolean", TypeBoolean.INSTANCE);
            put("Int8", TypeInt8.INSTANCE);
            put("UInt8", TypeUInt8.INSTANCE);
            put("Int16", TypeInt16.INSTANCE);
            put("UInt16", TypeUInt16.INSTANCE);
            put("Int32", TypeInt32.INSTANCE);
            put("UInt32", TypeUInt32.INSTANCE);
            put("Int64", TypeInt64.INSTANCE);
            put("UInt64", TypeUInt64.INSTANCE);
            put("Float32", TypeFloat32.INSTANCE);
            put("Float64", TypeFloat64.INSTANCE);
            put("Decimal", TypeDecimal.INSTANCE);
            put("Text", TypeText.INSTANCE);
            put("Data", TypeData.INSTANCE);
            put("Timestamp", TypeTimestamp.INSTANCE);
            put("Duration", TypeDuration.INSTANCE);
            put("Time", TypeTime.INSTANCE);
            put("Date", TypeDate.INSTANCE);
            put("UUID", TypeUUID.INSTANCE);
        }
    };

    static {
        ProjectDef builtinProject = new ProjectDef(null, null);
        NamespaceDef builtinNamespace = new NamespaceDef(builtinProject, "");
        FileDef builtinFile = new FileDef(builtinNamespace, null);

        final EnumerationDef decoratorTargetDef = new EnumerationDef(builtinFile, "DecoratorTarget", IntegralType.INT32);
        new EnumerationMemberDef(decoratorTargetDef, "ENUMERATION", new LiteralInt32(1));
        new EnumerationMemberDef(decoratorTargetDef, "ENUMERATION_MEMBER", new LiteralInt32(2));
        new EnumerationMemberDef(decoratorTargetDef, "INTERFACE", new LiteralInt32(3));
        new EnumerationMemberDef(decoratorTargetDef, "INTERFACE_PROPERTY", new LiteralInt32(4));
        new EnumerationMemberDef(decoratorTargetDef, "CLASS", new LiteralInt32(5));
        new EnumerationMemberDef(decoratorTargetDef, "CLASS_PROPERTY", new LiteralInt32(6));
        new EnumerationMemberDef(decoratorTargetDef, "DECORATOR", new LiteralInt32(7));
        new EnumerationMemberDef(decoratorTargetDef, "DECORATOR_PROPERTY", new LiteralInt32(8));

        DECORATOR_USAGE_DEF = new DecoratorDef(builtinFile, "DecoratorUsage");

        final LiteralList defaultValidOn = new LiteralList();
        for (EnumerationMemberDef enumerationMemberDef : decoratorTargetDef.getMembers())
            defaultValidOn.getValue().add(new LiteralEnumerationValue(enumerationMemberDef));
        new DecoratorPropertyDef(DECORATOR_USAGE_DEF, "ValidOn", new TypeList(decoratorTargetDef.getType()), defaultValidOn);
        new DecoratorPropertyDef(DECORATOR_USAGE_DEF, "Repeatable", TypeBoolean.INSTANCE, LiteralBoolean.FALSE);

        builtinFile.getDefinedDecorators().add(DECORATOR_USAGE_DEF);

        PREDEFINED_TYPES.put(decoratorTargetDef.getName(), decoratorTargetDef.getType());
        PREDEFINED_TYPES.put(DECORATOR_USAGE_DEF.getName(), DECORATOR_USAGE_DEF.getType());
    }

    private final HashSet<Location> parsedProjects = new HashSet<>();
    private final HashMap<Location, ProjectDef> loadedProjects = new HashMap<>();
    private boolean verbose = false;

    /**
     * Returns true if verbose output enabled.
     *
     * @return true if verbose output enabled.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Set verbose output flag.
     *
     * @param verbose new value of the flag.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Initializes the new instance of @see ProjectLoader.
     */
    public ProjectLoader() {
    }

    /**
     * Loads the project with given relative or absolute path.
     *
     * @param location Location of the project file. Can be either {@link ZipArchiveLocation} or {@link FileSystemLocation}.
     * @return Loaded project.
     * @throws IOException When IO exception occurs while loading the project file or any of the source files.
     */
    public ProjectDef load(Location location) throws IOException {
        return load(location, 0);
    }

    /**
     * Loads the project with given relative or absolute path.
     *
     * @param projectFile Relative or absolute path to a project file.
     * @return Loaded project.
     * @throws IOException When IO exception occurs while loading the project file or any of the source files.
     */
    public ProjectDef load(String projectFile) throws IOException {
        return load(new FileSystemLocation("."), projectFile, 0);
    }

    private ProjectDef tryLoad(Location directory, String relativePath, int level) throws IOException {
        try {
            final Location location = directory.resolve(relativePath);
            if (location != null)
                return load(location, 0);
        } catch (IOException ignored) {
        }
        return null;
    }

    private ProjectDef load(Location current, String referenceString, int level) throws IOException {
        // 1. Check whether the provided reference is absolute file location.

        final Path reference = Paths.get(referenceString);
        if (Files.exists(reference))
            return load(new FileSystemLocation(reference));

        // 2. Provided reference is NOT an absolute file location.

        // 2.1. Check against current location.
        ProjectDef projectDef = tryLoad(current, referenceString, level);
        if (projectDef != null)
            return projectDef;

        // 2.2. Check against all search path entries in turn.

        final String searchPath = getSearchPath();
        if (searchPath != null) {
            for (String searchEntryString : searchPath.split(System.getProperty("path.separator"))) {
                final Path searchEntry = Paths.get(searchEntryString);
                if (Files.notExists(searchEntry))
                    continue;
                if (Files.isDirectory(searchEntry)) {
                    // 2.2.1. Search entry is a directory.
                    projectDef = tryLoad(new FileSystemLocation(searchEntry), referenceString, level);
                    if (projectDef != null)
                        return projectDef;
                } else {
                    // 2.2.2. Search entry is NOT a directory. Check whether it is a ZIP archive.
                    try {
                        projectDef = tryLoad(new ZipArchiveLocation(searchEntry.toString()), referenceString, level);
                        if (projectDef != null)
                            return projectDef;
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        throw new FileNotFoundException("Reference '" + referenceString + "' cannot be located.");
    }

    private ProjectDef load(Location location, int level) throws IOException {
        if (location == null)
            throw new IllegalStateException("Cannot find the project file.");

        // Project has been loaded (and thus protocols imported by it).
        ProjectDef projectDef = loadedProjects.get(location);
        if (projectDef != null)
            return projectDef;

        // First, parse the project file and attempt to load referenced projects recursively.
        ProjectFile projectFile = loadProjectFile(location, level);
        Map<String, ProjectDef> references = new HashMap<>(projectFile.References != null ? projectFile.References.length : 0);
        if (projectFile.References != null)
            for (String reference : projectFile.References)
                references.put(reference, load(location.getParent(), reference, level + 1));

        // When all dependencies have been loaded correctly, analyze the protocol itself.
        projectDef = loadProject(projectFile, references, level + 1);
        loadedProjects.put(location, projectDef);
        return projectDef;
    }

    private ProjectFile loadProjectFile(Location location, int level) throws IOException {
        if (verbose) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < level; i += 1)
                builder.append("    ");
            builder.append("Loading project file '").append(location).append("'...");
            System.out.println(builder.toString());
        }

        if (parsedProjects.contains(location))
            throw new IllegalStateException("Project '" + location + "' is already parsed but not analyzed. This indicates a cycle of imports.");

        final ProjectFile projectFile;
        try {
            projectFile = parseProjectFile(location);
        } catch (JsonParseException exception) {
            throw new IllegalStateException(location.toString() + ": Failed to parse JSON.", exception);
        }

        projectFile.Path = location;
        parsedProjects.add(location);
        return projectFile;
    }

    private ProjectDef loadProject(ProjectFile projectFile, Map<String, ProjectDef> references, int level) throws IOException {
        final ProjectDef projectDef = new ProjectDef(projectFile.Path.getParent(), projectFile.Path.getFileName());
        projectDef.getReferences().putAll(references);

        final HashMap<Location, FileNode> files = new HashMap<>();
        final Location projectDir = projectFile.Path.getParent();
        for (Pair<Location, Location> source : enumerateSources(projectFile)) {
            final Location sourceLocation = source.a;
            final Location sourceFile = source.b;

            final String relativePath = sourceLocation.relativize(sourceFile.getParent());

            if (verbose) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < level; i += 1)
                    builder.append("    ");
                builder.append("Loading source file '").append(projectDir.relativize(sourceFile)).append("'...");
                System.out.println(builder.toString());
            }

            final FileNode fileNode;
            try {
                fileNode = parseSourceFile(sourceFile);
            } catch (Exception exception) {
                throw new IllegalStateException(sourceFile.toString() + ": failed to parse Luminary.", exception);
            }

            // Make sure that source file's relative path matches with its namespace.
            final String namespace = relativePath.replace('\\', '.').replace('/', '.');
            if (!namespace.equals(fileNode.getNamespace()))
                throw new IllegalStateException("File '" + relativePath + "' has invalid namespace: '" + namespace + "' is expected.");

            files.put(sourceFile, fileNode);
        }

        // Accumulate types defined within the project.

        for (Map.Entry<Location, FileNode> file : files.entrySet()) {
            NamespaceDef namespaceDef = projectDef.getNamespaces().get(file.getValue().getNamespace());
            if (namespaceDef == null) {
                namespaceDef = new NamespaceDef(projectDef, file.getValue().getNamespace());
                projectDef.getNamespaces().put(namespaceDef.getNamespace(), namespaceDef);
            }

            final FileDef fileDef = new FileDef(namespaceDef, file.getKey().getFileName());
            fileDef.getOptions().addAll(file.getValue().getOptions());

            collectTypes(fileDef, file.getValue(), fileDef.getNamespace());
        }

        // Resolve imports.

        for (Map.Entry<Location, FileNode> file : files.entrySet()) {
            final NamespaceDef namespaceDef = projectDef.getNamespaces().get(file.getValue().getNamespace());
            final FileDef fileDef = namespaceDef.getFiles().get(file.getKey().getFileName());

            for (int i = 0; i < file.getValue().getImports().size(); i += 1) {
                final ImportNode importNode = file.getValue().getImports().get(i);

                final List<NamespaceDef> namespaceCandidates = new ArrayList<>();
                final NamespaceDef importedNamespaceDef = projectDef.getNamespaces().get(importNode.getNamespace());
                if (importedNamespaceDef != null)
                    namespaceCandidates.add(importedNamespaceDef);

                for (ProjectDef referencedProjectDef : projectDef.getReferences().values()) {
                    final NamespaceDef def = referencedProjectDef.getNamespaces().get(importNode.getNamespace());
                    if (def != null)
                        namespaceCandidates.add(def);
                }

                if (importNode.getTypeName() == null) {
                    if (namespaceCandidates.size() == 0)
                        throw new IllegalStateException(fileDef.getFileName() + ": namespace '" + importNode.getNamespace()
                            + "' is not defined within this project or referenced projects.");
                    if (namespaceCandidates.size() > 1)
                        throw new IllegalStateException(fileDef.getFileName() + ": namespace '" + importNode.getNamespace()
                            + "' is defined multiple times.");

                    fileDef.getImports().add(new ImportNamespaceDef(namespaceCandidates.get(0)));
                    continue;
                }

                final List<TypeCustom> typeCandidates = new ArrayList<>();
                for (NamespaceDef candidate : namespaceCandidates) {
                    final TypeCustom type = candidate.getDefinedTypes().get(importNode.getTypeName());
                    if (type != null)
                        typeCandidates.add(type);
                }

                if (typeCandidates.size() == 0)
                    throw new IllegalStateException(fileDef.getFileName() + ": namespace '" + importNode.getNamespace()
                        + "." + importNode.getTypeName() + "' is not defined within this project or referenced projects.");
                if (typeCandidates.size() > 1)
                    throw new IllegalStateException(fileDef.getFileName() + ": namespace '" + importNode.getNamespace()
                        + "." + importNode.getTypeName() + "' is defined multiple times.");

                if (importNode.getAlias() == null) {
                    for (int j = i + 1; j < file.getValue().getImports().size(); j += 1)
                        if (importNode.getTypeName().equals(file.getValue().getImports().get(j).getTypeName()) && file.getValue().getImports().get(j).getAlias() == null)
                            throw new IllegalStateException(fileDef.getFileName() + ": type '" + importNode.getTypeName() + "' imported more than once.");
                } else {
                    for (int j = i + 1; j < file.getValue().getImports().size(); j += 1)
                        if (importNode.getAlias().equals(file.getValue().getImports().get(i).getAlias()))
                            throw new IllegalStateException(fileDef.getFileName() + ": alias '" + importNode.getAlias() + "' defined more than once.");
                }

                fileDef.getImports().add(new ImportTypeDef(typeCandidates.get(0), importNode.getAlias()));
            }
        }

        // Collect all the constants defined within a project.

        for (Map.Entry<Location, FileNode> file : files.entrySet()) {
            final NamespaceDef namespaceDef = projectDef.getNamespaces().get(file.getValue().getNamespace());
            final FileDef fileDef = namespaceDef.getFiles().get(file.getKey().getFileName());

            collectConstants(fileDef, file.getValue(), fileDef.getNamespace());
        }

        // Check types of all constants.

        for (NamespaceDef namespaceDef : projectDef.getNamespaces().values())
            for (FileDef fileDef : namespaceDef.getFiles().values())
                checkConstantValues(fileDef);

        // Check enumeration members.

        for (NamespaceDef namespaceDef : projectDef.getNamespaces().values())
            for (FileDef fileDef : namespaceDef.getFiles().values())
                checkEnumerationMembers(fileDef);

        // Check custom type definitions.

        for (Map.Entry<Location, FileNode> file : files.entrySet()) {
            final NamespaceDef namespaceDef = projectDef.getNamespaces().get(file.getValue().getNamespace());
            final FileDef fileDef = namespaceDef.getFiles().get(file.getKey().getFileName());

            buildTypeDefinitions(fileDef, file.getValue());
        }

        // Check interface method definitions.

        for (Map.Entry<Location, FileNode> file : files.entrySet()) {
            final NamespaceDef namespaceDef = projectDef.getNamespaces().get(file.getValue().getNamespace());
            final FileDef fileDef = namespaceDef.getFiles().get(file.getKey().getFileName());

            buildInterfaceMethods(fileDef, file.getValue());
        }

        // Check decorators.

        for (Map.Entry<Location, FileNode> file : files.entrySet()) {
            final NamespaceDef namespaceDef = projectDef.getNamespaces().get(file.getValue().getNamespace());
            checkDecorators(namespaceDef.getFiles().get(file.getKey().getFileName()), file.getValue());
        }

        // Resolve all unresolved literals.

        for (NamespaceDef namespaceDef : projectDef.getNamespaces().values())
            for (FileDef fileDef : namespaceDef.getFiles().values())
                resolveLiterals(fileDef);

        // TODO: Check that there is no cycles within inheritance graph.

        for (NamespaceDef namespaceDef : projectDef.getNamespaces().values()) {
            for (FileDef fileDef : namespaceDef.getFiles().values()) {
                for (ClassDef classDef : fileDef.getDefinedClasses())
                    checkInheritance(classDef);

                for (InterfaceDef interfaceDef : fileDef.getDefinedInterfaces())
                    checkInheritance(interfaceDef);
            }
        }

        return projectDef;
    }

    private void resolveDecoratorValues(TypeScope scope, ItemDef itemDef) {
        for (Decorator decorator : itemDef.getDecorators())
            for (DecoratorPropertyValue decoratorPropertyValue : decorator.getArguments())
                resolveLiteral(scope, decoratorPropertyValue.getValue());
    }

    private void resolveConstantValues(ConstantAndTypeScope scope) {
        for (ConstantDef constantDef : scope.getDefinedConstants()) {
            constantDef.setValue(resolveLiteral(scope, constantDef.getValue()));
            resolveDecoratorValues(scope, constantDef);
        }
    }

    private void resolveLiterals(TypeScope scope) {
        for (ClassDef classDef : scope.getDefinedClasses()) {
            resolveDecoratorValues(classDef, classDef);
            resolveConstantValues(classDef);

            for (ClassPropertyDef propertyDef : classDef.getProperties()) {
                if (propertyDef.getDefault() != null)
                    propertyDef.setDefault(resolveLiteral(classDef, propertyDef.getDefault()));
                resolveDecoratorValues(classDef, propertyDef);
            }

            resolveLiterals(classDef);
        }

        for (DecoratorDef decoratorDef : scope.getDefinedDecorators()) {
            resolveDecoratorValues(decoratorDef, decoratorDef);
            resolveConstantValues(decoratorDef);

            for (DecoratorPropertyDef propertyDef : decoratorDef.getProperties()) {
                if (propertyDef.getDefault() != null)
                    propertyDef.setDefault(resolveLiteral(decoratorDef, propertyDef.getDefault()));
                resolveDecoratorValues(decoratorDef, propertyDef);
            }

            resolveLiterals(decoratorDef);
        }

        for (InterfaceDef interfaceDef : scope.getDefinedInterfaces()) {
            resolveDecoratorValues(interfaceDef, interfaceDef);
            resolveLiterals(interfaceDef);

            for (InterfacePropertyDef propertyDef : interfaceDef.getProperties())
                resolveDecoratorValues(interfaceDef, propertyDef);
        }

        for (EnumerationDef enumerationDef : scope.getDefinedEnumerations()) {
            resolveDecoratorValues(scope, enumerationDef);

            for (EnumerationMemberDef memberDef : enumerationDef.getMembers()) {
                memberDef.setRawValue(resolveLiteral(scope, memberDef.getRawValue()));
                resolveDecoratorValues(scope, memberDef);
            }
        }
    }

    private void collectTypes(TypeScope parent, TypeContainerNode typeContainerNode, NamespaceDef namespaceDef) {
        for (EnumerationNode enumerationNode : typeContainerNode.getDefinedEnumerations()) {
            checkForTypeConflicts(parent, enumerationNode.getName());

            final EnumerationDef enumerationDef = new EnumerationDef(parent, enumerationNode.getName(), extractUnderlyingType(enumerationNode.getUnderlyingType()), enumerationNode.getComments());
            for (EnumerationMemberNode enumerationMemberNode : enumerationNode.getMembers()) {
                EnumerationMemberDef memberDef = new EnumerationMemberDef(enumerationDef, enumerationMemberNode.getName(), enumerationMemberNode.getComments());
                memberDef.setRawValue(enumerationMemberNode.getValue());
            }

            collectType(namespaceDef, enumerationDef.getFullName(), enumerationDef.getType());
        }
        for (DecoratorNode decoratorNode : typeContainerNode.getDefinedDecorators()) {
            checkForTypeConflicts(parent, decoratorNode.getName());

            final DecoratorDef decoratorDef = new DecoratorDef(parent, decoratorNode.getName(), decoratorNode.getComments());

            collectType(namespaceDef, decoratorDef.getFullName(), decoratorDef.getType());

            collectTypes(decoratorDef, decoratorNode, namespaceDef);
        }
        for (ClassNode classNode : typeContainerNode.getDefinedClasses()) {
            checkForTypeConflicts(parent, classNode.getName());

            final ClassDef classDef = new ClassDef(parent, classNode.getName(), classNode.isFinal());
            if (classNode.getComments() != null)
                classDef.getComments().addAll(classNode.getComments());

            collectType(namespaceDef, classDef.getFullName(), classDef.getType());

            collectTypes(classDef, classNode, namespaceDef);
        }
        for (InterfaceNode interfaceNode : typeContainerNode.getDefinedInterfaces()) {
            checkForTypeConflicts(parent, interfaceNode.getName());

            final InterfaceDef interfaceDef = new InterfaceDef(parent, interfaceNode.getName(), interfaceNode.getComments());

            collectType(namespaceDef, interfaceDef.getFullName(), interfaceDef.getType());

            collectTypes(interfaceDef, interfaceNode, namespaceDef);
        }
    }

    private void collectConstants(TypeScope parent, TypeContainerNode typeContainerNode, NamespaceDef namespaceDef) {
        for (int i = 0; i < parent.getDefinedDecorators().size(); i += 1) {
            final DecoratorDef decoratorDef = parent.getDefinedDecorators().get(i);
            final DecoratorNode decoratorNode = typeContainerNode.getDefinedDecorators().get(i);

            for (ConstantNode constantNode : decoratorNode.getDefinedConstants())
                checkConstant(constantNode, decoratorDef);

            collectConstants(decoratorDef, decoratorNode, namespaceDef);
        }

        for (int i = 0; i < parent.getDefinedClasses().size(); i += 1) {
            final ClassDef decoratorDef = parent.getDefinedClasses().get(i);
            final ClassNode decoratorNode = typeContainerNode.getDefinedClasses().get(i);

            for (ConstantNode constantNode : decoratorNode.getDefinedConstants())
                checkConstant(constantNode, decoratorDef);

            collectConstants(decoratorDef, decoratorNode, namespaceDef);
        }

        for (int i = 0; i < parent.getDefinedInterfaces().size(); i += 1) {
            final InterfaceDef interfaceDef = parent.getDefinedInterfaces().get(i);
            final InterfaceNode interfaceNode = typeContainerNode.getDefinedInterfaces().get(i);

            collectConstants(interfaceDef, interfaceNode, namespaceDef);
        }
    }

    private void doCheckConstantValues(ConstantAndTypeScope scope) {
        for (ConstantDef constantDef : scope.getDefinedConstants()) {
            final Literal rawValue = resolveLiteral(scope, constantDef.getValue());

            final Literal value = checkLiteral(rawValue, constantDef.getType());
            if (value == null)
                throw new IllegalStateException(scope.getFile().getFileName() + ": constant '" + constantDef.getFullName() + "' of type '" + constantDef.getType() + "' cannot be initialized with value '" + constantDef.getValue() + "'.");
            constantDef.setValue(value);
        }
    }

    private void checkConstantValues(TypeScope scopeDef) {
        for (DecoratorDef decoratorDef : scopeDef.getDefinedDecorators())
            doCheckConstantValues(decoratorDef);
        for (ClassDef classDef : scopeDef.getDefinedClasses())
            doCheckConstantValues(classDef);
    }

    private void checkEnumerationMembers(TypeScope scope) {
        for (EnumerationDef enumerationDef : scope.getDefinedEnumerations())
            for (EnumerationMemberDef memberDef : enumerationDef.getMembers()) {
                Literal rawValue = resolveLiteral(scope, memberDef.getRawValue());
                memberDef.setRawValue(rawValue);

                while (rawValue instanceof LiteralConstant)
                    rawValue = ((LiteralConstant) rawValue).getValue().getValue();
                if (!(rawValue instanceof LiteralInteger))
                    throw new IllegalStateException(scope.getFile().getFileName() + ": enumeration member '" + memberDef.getFullName() + "' cannot be initialized with value '" + rawValue + "'.");
                memberDef.setValue(((LiteralInteger) rawValue).castTo(enumerationDef.getUnderlyingType()));
            }
        for (ClassDef classDef : scope.getDefinedClasses())
            checkEnumerationMembers(classDef);
        for (InterfaceDef interfaceDef : scope.getDefinedInterfaces())
            checkEnumerationMembers(interfaceDef);
        for (DecoratorDef decoratorDef : scope.getDefinedDecorators())
            checkEnumerationMembers(decoratorDef);
    }

    private void buildTypeDefinitions(TypeScope scopeDef, TypeContainerNode scopeNode) {
        for (int i = 0; i < scopeNode.getDefinedDecorators().size(); i += 1)
            checkDecoratorDefinition(scopeDef.getDefinedDecorators().get(i), scopeNode.getDefinedDecorators().get(i));
        for (int i = 0; i < scopeNode.getDefinedClasses().size(); i += 1)
            checkClassDefinition(scopeDef.getDefinedClasses().get(i), scopeNode.getDefinedClasses().get(i));
        for (int i = 0; i < scopeNode.getDefinedInterfaces().size(); i += 1)
            checkInterfaceDefinition(scopeDef.getDefinedInterfaces().get(i), scopeNode.getDefinedInterfaces().get(i));
    }

    private void doBuildInterfaceMethods(InterfaceDef interfaceDef, InterfaceNode interfaceNode) {
        for (InterfaceMethodNode interfaceMethodNode : interfaceNode.getMethods()) {
            Type returnType = null;
            if (interfaceMethodNode.getReturnType() != null)
                returnType = resolveType(interfaceDef, interfaceMethodNode.getReturnType());

            final InterfaceMethodDef interfaceMethodDef = new InterfaceMethodDef(interfaceDef, interfaceMethodNode.getName(), returnType, interfaceNode.getComments());

            for (FormalParameterNode formalParameterNode : interfaceMethodNode.getFormalParameters()) {
                final Type type = resolveType(interfaceDef, formalParameterNode.getType());
                final FormalParameterDef formalParameterDef = new FormalParameterDef(interfaceMethodDef,
                    formalParameterNode.getName(), type, formalParameterNode.isParameterArray(),
                    formalParameterNode.getComments());

                FormalParameterDef duplicate = null;
                for (FormalParameterDef parameterDef : interfaceMethodDef.getFormalParameters()) {
                    if (parameterDef.getName().equals(formalParameterDef.getName())) {
                        duplicate = parameterDef;
                        break;
                    }
                }

                if (duplicate != null)
                    throw new IllegalStateException(interfaceDef.getFile().getFileName() + ": interface method '" +
                        interfaceMethodDef.getFullName() + "' has two formal parameters with the same name '" +
                        formalParameterDef.getName() + "'.");

                interfaceMethodDef.getFormalParameters().add(formalParameterDef);
            }
            interfaceDef.getMethods().add(interfaceMethodDef);
        }
    }

    private void buildInterfaceMethods(TypeScope scopeDef, TypeContainerNode scopeNode) {
        for (int i = 0; i < scopeNode.getDefinedDecorators().size(); i += 1)
            buildInterfaceMethods(scopeDef.getDefinedDecorators().get(i), scopeNode.getDefinedDecorators().get(i));
        for (int i = 0; i < scopeNode.getDefinedClasses().size(); i += 1)
            buildInterfaceMethods(scopeDef.getDefinedClasses().get(i), scopeNode.getDefinedClasses().get(i));
        for (int i = 0; i < scopeNode.getDefinedInterfaces().size(); i += 1) {
            doBuildInterfaceMethods(scopeDef.getDefinedInterfaces().get(i), scopeNode.getDefinedInterfaces().get(i));
            buildInterfaceMethods(scopeDef.getDefinedInterfaces().get(i), scopeNode.getDefinedInterfaces().get(i));
        }
    }

    private void checkDecoratorPropertyType(Type type) {
        // TODO: Implement this.
    }

    private void checkDecoratorDefinition(DecoratorDef decoratorDef, DecoratorNode decoratorNode) {
        for (DecoratorPropertyNode propertyNode : decoratorNode.getProperties()) {
            final Type propertyType = resolveType(decoratorDef, propertyNode.getType());
            checkDecoratorPropertyType(propertyType);

            Literal defaultValue = null;
            if (propertyNode.getDefault() != null) {
                final Literal rawDefaultValue = resolveLiteral(decoratorDef, propertyNode.getDefault());
                defaultValue = checkLiteral(rawDefaultValue, propertyType);
                if (defaultValue == null)
                    throw new IllegalArgumentException("Field '" + decoratorNode.getName() + "." +
                        propertyNode.getName() + "' cannot have default value '" + propertyNode.getDefault() + "'.");
            }

            final DecoratorPropertyDef propertyDef = new DecoratorPropertyDef(decoratorDef, propertyNode.getName(),
                propertyType, defaultValue, propertyNode.getComments());
        }

        buildTypeDefinitions(decoratorDef, decoratorNode);
    }

    private void checkInterfaceDefinition(InterfaceDef interfaceDef, InterfaceNode interfaceNode) {
        if (interfaceNode.getSupertypes() != null) {
            for (String supertypeName : new HashSet<>(interfaceNode.getSupertypes())) {
                final Type supertype = resolveType(interfaceDef, supertypeName);
                if (supertype.getKind() != TypeKind.INTERFACE)
                    throw new IllegalStateException("Type '" + supertype + "' must be either an interface.");

                interfaceDef.getSuperInterfaces().add(((TypeInterface) supertype).getDefinition());
            }
        }

        for (InterfacePropertyNode propertyNode : interfaceNode.getProperties()) {
            final Type propertyType = resolveType(interfaceDef, propertyNode.getType());
            final InterfacePropertyDef propertyDef = new InterfacePropertyDef(interfaceDef, propertyNode.getName(),
                propertyType, propertyNode.isOverride(), propertyNode.isGettable(), propertyNode.isSettable(),
                propertyNode.getComments());
        }

        buildTypeDefinitions(interfaceDef, interfaceNode);
    }

    private void checkClassDefinition(ClassDef classDef, ClassNode classNode) {
        if (classNode.getSupertypes() != null) {
            for (String supertypeName : new HashSet<>(classNode.getSupertypes())) {
                final Type supertype = resolveType(classDef, supertypeName);
                if (supertype.getKind() != TypeKind.CLASS && supertype.getKind() != TypeKind.INTERFACE)
                    throw new IllegalStateException("Type '" + supertype + "' must be either a class or an interface.");

                if (supertype.getKind() == TypeKind.CLASS) {
                    if (classDef.getSuperClass() != null)
                        throw new IllegalStateException("Class '" + classDef.getName() + "' cannot have more than one superclass.");
                    final ClassDef superClass = ((TypeClass) supertype).getDefinition();
                    if (superClass.isFinal())
                        throw new IllegalStateException("Cannot inherit class '" + classDef.getFullName() + "' from final '" + superClass.getFullName() + "'.");
                    classDef.setSuperClass(superClass);
                } else {
                    classDef.getSuperInterfaces().add(((TypeInterface) supertype).getDefinition());
                }
            }
        }

        for (ClassPropertyNode propertyNode : classNode.getProperties()) {
            final Type propertyType = resolveType(classDef, propertyNode.getType());

            Literal defaultValue = null;
            if (propertyNode.getDefault() != null) {
                final Literal rawDefaultValue = resolveLiteral(classDef, propertyNode.getDefault());
                defaultValue = checkLiteral(rawDefaultValue, propertyType);
                if (defaultValue == null)
                    throw new IllegalArgumentException("Field '" + classNode.getName() + "." + propertyNode.getName()
                        + "' cannot have default value '" + propertyNode.getDefault() + "'.");
            }

            final ClassPropertyDef propertyDef = new ClassPropertyDef(classDef, propertyNode.getName(), propertyType,
                defaultValue, propertyNode.isOverride(), propertyNode.isFinal(), propertyNode.getComments());
        }

        buildTypeDefinitions(classDef, classNode);
    }

    private Set<TypeCustom> tryResolveCustomType(@NotNull TypeScope scope, @NotNull String name) {
        return name.contains(".")
            ? tryResolveCustomTypeByQualifiedName(scope, name)
            : tryResolveCustomTypeByName(scope, name);
    }

    private Set<TypeCustom> tryResolveCustomTypeByName(@NotNull TypeScope scope, @NotNull String name) {
        assert !name.contains(".") : "Type name cannot be qualified at this point.";

        final Set<TypeCustom> candidates = new HashSet<>();
        final FileDef fileDef = scope.getFile();

        // Lookup definition in the current and all parent scopes.

        for (TypeScope currentScope = scope; currentScope != null; currentScope = currentScope.getParent()) {
            for (ClassDef classDef : currentScope.getDefinedClasses())
                if (classDef.getName().equals(name))
                    candidates.add(classDef.getType());
            for (InterfaceDef interfaceDef : currentScope.getDefinedInterfaces())
                if (interfaceDef.getName().equals(name))
                    candidates.add(interfaceDef.getType());
            for (DecoratorDef decoratorDef : currentScope.getDefinedDecorators())
                if (decoratorDef.getName().equals(name))
                    candidates.add(decoratorDef.getType());
            for (EnumerationDef enumerationDef : currentScope.getDefinedEnumerations())
                if (enumerationDef.getName().equals(name))
                    candidates.add(enumerationDef.getType());
            if (!candidates.isEmpty())
                return candidates;
        }

        // Lookup type definition in the same namespace.

        final NamespaceDef namespaceDef = fileDef.getNamespace();
        final TypeCustom type = namespaceDef.getDefinedTypes().get(name);
        if (type != null) {
            candidates.add(type);
            return candidates;
        }

        // Prefer aliased types over regular imports.

        for (ImportDef importDef : fileDef.getImports()) {
            if (importDef.getKind() == ImportKind.TYPE) {
                final ImportTypeDef typeImportDef = (ImportTypeDef) importDef;
                if (name.equals(typeImportDef.getAlias()))
                    candidates.add(typeImportDef.getTarget());
            }
        }
        if (!candidates.isEmpty())
            return candidates;

        // Other import directives.

        for (ImportDef importDef : fileDef.getImports()) {
            if (importDef.getKind() == ImportKind.TYPE) {
                final ImportTypeDef importTypeDef = (ImportTypeDef) importDef;
                if (name.equals(importTypeDef.getTarget().getName()) && importTypeDef.getAlias() == null)
                    candidates.add(importTypeDef.getTarget());
            } else {
                final ImportNamespaceDef importNamespaceDef = (ImportNamespaceDef) importDef;
                final TypeCustom importedType = importNamespaceDef.getTarget().getDefinedTypes().get(name);
                if (importedType != null)
                    candidates.add(importedType);
            }
        }

        return candidates;
    }

    private Set<TypeCustom> tryResolveCustomTypeByQualifiedName(@NotNull TypeScope scope, @NotNull String qualifiedName) {
        assert qualifiedName.contains(".") : "Type name must be qualified at this point.";

        final Set<TypeCustom> candidates = new HashSet<>();

        final int i = qualifiedName.lastIndexOf('.');
        final String outerName = qualifiedName.substring(0, i);
        final String typeName = qualifiedName.substring(i + 1);

        // Case 1: `outerName` is the name of the type itself.

        final Set<TypeCustom> parentCandidates = tryResolveCustomType(scope, outerName);
        for (TypeCustom parent : parentCandidates) {
            TypeScope parentScope;
            if (parent.getKind() == TypeKind.CLASS)
                parentScope = ((TypeClass) parent).getDefinition();
            else if (parent.getKind() == TypeKind.DECORATOR)
                parentScope = ((TypeDecorator) parent).getDefinition();
            else if (parent.getKind() == TypeKind.INTERFACE)
                parentScope = ((TypeInterface) parent).getDefinition();
            else
                continue;

            for (ClassDef classDef : parentScope.getDefinedClasses())
                if (classDef.getName().equals(typeName))
                    candidates.add(classDef.getType());
            for (InterfaceDef interfaceDef : parentScope.getDefinedInterfaces())
                if (interfaceDef.getName().equals(typeName))
                    candidates.add(interfaceDef.getType());
            for (DecoratorDef decoratorDef : parentScope.getDefinedDecorators())
                if (decoratorDef.getName().equals(typeName))
                    candidates.add(decoratorDef.getType());
            for (EnumerationDef enumerationDef : parentScope.getDefinedEnumerations())
                if (enumerationDef.getName().equals(typeName))
                    candidates.add(enumerationDef.getType());
        }

        // Case 2: `outerName` is the namespace in current project and/or referenced projects.

        final ProjectDef projectDef = scope.getFile().getNamespace().getProject();
        TypeCustom type = tryLookUpType(projectDef, outerName, typeName);
        if (type != null)
            candidates.add(type);
        for (ProjectDef referencedProjectDef : projectDef.getReferences().values()) {
            type = tryLookUpType(referencedProjectDef, outerName, typeName);
            if (type != null)
                candidates.add(type);
        }

        return candidates;
    }

    private static TypeCustom tryLookUpType(ProjectDef projectDef, String namespace, String name) {
        final NamespaceDef namespaceDef = projectDef.getNamespaces().get(namespace);
        return namespaceDef != null ? namespaceDef.getDefinedTypes().get(name) : null;
    }

    @NotNull
    private TypeCustom resolveCustomType(@NotNull TypeScope scope, @NotNull String name) {
        final Set<TypeCustom> candidates = tryResolveCustomType(scope, name);
        if (candidates.size() == 0)
            throw new IllegalStateException(scope.getFile().getFileName() + ": type '" + name + "' is undefined.");
        if (candidates.size() > 1)
            throw new IllegalStateException(scope.getFile().getFileName() + ": type '" + name + "' is ambiguous.");
        return candidates.iterator().next();
    }

    @NotNull
    private List<Type> resolveTypeList(@NotNull TypeScope scope, @NotNull String nameList) {
        int index = 0;
        int angles = 0;

        loop:
        while (index < nameList.length()) {
            final char c = nameList.charAt(index);
            switch (c) {
                case '<':
                    angles += 1;
                    break;

                case '>':
                    angles -= 1;
                    break;

                case ',':
                    if (angles == 0)
                        break loop;
            }
            index += 1;
        }

        final Type type = resolveType(scope, nameList.substring(0, index));
        if (index < nameList.length()) {
            final List<Type> remainingTypes = resolveTypeList(scope, nameList.substring(index + 1));
            remainingTypes.add(0, type);
            return remainingTypes;
        } else {
            final List<Type> types = new ArrayList<>();
            types.add(type);
            return types;
        }
    }

    @NotNull
    private Type resolveType(@NotNull TypeScope scope, @NotNull String name) {
        if (name.endsWith("?"))
            return new TypeNullable(resolveType(scope, name.substring(0, name.length() - 1)));

        if (name.endsWith(">")) {
            final int index = name.indexOf('<');
            final String genericType = name.substring(0, index);
            final String typeParameters = name.substring(index + 1, name.length() - 1);
            final List<Type> typeList = resolveTypeList(scope, typeParameters);

            switch (genericType) {
                case "List":
                    if (typeList.size() != 1)
                        throw new IllegalStateException("List must have exactly one generic parameter.");
                    return new TypeList(typeList.get(0));

                case "Set":
                    if (typeList.size() != 1)
                        throw new IllegalStateException("Set must have exactly one generic parameter.");
                    return new TypeSet(typeList.get(0));

                case "Map":
                    if (typeList.size() != 2)
                        throw new IllegalStateException("Map must have exactly two generic parameter.");
                    return new TypeMap(typeList.get(0), typeList.get(1));

                case "Action":
                    return new TypeAction(typeList);

                case "Function":
                    if (typeList.size() < 1)
                        throw new IllegalStateException("Function must have at least one generic parameter.");
                    final Type returnType = typeList.remove(typeList.size() - 1);
                    return new TypeFunction(typeList, returnType);

                default:
                    throw new IllegalStateException("Generic type '" + genericType + "' is not defined.");
            }
        }

        if (!name.contains(".")) {
            final Type type = PREDEFINED_TYPES.get(name);
            if (type != null)
                return type;
            if (name.equals("Action"))
                return new TypeAction();
        }

        return resolveCustomType(scope, name);
    }

    @SuppressWarnings("ConstantConditions")
    private Literal resolveMemberReference(TypeScope scope, String text) {
        final int index = text.lastIndexOf('.');

        String memberName;
        Type type;

        if (index > 0) {
            final String enumerationName = text.substring(0, index);
            memberName = text.substring(index + 1);
            type = resolveType(scope, enumerationName);
            if (type == null)
                throw new IllegalStateException("Type '" + enumerationName + "' is not defined.");
        } else {
            if (scope instanceof ClassDef)
                type = ((ClassDef) scope).getType();
            else if (scope instanceof InterfaceDef)
                type = ((InterfaceDef) scope).getType();
            else if (scope instanceof DecoratorDef)
                type = ((DecoratorDef) scope).getType();
            else
                throw new IllegalStateException("Something went completely wrong.");
            memberName = text;
        }

        ConstantScope constantScope = null;
        switch (type.getKind()) {
            case ENUMERATION: {
                final EnumerationDef enumerationDef = ((TypeEnumeration) type).getDefinition();
                for (EnumerationMemberDef memberDef : enumerationDef.getMembers())
                    if (memberDef.getName().equals(memberName))
                        return new LiteralEnumerationValue(memberDef);

                throw new IllegalStateException("Enumeration '" + type + "' does not contain member with name '" + memberName + "'.");
            }

            case CLASS:
                constantScope = ((TypeClass) type).getDefinition();
                break;

            case DECORATOR:
                constantScope = ((TypeDecorator) type).getDefinition();
                break;

            case INT8:
                constantScope = Int8Def.INSTANCE;
                break;

            case INT16:
                constantScope = Int16Def.INSTANCE;
                break;

            case INT32:
                constantScope = Int32Def.INSTANCE;
                break;

            case INT64:
                constantScope = Int64Def.INSTANCE;
                break;

            case UINT8:
                constantScope = UInt8Def.INSTANCE;
                break;

            case UINT16:
                constantScope = UInt16Def.INSTANCE;
                break;

            case UINT32:
                constantScope = UInt32Def.INSTANCE;
                break;

            case UINT64:
                constantScope = UInt64Def.INSTANCE;
                break;

            case FLOAT32:
                constantScope = Float32Def.INSTANCE;
                break;

            case FLOAT64:
                constantScope = Float64Def.INSTANCE;
                break;

            case DECIMAL:
                constantScope = DecimalDef.INSTANCE;
                break;

            case TIMESTAMP:
                constantScope = TimestampDef.INSTANCE;
                break;

            case DURATION:
                constantScope = DurationDef.INSTANCE;
                break;

            case DATE:
                constantScope = DateDef.INSTANCE;
                break;

            case TIME:
                constantScope = TimeDef.INSTANCE;
                break;

            case UUID:
                constantScope = UUIDDef.INSTANCE;
                break;
        }

        if (constantScope != null) {
            for (ConstantDef constantDef : constantScope.getDefinedConstants())
                if (constantDef.getName().equals(memberName))
                    return new LiteralConstant(constantDef);

            throw new IllegalStateException("Type '" + type + "' does contain constant with name '" + memberName + "'.");
        }

        throw new IllegalStateException("Type '" + type + "' does not provide any constant definitions.");
    }

    private LiteralType resolveTypeReference(TypeScope scope, String text) {
        final Type type = resolveType(scope, text);
        return new LiteralType(type);
    }

    private Literal resolveLiteral(TypeScope scope, Literal literal) {
        if (literal instanceof LiteralUnresolved)
            return resolveUnresolvedLiteral(scope, (LiteralUnresolved) literal);

        if (literal instanceof LiteralList) {
            final LiteralList list = (LiteralList) literal;
            for (int i = 0; i < list.getValue().size(); i += 1)
                list.getValue().set(i, resolveLiteral(scope, list.getValue().get(i)));

            return list;
        }

        return literal;
    }

    private Literal resolveUnresolvedLiteral(TypeScope scope, LiteralUnresolved literal) {
        switch (literal.getKind()) {
            case ENUMERATION_VALUE:
            case CONSTANT:
                return resolveMemberReference(scope, literal.getValue());

            case TYPE:
                return resolveTypeReference(scope, literal.getValue());

            default:
                throw new IllegalStateException("Kind of unresolved literal is unknown.");
        }
    }

    private boolean doCheckLiteral(LiteralConstant literal, Type expectedType, Queue<LiteralConstant> visited) {
        if (visited.contains(literal))
            return false;
        visited.add(literal);

        Literal value = literal.getValue().getValue();
        if (value instanceof LiteralConstant)
            return doCheckLiteral((LiteralConstant) value, expectedType, visited);

        return checkLiteral(value, expectedType) != null;
    }

    private Literal checkLiteral(Literal literal, Type expectedType) {
        if (literal instanceof LiteralConstant)
            return !doCheckLiteral((LiteralConstant) literal, expectedType, new ArrayDeque<LiteralConstant>()) ? null : literal;

        switch (expectedType.getKind()) {
            case TYPE:
                return literal.getKind() == LiteralKind.TYPE ? literal : null;

            case BOOLEAN:
                return literal.getKind() == LiteralKind.BOOLEAN ? literal : null;

            case INT8:
            case UINT8:
            case INT16:
            case UINT16:
            case INT32:
            case UINT32:
            case INT64:
            case UINT64:
                if (literal instanceof LiteralInteger)
                    return ((LiteralInteger) literal).castTo(expectedType.getKind().toIntegralType());
                return null;

            case FLOAT32:
                return literal.getKind() == LiteralKind.FLOAT32 ? literal : null;

            case FLOAT64:
                return literal.getKind() == LiteralKind.FLOAT64 ? literal : null;

            case DECIMAL:
                return literal.getKind() == LiteralKind.DECIMAL ? literal : null;

            case TIMESTAMP:
                return literal.getKind() == LiteralKind.TIMESTAMP ? literal : null;

            case DURATION:
                return literal.getKind() == LiteralKind.DURATION ? literal : null;

            case DATE:
                return literal.getKind() == LiteralKind.DATE ? literal : null;

            case TIME:
                return literal.getKind() == LiteralKind.TIME ? literal : null;

            case UUID:
                return literal.getKind() == LiteralKind.UUID ? literal : null;

            case TEXT:
                return literal.getKind() == LiteralKind.TEXT ? literal : null;

            case NULLABLE:
                return literal.getKind() == LiteralKind.NULL ? literal :
                    checkLiteral(literal, ((TypeNullable) expectedType).getUnderlyingType());

            case LIST:
                if (literal.getKind() == LiteralKind.LIST) {
                    final TypeList listType = (TypeList) expectedType;
                    final LiteralList typedValue = (LiteralList) literal;
                    if (typedValue.getValue().size() == 0)
                        return literal;

                    final LiteralList result = new LiteralList();
                    for (Literal value : typedValue.getValue()) {
                        final Literal item = checkLiteral(value, listType.getUnderlyingType());
                        if (item == null)
                            return null;

                        result.getValue().add(item);
                    }

                    return result;
                }
                return null;

            case ENUMERATION:
                if (literal.getKind() == LiteralKind.ENUMERATION_VALUE) {
                    final LiteralEnumerationValue typedValue = (LiteralEnumerationValue) literal;
                    return typedValue.getValue().getOwner().equals(((TypeEnumeration) expectedType).getDefinition()) ? literal : null;
                }
                return null;

            default:
                return null;
        }
    }

    private void doCheckDecorators(TypeScope scope, ItemDef decoratable, List<DecoratorValueNode> decorators,
                                   DecoratorTarget target, String name) {
        for (DecoratorValueNode decoratorValueNode : decorators) {
            final Type decoratorType = resolveType(scope, decoratorValueNode.getName());
            if (decoratorType.getKind() != TypeKind.DECORATOR)
                throw new IllegalStateException("Error while processing '" + name + "': decorator '" + decoratorValueNode.getName() + "' is not defined");

            final DecoratorDef decoratorDef = ((TypeDecorator) decoratorType).getDefinition();

            final DecoratorUsage decoratorUsage = retrieveDecoratorUsage(decoratorDef);
            if (!decoratorUsage.getValidOn().contains(target))
                throw new IllegalStateException("Error while processing '" + name + "': decorator '" + decoratorDef.getFullName() + "' cannot be applied to " + target + ".");
            if (!decoratorUsage.isRepeatable()) {
                for (Decorator another : decoratable.getDecorators())
                    if (another.getDefinition() == decoratorDef)
                        throw new IllegalStateException("Error while processing '" + name + "': decorator '" + decoratorDef.getFullName() + "' cannot be applied more than once.");
            }

            final List<DecoratorPropertyValue> arguments = new ArrayList<>();
            for (DecoratorPropertyDef propertyDef : decoratorDef.getProperties()) {
                NameValuePair argument = null;
                for (NameValuePair arg : decoratorValueNode.getArguments())
                    if (arg.getName().equals(propertyDef.getName())) {
                        argument = arg;
                        break;
                    }

                if (argument == null && propertyDef.getDefault() == null)
                    throw new IllegalStateException("Error while processing '" + name + "': decorator argument '" + decoratorDef.getFullName() + "." + propertyDef.getName() + "' is not set.");

                final Literal value;
                if (argument != null) {
                    value = checkLiteral(resolveLiteral(scope, argument.getValue()), propertyDef.getType());
                    if (value == null)
                        throw new IllegalStateException("Error while processing '" + name + "': cannot cast value '" + argument.getValue() + "' to type '" + propertyDef.getType() + "' for decorator property '" + decoratorDef.getName() + "." + decoratorDef.getName() + "'.");
                } else
                    value = propertyDef.getDefault();

                arguments.add(new DecoratorPropertyValue(propertyDef, value, argument == null));
            }
            for (NameValuePair argument : decoratorValueNode.getArguments()) {
                DecoratorPropertyDef propertyDef = null;
                for (DecoratorPropertyDef def : decoratorDef.getProperties()) {
                    if (def.getName().equals(argument.getName())) {
                        propertyDef = def;
                        break;
                    }
                }
                if (propertyDef == null)
                    throw new IllegalStateException("Error while processing '" + name + "': decorator '" + decoratorDef.getName() + "' does not contain property with name '" + argument.getName() + "'.");
            }
            decoratable.getDecorators().add(new Decorator(decoratorDef, arguments));
        }
    }

    private void checkDecorators(TypeScope scopeDef, TypeContainerNode scopeNode) {
        for (int i = 0; i < scopeNode.getDefinedEnumerations().size(); i += 1)
            doCheckDecorators(scopeDef, scopeDef.getDefinedEnumerations().get(i), scopeNode.getDefinedEnumerations().get(i));
        for (int i = 0; i < scopeNode.getDefinedDecorators().size(); i += 1)
            doCheckDecorators(scopeDef, scopeDef.getDefinedDecorators().get(i), scopeNode.getDefinedDecorators().get(i));
        for (int i = 0; i < scopeNode.getDefinedClasses().size(); i += 1)
            doCheckDecorators(scopeDef, scopeDef.getDefinedClasses().get(i), scopeNode.getDefinedClasses().get(i));
        for (int i = 0; i < scopeNode.getDefinedInterfaces().size(); i += 1)
            doCheckDecorators(scopeDef, scopeDef.getDefinedInterfaces().get(i), scopeNode.getDefinedInterfaces().get(i));
    }

    private void doCheckDecorators(TypeScope scope, EnumerationDef enumerationDef, EnumerationNode enumerationNode) {
        doCheckDecorators(scope, enumerationDef, enumerationNode.getDecorators(), DecoratorTarget.ENUMERATION, enumerationDef.getFullName());

        for (int i = 0; i < enumerationNode.getMembers().size(); i += 1)
            doCheckDecorators(scope, enumerationDef.getMembers().get(i), enumerationNode.getMembers().get(i).getDecorators(), DecoratorTarget.ENUMERATION_MEMBER, enumerationDef.getMembers().get(i).getFullName());
    }

    private void doCheckDecorators(TypeScope scope, DecoratorDef decoratorDef, DecoratorNode decoratorNode) {
        doCheckDecorators(scope, decoratorDef, decoratorNode.getDecorators(), DecoratorTarget.DECORATOR, decoratorDef.getFullName());

        for (int i = 0; i < decoratorNode.getProperties().size(); i += 1)
            doCheckDecorators(decoratorDef, decoratorDef.getProperties().get(i), decoratorNode.getProperties().get(i).getDecorators(), DecoratorTarget.DECORATOR_PROPERTY, decoratorDef.getProperties().get(i).getFullName());

        checkDecorators(decoratorDef, decoratorNode);
    }

    private void doCheckDecorators(TypeScope scope, ClassDef classDef, ClassNode classNode) {
        doCheckDecorators(scope, classDef, classNode.getDecorators(), DecoratorTarget.CLASS, classDef.getFullName());

        for (int i = 0; i < classNode.getProperties().size(); i += 1)
            doCheckDecorators(classDef, classDef.getProperties().get(i), classNode.getProperties().get(i).getDecorators(), DecoratorTarget.CLASS_PROPERTY, classDef.getProperties().get(i).getFullName());

        checkDecorators(classDef, classNode);
    }

    private void doCheckDecorators(TypeScope scope, InterfaceDef interfaceDef, InterfaceNode interfaceNode) {
        doCheckDecorators(scope, interfaceDef, interfaceNode.getDecorators(), DecoratorTarget.INTERFACE, interfaceDef.getFullName());

        for (int i = 0; i < interfaceNode.getProperties().size(); i += 1)
            doCheckDecorators(interfaceDef, interfaceDef.getProperties().get(i), interfaceNode.getProperties().get(i).getDecorators(), DecoratorTarget.INTERFACE_PROPERTY, interfaceDef.getProperties().get(i).getFullName());

        for (int i = 0; i < interfaceNode.getMethods().size(); i += 1) {
            final InterfaceMethodDef interfaceMethodDef = interfaceDef.getMethods().get(i);
            final InterfaceMethodNode interfaceMethodNode = interfaceNode.getMethods().get(i);
            doCheckDecorators(interfaceDef, interfaceMethodDef, interfaceMethodNode.getDecorators(), DecoratorTarget.INTERFACE_METHOD, interfaceMethodDef.getFullName());

            for (int j = 0; j < interfaceMethodNode.getFormalParameters().size(); j += 1)
                doCheckDecorators(interfaceDef, interfaceMethodDef.getFormalParameters().get(j), interfaceMethodNode.getFormalParameters().get(j).getDecorators(), DecoratorTarget.FORMAL_PARAMETER, interfaceMethodDef.getFormalParameters().get(j).getFullName());
        }

        checkDecorators(interfaceDef, interfaceNode);
    }

    private void accumulateInheritedProperties(String name, HashMap<String, ClassPropertyDef> properties,
                                               ClassDef classDef) {
        if (classDef.getSuperClass() != null)
            accumulateInheritedProperties(name, properties, classDef.getSuperClass());

        for (ClassPropertyDef propertyDef : classDef.getProperties()) {
            ClassPropertyDef inheritedPropertyDef = properties.get(propertyDef.getName());
            if (inheritedPropertyDef != null) {
                if (!propertyDef.isOverride() || !propertyDef.getType().equals(inheritedPropertyDef.getType()))
                    throw new IllegalStateException("Type " + name + " inherited property with name " + propertyDef.getName() + " twice: one from " + propertyDef.getOwner().getFullName() + " and one from " + inheritedPropertyDef.getOwner().getFullName());
                if (inheritedPropertyDef.isFinal())
                    throw new IllegalStateException("Type " + name + " cannot override property " + propertyDef.getName() + " as it is declared as final in the base class " + inheritedPropertyDef.getOwner().getFullName());
            } else if (propertyDef.isOverride())
                throw new IllegalStateException("Property '" + propertyDef.getOwner().getFullName() + "." + propertyDef.getName() + "' does not override anything.");
            properties.put(propertyDef.getName(), propertyDef);
        }
    }

    private void checkInterfaceImplementation(String name, HashMap<String, ClassPropertyDef> classProperties,
                                              InterfaceDef interfaceDef) {
        for (InterfacePropertyDef interfacePropertyDef : interfaceDef.getProperties()) {
            final ClassPropertyDef classPropertyDef = classProperties.get(interfacePropertyDef.getName());
            if (classPropertyDef == null)
                throw new IllegalStateException("Class " + name + " does not contain property " +
                    interfacePropertyDef.getName() + " inherited from the interface " +
                    interfacePropertyDef.getOwner().getName());
            if (!classPropertyDef.getType().equals(interfacePropertyDef.getType()))
                throw new IllegalStateException("Type of the property " + name + "." +
                    classPropertyDef.getName() + " differs from the inherited " +
                    interfacePropertyDef.getOwner().getName() + "." + interfacePropertyDef.getName());
        }

        for (InterfaceDef superInterfaceDef : interfaceDef.getSuperInterfaces())
            checkInterfaceImplementation(name, classProperties, superInterfaceDef);
    }

    private void checkInheritance(ClassDef classDef) {
        final HashMap<String, ClassPropertyDef> classProperties = new HashMap<>();
        accumulateInheritedProperties(classDef.getFullName(), classProperties, classDef);

        for (InterfaceDef superInterfaceDef : classDef.getSuperInterfaces())
            checkInterfaceImplementation(classDef.getFullName(), classProperties, superInterfaceDef);
    }

    private void accumulateInheritedProperties(String name, HashMap<String, InterfacePropertyDef> properties, InterfaceDef interfaceDef) {
        for (InterfacePropertyDef property : interfaceDef.getProperties()) {
            final InterfacePropertyDef existingProperty = properties.get(property.getName());
            if (existingProperty != null && !existingProperty.getType().equals(property.getType()))
                throw new IllegalStateException("Type " + name + " inherited property with name " + property.getName() +
                    " twice: one from " + property.getOwner().getName() + " and one from " +
                    existingProperty.getOwner().getName());

            properties.put(property.getName(), property);
        }
        for (InterfaceDef superInterfaceDef : interfaceDef.getSuperInterfaces())
            accumulateInheritedProperties(name, properties, superInterfaceDef);
    }

    private void checkInheritance(InterfaceDef interfaceDef) {
        final HashMap<String, InterfacePropertyDef> properties = new HashMap<>();
        for (InterfaceDef superInterfaceDef : interfaceDef.getSuperInterfaces())
            accumulateInheritedProperties(interfaceDef.getName(), properties, superInterfaceDef);

        for (InterfacePropertyDef propertyDef : interfaceDef.getProperties()) {
            final InterfacePropertyDef inheritedPropertyDef = properties.get(propertyDef.getName());
            if (inheritedPropertyDef != null) {
                if (!propertyDef.isOverride())
                    throw new IllegalStateException("Property '" + propertyDef.getFullName() +
                        "' hides inherited property '" + inheritedPropertyDef.getFullName() + "'.");
            } else {
                if (propertyDef.isOverride())
                    throw new IllegalStateException("'" + propertyDef.getFullName() + "': no suitable property found to override.");
            }
        }
    }

    private ConstantDef checkConstant(ConstantNode constantNode, ConstantAndTypeScope ownerDef) {
        final Type type = resolveType(ownerDef, constantNode.getType());
        return new ConstantDef(ownerDef, constantNode.getName(), type, constantNode.getValue(),
            constantNode.getComments());
    }

    private static void collectType(NamespaceDef namespaceDef, String name, TypeCustom type) {
        final TypeCustom existingType = namespaceDef.getDefinedTypes().get(name);
        if (existingType != null) {
            assert existingType.getFile() != null;
            assert type.getFile() != null;
            throw new IllegalStateException("Namespace '" + namespaceDef.getNamespace() +
                "' contains two different types with name '" + name + "': one in file '" +
                existingType.getFile().getFileName() + "' and another in '" + type.getFile().getFileName() + "'.");
        }
        namespaceDef.getDefinedTypes().put(name, type);
    }

    private static String getOuterTypeName(TypeScope scope) {
        if (scope instanceof ClassDef)
            return ((ClassDef) scope).getName();
        if (scope instanceof InterfaceDef)
            return ((InterfaceDef) scope).getName();
        if (scope instanceof DecoratorDef)
            return ((DecoratorDef) scope).getName();
        return null;
    }

    private static void checkForTypeConflicts(TypeScope parent, String name) {
        if (PREDEFINED_TYPES.containsKey(name))
            throw new IllegalStateException("Type name '" + name + "' conflicts with builtin type of the same name.");

        for (EnumerationDef enumerationDef : parent.getDefinedEnumerations())
            if (enumerationDef.getName().equals(name))
                throw new IllegalStateException("Type name '" + name + "' is already defined within the current scope.");
        for (ClassDef classDef : parent.getDefinedClasses())
            if (classDef.getName().equals(name))
                throw new IllegalStateException("Type name '" + name + "' is already defined within the current scope.");
        for (InterfaceDef interfaceDef : parent.getDefinedInterfaces())
            if (interfaceDef.getName().equals(name))
                throw new IllegalStateException("Type name '" + name + "' is already defined within the current scope.");
        for (DecoratorDef decoratorDef : parent.getDefinedDecorators())
            if (decoratorDef.getName().equals(name))
                throw new IllegalStateException("Type name '" + name + "' is already defined within the current scope.");

        if (name.equals(getOuterTypeName(parent)))
            throw new IllegalStateException("Type name '" + name + "' conflicts with its outer type name.");
    }

    private static ProjectFile parseProjectFile(Location location) throws IOException {
        final Gson gson = new GsonBuilder().create();
        try (InputStreamReader reader = new InputStreamReader(location.readAsStream(), Charset.forName("UTF-8"))) {
            // TODO: Validate loaded project file.
            return gson.fromJson(reader, ProjectFile.class);
        }
    }

    public static class ThrowingErrorListener extends BaseErrorListener {
        public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) throws ParseCancellationException {
            throw new ParseCancellationException("line " + line + ":" + charPositionInLine + ": " + msg);
        }
    }

    private static FileNode parseSourceFile(Location location) throws IOException {
        final ANTLRInputStream input = new ANTLRInputStream(location.readAsStream());

        final LuminaryLexer lexer = new LuminaryLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);

        final LuminaryParser parser = new LuminaryParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);

        final ParseTree tree = parser.protocol();
        final ParseTreeWalker walker = new ParseTreeWalker();
        final LuminaryListenerImplementation visitor = new LuminaryListenerImplementation(tokens);
        walker.walk(visitor, tree);
        if (visitor.getNumberOfErrors() != 0)
            throw new IllegalStateException("File '" + location + "' does not contain a valid protocol definition.");

        final FileNode fileNode = visitor.getFile();
        fileNode.setLocation(location);
        return fileNode;
    }

    private static IntegralType extractUnderlyingType(String text) {
        if ("Int64".equals(text))
            return IntegralType.INT64;
        if ("UInt64".equals(text))
            return IntegralType.UINT64;
        if ("Int32".equals(text))
            return IntegralType.INT32;
        if ("UInt32".equals(text))
            return IntegralType.UINT32;
        if ("Int16".equals(text))
            return IntegralType.INT16;
        if ("UInt16".equals(text))
            return IntegralType.UINT16;
        if ("Int8".equals(text))
            return IntegralType.INT8;
        if ("UInt8".equals(text))
            return IntegralType.UINT8;
        throw new IllegalArgumentException("Type 'Int8', 'Int16', 'Int32', 'Int64', 'UInt8', 'UInt16', 'UInt32' or 'UInt64' expected.");
    }

    private static DecoratorUsage decoratorUsageForItself() {
        final DecoratorUsage usage = new DecoratorUsage();
        usage.getValidOn().add(DecoratorTarget.DECORATOR);
        return usage;
    }

    private DecoratorUsage retrieveDecoratorUsage(DecoratorDef decoratorDef) {
        if (decoratorDef == DECORATOR_USAGE_DEF)
            return decoratorUsageForItself();

        DecoratorUsage usage = null;
        for (Decorator decorator : decoratorDef.getDecorators()) {
            if (decorator.getDefinition() == DECORATOR_USAGE_DEF) {
                if (usage != null)
                    throw new IllegalStateException("Error while processing '" + decoratorDef.getFullName() + "': 'DecoratorUsage' cannot be applied more than once.");

                usage = new DecoratorUsage();

                LiteralList validOn = null;
                for (DecoratorPropertyValue argument : decorator.getArguments())
                    if (argument.getDefinition().getName().equals("ValidOn")) {
                        validOn = (LiteralList) argument.getValue();
                        break;
                    }
                assert validOn != null;

                for (Literal item : validOn.getValue()) {
                    LiteralEnumerationValue value = (LiteralEnumerationValue) item;
                    assert value != null;

                    switch (value.getValue().getName()) {
                        case "ENUMERATION":
                            usage.getValidOn().add(DecoratorTarget.ENUMERATION);
                            break;

                        case "ENUMERATION_MEMBER":
                            usage.getValidOn().add(DecoratorTarget.ENUMERATION_MEMBER);
                            break;

                        case "INTERFACE":
                            usage.getValidOn().add(DecoratorTarget.INTERFACE);
                            break;

                        case "INTERFACE_PROPERTY":
                            usage.getValidOn().add(DecoratorTarget.INTERFACE_PROPERTY);
                            break;

                        case "CLASS":
                            usage.getValidOn().add(DecoratorTarget.CLASS);
                            break;

                        case "CLASS_PROPERTY":
                            usage.getValidOn().add(DecoratorTarget.CLASS_PROPERTY);
                            break;

                        case "DECORATOR":
                            usage.getValidOn().add(DecoratorTarget.DECORATOR);
                            break;

                        case "DECORATOR_PROPERTY":
                            usage.getValidOn().add(DecoratorTarget.DECORATOR_PROPERTY);
                            break;

                        case "INTERFACE_METHOD":
                            usage.getValidOn().add(DecoratorTarget.INTERFACE_METHOD);
                            break;

                        case "FORMAL_PARAMETER":
                            usage.getValidOn().add(DecoratorTarget.FORMAL_PARAMETER);
                            break;
                    }

                    LiteralBoolean repeatable = null;
                    for (DecoratorPropertyValue argument : decorator.getArguments())
                        if (argument.getDefinition().getName().equals("Repeatable")) {
                            repeatable = (LiteralBoolean) argument.getValue();
                            break;
                        }
                    assert repeatable != null;
                    usage.setRepeatable(repeatable.getValue());
                }
            }
        }

        return usage != null ? usage : DecoratorUsage.byDefault();
    }

    private List<Pair<Location, Location>> enumerateSources(ProjectFile projectFile) throws IOException {
        final List<Pair<Location, Location>> files = new ArrayList<>();

        for (int i = 0; i < projectFile.Sources.length; i += 1) {
            final ProjectSource source = projectFile.Sources[i];
            final Location location = projectFile.Path.getParent().resolve(source.Directory);
            if (location == null)
                continue;

            for (Location file : location.listFiles())
                if (file.getFileName().endsWith(".lux"))
                    files.add(new Pair<>(location, file));
        }

        return files;
    }

    private static String getSearchPath() {
        final String searchPath = System.getProperty("luminary.search.path");
        return searchPath != null ? searchPath : System.getenv("LUMINARY_SEARCH_PATH");
    }

    public static void main(String[] arguments) throws IOException {
        final ProjectLoader loader = new ProjectLoader();
        loader.setVerbose(true);
        final ProjectDef projectDef = loader.load("samples/Test.json");
    }
}
