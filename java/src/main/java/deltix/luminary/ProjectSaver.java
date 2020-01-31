package deltix.luminary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectSaver {
    public static void save(ProjectDef projectDef, String rootDirectory) throws IOException {
        if (projectDef.getFileName() == null)
            throw new IllegalArgumentException("Built-in projects cannot be exported.");
        final Path directory = Paths.get(rootDirectory);
        if (!Files.exists(directory))
            throw new IllegalArgumentException("Directory does not exist.");

        saveProjectFile(projectDef, directory);
        for (NamespaceDef namespaceDef : projectDef.getNamespaces().values())
            saveNamespace(namespaceDef, directory);

    }

    private static void saveProjectFile(ProjectDef projectDef, Path directory) throws IOException {
        final ProjectFile projectFile = new ProjectFile();
        projectFile.References = projectDef.getReferences().keySet().toArray(new String[0]);
        projectFile.Sources = new ProjectSource[]{new ProjectSource()};

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(directory.resolve(projectDef.getFileName()).toFile()))) {
            gson.toJson(projectFile, writer);
        }
    }

    private static void saveNamespace(NamespaceDef namespaceDef, Path directory) throws IOException {
        Path currentDirectory = directory;
        for (String component : namespaceDef.getNamespace().split("\\.")) {
            currentDirectory = currentDirectory.resolve(component);
            Files.createDirectories(currentDirectory);
        }

        for (FileDef fileDef : namespaceDef.getFiles().values()) {
            final CodeWriter writer = new CodeWriter(4);
            writer.writeLine(String.format("namespace %s;", namespaceDef.getNamespace()));

            if (fileDef.getImports().size() > 0) {
                writer.newLine();
                for (ImportDef importDef : fileDef.getImports()) {
                    switch (importDef.getKind()) {
                        case NAMESPACE:
                            writer.writeLine(String.format("import %s.*;", ((ImportNamespaceDef) importDef).getTarget().getNamespace()));
                            break;

                        case TYPE:
                            ImportTypeDef importTypeDef = (ImportTypeDef) importDef;
                            writer.writeLine(importTypeDef.getAlias() == null
                                ? String.format("import %s.%s;", importTypeDef.getTarget().getNamespace().getNamespace(), importTypeDef.getTarget().getName())
                                : String.format("import %s.%s as %s;", importTypeDef.getTarget().getNamespace().getNamespace(), importTypeDef.getTarget().getName(), importTypeDef.getAlias()));
                            break;

                        default:
                            throw new IllegalArgumentException("Type of import directive is unknown: " + importDef.getKind());
                    }
                }
            }

            if (fileDef.getOptions().size() > 0) {
                writer.newLine();
                for (NameValuePair option : fileDef.getOptions())
                    writer.writeLine(String.format("option %s = %s;", option.getName(), option.getValue().toString()));
            }

            saveTypeScope(fileDef, writer);

            try (OutputStreamWriter stream = new OutputStreamWriter(new FileOutputStream(currentDirectory.resolve(fileDef.getFileName()).toFile()))) {
                writer.saveTo(stream);
            }
        }
    }

    private static void saveTypeScope(TypeScope scope, CodeWriter writer) {
        for (DecoratorDef decoratorDef : scope.getDefinedDecorators()) {
            writer.newLine();
            saveDecorator(decoratorDef, writer);
        }
        for (EnumerationDef enumerationDef : scope.getDefinedEnumerations()) {
            writer.newLine();
            saveEnumeration(enumerationDef, writer);
        }
        for (InterfaceDef interfaceDef : scope.getDefinedInterfaces()) {
            writer.newLine();
            saveInterface(interfaceDef, writer);
        }
        for (ClassDef classDef : scope.getDefinedClasses()) {
            writer.newLine();
            saveClass(classDef, writer);
        }
    }

    private static void saveDecorator(DecoratorDef decoratorDef, CodeWriter writer) {
        saveComments(decoratorDef, writer);
        saveDecorators(decoratorDef, writer);

        writer.write(String.format("decorator %s {", decoratorDef.getName()));
        writer.indent();

        if (decoratorDef.getDefinedConstants().size() > 0) {
            writer.newLine();
            saveConstants(decoratorDef, writer);
        }

        saveTypeScope(decoratorDef, writer);

        if (decoratorDef.getProperties().size() > 0) {
            writer.newLine();
            for (int i = 0; i < decoratorDef.getProperties().size(); i += 1) {
                final DecoratorPropertyDef propertyDef = decoratorDef.getProperties().get(i);
                saveComments(propertyDef, writer);
                saveDecorators(propertyDef, writer);

                if (propertyDef.getDefault() == null)
                    writer.writeLine(String.format("%s %s;", propertyDef.getType().toString(), propertyDef.getName()));
                else
                    writer.writeLine(String.format("%s %s = %s;", propertyDef.getType().toString(), propertyDef.getName(), propertyDef.getDefault().toString()));
                if (i + 1 < decoratorDef.getProperties().size())
                    writer.newLine();
            }
        }

        writer.dedent();
        writer.writeLine("}");
    }

    private static String luminaryTypeName(IntegralType type) {
        switch (type) {
            case INT8:
                return "Int8";

            case UINT8:
                return "Int8";

            case INT16:
                return "Int8";

            case UINT16:
                return "Int8";

            case INT32:
                return "Int8";

            case UINT32:
                return "Int8";

            case INT64:
                return "Int8";

            case UINT64:
                return "Int8";

            default:
                throw new IllegalArgumentException("Integral type '" + type + "' is unknown.");
        }
    }

    private static void saveEnumeration(EnumerationDef enumerationDef, CodeWriter writer) {
        saveComments(enumerationDef, writer);
        saveDecorators(enumerationDef, writer);

        writer.write(String.format("enum %s : %s {", enumerationDef.getName(), luminaryTypeName(enumerationDef.getUnderlyingType())));
        writer.indent();

        if (enumerationDef.getMembers().size() > 0) {
            writer.newLine();
            for (int i = 0; i < enumerationDef.getMembers().size(); i += 1) {
                final EnumerationMemberDef memberDef = enumerationDef.getMembers().get(i);
                saveComments(memberDef, writer);
                saveDecorators(memberDef, writer);

                writer.writeLine(String.format("%s = %s;", memberDef.getName(), memberDef.getValue().toString()));
                if (i + 1 < enumerationDef.getMembers().size())
                    writer.newLine();
            }
        }

        writer.dedent();
        writer.writeLine("}");
    }

    private static void saveInterface(InterfaceDef interfaceDef, CodeWriter writer) {
        saveComments(interfaceDef, writer);
        saveDecorators(interfaceDef, writer);

        writer.write(String.format("interface %s ", interfaceDef.getName()));
        if (interfaceDef.getSuperInterfaces().size() > 0) {
            writer.write(": " + interfaceDef.getSuperInterfaces().get(0).getName());
            for (int i = 1; i < interfaceDef.getSuperInterfaces().size(); i += 1)
                writer.write(", " + interfaceDef.getSuperInterfaces().get(i).getName());
        }
        writer.write(" {");
        writer.indent();

        saveTypeScope(interfaceDef, writer);

        if (interfaceDef.getProperties().size() > 0) {
            writer.newLine();
            for (int i = 0; i < interfaceDef.getProperties().size(); i += 1) {
                final InterfacePropertyDef propertyDef = interfaceDef.getProperties().get(i);
                saveComments(propertyDef, writer);
                saveDecorators(propertyDef, writer);

                writer.write(String.format("%s %s", propertyDef.getType().toString(), propertyDef.getName()));
                if (propertyDef.isReadable())
                    writer.write(" get");
                if (propertyDef.isWritable())
                    writer.write(" set");
                writer.writeLine(";");
                if (i + 1 < interfaceDef.getProperties().size())
                    writer.newLine();
            }
        }

        writer.dedent();
        writer.writeLine("}");
    }

    private static void saveClass(ClassDef classDef, CodeWriter writer) {
        saveComments(classDef, writer);
        saveDecorators(classDef, writer);

        writer.write(String.format(classDef.isFinal() ? "final class" : "class %s", classDef.getName()));
        if (classDef.getSuperClass() != null || classDef.getSuperInterfaces().size() > 0) {
            boolean isFirst = true;
            if (classDef.getSuperClass() != null) {
                isFirst = false;
                writer.write(String.format(": %s", classDef.getSuperClass().getName()));
            }
            for (InterfaceDef superInterfaceDef : classDef.getSuperInterfaces()) {
                if (isFirst) {
                    isFirst = false;
                    writer.write(String.format(": %s", superInterfaceDef.getName()));
                } else
                    writer.write(String.format(", %s", superInterfaceDef.getName()));
            }
        }
        writer.write(" {");
        writer.indent();

        if (classDef.getDefinedConstants().size() > 0) {
            writer.newLine();
            saveConstants(classDef, writer);
        }

        saveTypeScope(classDef, writer);

        if (classDef.getProperties().size() > 0) {
            writer.newLine();
            for (int i = 0; i < classDef.getProperties().size(); i += 1) {
                final ClassPropertyDef propertyDef = classDef.getProperties().get(i);
                saveComments(propertyDef, writer);
                saveDecorators(propertyDef, writer);

                final String prefix = propertyDef.isFinal()
                    ? (propertyDef.isOverride() ? "final override " : "final ")
                    : (propertyDef.isOverride() ? "override " : "");
                if (propertyDef.getDefault() == null)
                    writer.writeLine(String.format("%s%s %s;", prefix, propertyDef.getType().toString(), propertyDef.getName()));
                else
                    writer.writeLine(String.format("%s%s %s = %s;", prefix, propertyDef.getType().toString(), propertyDef.getName(), propertyDef.getDefault().toString()));
                if (i + 1 < classDef.getProperties().size())
                    writer.newLine();
            }
        }

        writer.dedent();
        writer.writeLine("}");
    }

    private static void saveComments(ItemDef commentable, CodeWriter writer) {
        for (String comment : commentable.getComments())
            writer.writeLine("/// " + comment);
    }

    private static void saveDecorators(ItemDef decoratable, CodeWriter writer) {
        for (Decorator decorator : decoratable.getDecorators()) {
            final DecoratorDef decoratorDef = decorator.getDefinition();
            writer.write(String.format("[%s", decoratorDef.getName()));

            if (decorator.getArguments().size() > 0 && decoratorDef.getProperties().size() > 0) {
                writer.write("(");

                boolean isFirst = true;
                for (int i = 0; i < decorator.getArguments().size(); i += 1) {
                    final DecoratorPropertyDef propertyDef = decorator.getArguments().get(i).getDefinition();
                    if (!decorator.getArguments().get(i).getValue().equals(propertyDef.getDefault())) {
                        if (!isFirst)
                            writer.write(", ");
                        else
                            isFirst = false;

                        writer.write(String.format("%s = %s", propertyDef.getName(), decorator.getArguments().get(i).getValue()));
                    }
                }

                writer.write(")");
            }

            writer.writeLine("]");
        }
    }

    private static void saveConstants(ConstantScope constantScope, CodeWriter writer) {
        for (int i = 0; i < constantScope.getDefinedConstants().size(); i += 1) {
            final ConstantDef constantDef = constantScope.getDefinedConstants().get(i);
            saveComments(constantDef, writer);
            saveDecorators(constantDef, writer);
            writer.writeLine(String.format("const %s %s = %s;", constantDef.getType().toString(), constantDef.getName(), constantDef.getValue().toString()));
            if (i + 1 < constantScope.getDefinedConstants().size())
                writer.newLine();
        }
    }

    public static void main(String[] args) throws IOException {
        final ProjectLoader loader = new ProjectLoader();
        System.setProperty("luminary.search.path", "C:\\Projects\\MaxiMarkets\\ProtocolGenerators\\luminary");
        final ProjectDef projectDef = loader.load("C:\\Projects\\MaxiMarkets\\TradingServer\\luminary\\External\\External.json");
        ProjectSaver.save(projectDef, "D:\\Temporary\\Luminary");
    }
}
