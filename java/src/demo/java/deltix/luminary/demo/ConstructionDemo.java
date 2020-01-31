package deltix.luminary.demo;

import deltix.luminary.*;

import java.io.IOException;

public class ConstructionDemo {
    private static ProjectDef defineProject() {
        final ProjectDef projectDef = new ProjectDef(null, "Test.json");
        defineNamespace(projectDef);
        return projectDef;
    }

    private static void defineNamespace(ProjectDef projectDef) {
        final NamespaceDef namespaceDef = new NamespaceDef(projectDef, "Test");
        defineFile(namespaceDef, "A");
        defineFile(namespaceDef, "B");
    }

    private static void defineFile(NamespaceDef namespaceDef, String fileName) {
        final FileDef fileDef = new FileDef(namespaceDef, fileName + ".lux");
        defineTypes(fileDef, fileName);
    }

    private static void defineTypes(FileDef fileDef, String fileName) {
        ClassDef classDef = new ClassDef(fileDef, "Class" + fileName, false);
        ClassPropertyDef classPropertyDef = new ClassPropertyDef(classDef, "Test", TypeInt64.INSTANCE, new LiteralInt64(42L), false, false);
        ConstantDef classConstantDef = new ConstantDef(classDef, "ID", TypeText.INSTANCE, new LiteralText("<N/A>"));

        InterfaceDef interfaceDef = new InterfaceDef(fileDef, "Interface" + fileName);
        InterfacePropertyDef interfacePropertyDef = new InterfacePropertyDef(interfaceDef, "Test", TypeText.INSTANCE, false,true, true);

        EnumerationDef enumerationDef = new EnumerationDef(fileDef, "Enumeration" + fileName, IntegralType.INT32);
        EnumerationMemberDef enumerationMemberDef1 = new EnumerationMemberDef(enumerationDef, "A", new LiteralInt32(1));
        EnumerationMemberDef enumerationMemberDef2 = new EnumerationMemberDef(enumerationDef, "A", new LiteralInt32(2));

        DecoratorDef decoratorDef = new DecoratorDef(fileDef, "Decorator" + fileName);
        DecoratorPropertyDef decoratorPropertyDef = new DecoratorPropertyDef(decoratorDef, "Value", TypeInt64.INSTANCE, new LiteralInt64(42));
    }

    public static void main(String[] args) throws IOException {
        final ProjectDef projectDef = defineProject();
        ProjectSaver.save(projectDef, "D:\\Temporary\\Luminary");

        final ProjectLoader loader = new ProjectLoader();
        final ProjectDef projectDefLoaded = loader.load("D:\\Temporary\\Luminary\\Test.json");
    }
}
