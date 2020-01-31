using System;

namespace Deltix.Luminary.Demo
{
	class ConstructionDemo
	{
		private static ProjectDef DefineProject()
		{
			ProjectDef projectDef = new ProjectDef(null, "Test.json");
			DefineNamespace(projectDef);
			return projectDef;
		}

		private static void DefineNamespace(ProjectDef projectDef)
		{
			NamespaceDef namespaceDef = new NamespaceDef(projectDef, "Test");
			DefineFile(namespaceDef, "A");
			DefineFile(namespaceDef, "B");
		}

		private static void DefineFile(NamespaceDef namespaceDef, String fileName)
		{
			FileDef fileDef = new FileDef(namespaceDef, $"{fileName}.lux");
			DefineTypes(fileDef, fileName);
		}

		private static void DefineTypes(FileDef fileDef, String fileName)
		{
			ClassDef classDef = new ClassDef(fileDef, $"Class{fileName}");
			ClassPropertyDef classPropertyDef = new ClassPropertyDef(classDef, "Test", TypeInt64.Instance, new LiteralInt64(42));
			ConstantDef classConstantDef = new ConstantDef(classDef, "ID", TypeText.Instance, new LiteralText("<N/A>"));

			InterfaceDef interfaceDef = new InterfaceDef(fileDef, $"Interface{fileName}");
			InterfacePropertyDef interfacePropertyDef = new InterfacePropertyDef(interfaceDef, "Test", TypeText.Instance, false, true, true);

			EnumerationDef enumerationDef = new EnumerationDef(fileDef, $"Enumeration{fileName}", IntegralType.Int32);
			EnumerationMemberDef enumerationMemberDef1 = new EnumerationMemberDef(enumerationDef, "A", new LiteralInt32(1));
			EnumerationMemberDef enumerationMemberDef2 = new EnumerationMemberDef(enumerationDef, "A", new LiteralInt32(2));

			DecoratorDef decoratorDef = new DecoratorDef(fileDef, $"Decorator{fileName}");
			DecoratorPropertyDef decoratorPropertyDef = new DecoratorPropertyDef(decoratorDef, "Value", TypeInt64.Instance, new LiteralInt64(42));
		}

		static void Main(String[] args)
		{
			ProjectDef projectDef = DefineProject();
			ProjectSaver.Save(projectDef, "D:/Temporary/Luminary");

			ProjectLoader loader = new ProjectLoader();
			ProjectDef projectDefLoaded = loader.Load(@"D:/Temporary/Luminary/Test.json");
		}
	}
}
