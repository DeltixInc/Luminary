using System;
using System.IO;
using System.Linq;
using Newtonsoft.Json;

namespace Deltix.Luminary
{
	public static class ProjectSaver
	{
		/// <summary>
		/// Saves the Luminary project (its entire content including the *.lux files) into given directory.
		/// </summary>
		/// <param name="projectDef"></param>
		/// <param name="directory"></param>
		public static void Save(ProjectDef projectDef, String directory)
		{
			if (projectDef.FileName == null)
				throw new InvalidOperationException("Built-in projects cannot be exported.");
			if (!Directory.Exists(directory))
				throw new InvalidOperationException("Directory does not exist.");

			SaveProjectFile(projectDef, directory);
			foreach (NamespaceDef namespaceDef in projectDef.Namespaces.Values)
				SaveNamespace(namespaceDef, directory);
		}

		private static void SaveProjectFile(ProjectDef projectDef, String directory)
		{
			ProjectFile projectFile = new ProjectFile
			{
				References = projectDef.References.Select(x => x.Key).ToArray(),
				Sources = new[] { new ProjectSource() }
			};

			using (StreamWriter stream = new StreamWriter(new FileStream(Path.Combine(directory, projectDef.FileName), FileMode.Create, FileAccess.ReadWrite)))
				stream.WriteLine(JsonConvert.SerializeObject(projectFile, Formatting.Indented));
		}

		private static void SaveNamespace(NamespaceDef namespaceDef, String directory)
		{
			String currentDirectory = directory;
			foreach (String component in namespaceDef.Namespace.Split('.'))
				currentDirectory = Directory.CreateDirectory(Path.Combine(currentDirectory, component)).FullName;

			foreach (FileDef fileDef in namespaceDef.Files.Values)
			{
				CodeWriter writer = new CodeWriter(4);
				writer.WriteLine($"namespace {namespaceDef.Namespace};");

				if (fileDef.Imports.Count > 0)
				{
					writer.NewLine();
					foreach (ImportDef importDef in fileDef.Imports)
					{
						switch (importDef.Kind)
						{
							case ImportKind.Namespace:
								writer.WriteLine($"import {((ImportNamespaceDef)importDef).Target}.*;");
								break;

							case ImportKind.Type:
								ImportTypeDef importTypeDef = (ImportTypeDef)importDef;
								writer.WriteLine(importTypeDef.Alias == null
									? $"import {importTypeDef.Target.Namespace.Namespace}.{importTypeDef.Target.Name};"
									: $"import {importTypeDef.Target.Namespace.Namespace}.{importTypeDef.Target.Name} as {importTypeDef.Alias};");
								break;

							default:
								throw new ArgumentOutOfRangeException();
						}
					}
				}

				if (fileDef.Options.Count > 0)
				{
					writer.NewLine();
					foreach (NameValuePair option in fileDef.Options)
						writer.WriteLine($"option {option.Name} = {option.Value};");
				}

				SaveTypeScope(fileDef, writer);

				using (StreamWriter stream = new StreamWriter(new FileStream(Path.Combine(currentDirectory, fileDef.FileName), FileMode.Create, FileAccess.ReadWrite)))
					writer.SaveTo(stream);
			}
		}

		private static void SaveTypeScope(ITypeScope scope, CodeWriter writer)
		{
			foreach (DecoratorDef decoratorDef in scope.DefinedDecorators)
			{
				writer.NewLine();
				SaveDecorator(decoratorDef, writer);
			}
			foreach (EnumerationDef enumerationDef in scope.DefinedEnumerations)
			{
				writer.NewLine();
				SaveEnumeration(enumerationDef, writer);
			}
			foreach (InterfaceDef interfaceDef in scope.DefinedInterfaces)
			{
				writer.NewLine();
				SaveInterface(interfaceDef, writer);
			}
			foreach (ClassDef decortorDef in scope.DefinedClasses)
			{
				writer.NewLine();
				SaveClass(decortorDef, writer);
			}
		}

		private static void SaveDecorator(DecoratorDef decoratorDef, CodeWriter writer)
		{
			SaveComments(decoratorDef, writer);
			SaveDecorators(decoratorDef, writer);

			writer.Write("decorator {0} {{", decoratorDef.Name);
			writer.Indent();

			if (decoratorDef.DefinedConstants.Count > 0)
			{
				writer.NewLine();
				SaveConstants(decoratorDef, writer);
			}

			SaveTypeScope(decoratorDef, writer);

			if (decoratorDef.Properties.Count > 0)
			{
				writer.NewLine();
				for (Int32 index = 0; index < decoratorDef.Properties.Count; index += 1)
				{
					DecoratorPropertyDef propertyDef = decoratorDef.Properties[index];
					SaveComments(propertyDef, writer);
					SaveDecorators(propertyDef, writer);

					if (propertyDef.Default == null)
						writer.WriteLine("{0} {1};", propertyDef.Type, propertyDef.Name);
					else
						writer.WriteLine("{0} {1} = {2};", propertyDef.Type, propertyDef.Name, propertyDef.Default);
					if (index + 1 < decoratorDef.Properties.Count)
						writer.NewLine();
				}
			}

			writer.Dedent();
			writer.WriteLine("}");
		}

		private static void SaveEnumeration(EnumerationDef enumerationDef, CodeWriter writer)
		{
			SaveComments(enumerationDef, writer);
			SaveDecorators(enumerationDef, writer);

			writer.Write("enum {0} : {1} {{", enumerationDef.Name, enumerationDef.UnderlyingType);
			writer.Indent();

			if (enumerationDef.Members.Count > 0)
			{
				writer.NewLine();
				for (Int32 index = 0; index < enumerationDef.Members.Count; index += 1)
				{
					EnumerationMemberDef memberDef = enumerationDef.Members[index];
					SaveComments(memberDef, writer);
					SaveDecorators(memberDef, writer);

					writer.WriteLine("{0} = {1};", memberDef.Name, memberDef.Value);
					if (index + 1 < enumerationDef.Members.Count)
						writer.NewLine();
				}
			}

			writer.Dedent();
			writer.WriteLine("}");
		}

		private static void SaveInterface(InterfaceDef interfaceDef, CodeWriter writer)
		{
			SaveComments(interfaceDef, writer);
			SaveDecorators(interfaceDef, writer);

			writer.Write("interface {0} ", interfaceDef.Name);
			if (interfaceDef.SuperInterfaces.Count > 0)
			{
				writer.Write(": {0}", interfaceDef.SuperInterfaces[0].Name);
				foreach (InterfaceDef superInterfaceDef in interfaceDef.SuperInterfaces.Skip(1))
					writer.Write(", {0}", superInterfaceDef.Name);
			}
			writer.Write(" {");
			writer.Indent();

			SaveTypeScope(interfaceDef, writer);

			if (interfaceDef.Properties.Count > 0)
			{
				writer.NewLine();
				for (Int32 index = 0; index < interfaceDef.Properties.Count; index += 1)
				{
					InterfacePropertyDef propertyDef = interfaceDef.Properties[index];
					SaveComments(propertyDef, writer);
					SaveDecorators(propertyDef, writer);

					if (propertyDef.IsOverride)
						writer.Write("override ");
					writer.Write("{0} {1}", propertyDef.Type, propertyDef.Name);
					if (propertyDef.IsReadable)
						writer.Write(" get");
					if (propertyDef.IsWritable)
						writer.Write(" set");
					writer.WriteLine(";");
					if (index + 1 < interfaceDef.Properties.Count)
						writer.NewLine();
				}
			}

			writer.Dedent();
			writer.WriteLine("}");
		}

		private static void SaveClass(ClassDef classDef, CodeWriter writer)
		{
			SaveComments(classDef, writer);
			SaveDecorators(classDef, writer);

			writer.Write(classDef.IsFinal ? "final class {0}" : "class {0}", classDef.Name);
			if (classDef.SuperClass != null || classDef.SuperInterfaces.Count > 0)
			{
				Boolean isFirst = true;
				if (classDef.SuperClass != null)
				{
					isFirst = false;
					writer.Write(": {0}", classDef.SuperClass.Name);
				}
				foreach (InterfaceDef superInterfaceDef in classDef.SuperInterfaces)
				{
					if (isFirst)
					{
						isFirst = false;
						writer.Write(": {0}", superInterfaceDef.Name);
					}
					else
						writer.Write(", {0}", superInterfaceDef.Name);
				}
			}
			writer.Write(" {");
			writer.Indent();

			if (classDef.DefinedConstants.Count > 0)
			{
				writer.NewLine();
				SaveConstants(classDef, writer);
			}

			SaveTypeScope(classDef, writer);

			if (classDef.Properties.Count > 0)
			{
				writer.NewLine();
				for (Int32 index = 0; index < classDef.Properties.Count; index += 1)
				{
					ClassPropertyDef propertyDef = classDef.Properties[index];
					SaveComments(propertyDef, writer);
					SaveDecorators(propertyDef, writer);

					String prefix = propertyDef.IsFinal
						? (propertyDef.IsOverride ? "final override " : "final ")
						: (propertyDef.IsOverride ? "override ": "");
					if (propertyDef.Default == null)
						writer.WriteLine("{0}{1} {2};", prefix, propertyDef.Type, propertyDef.Name);
					else
						writer.WriteLine("{0}{1} {2} = {3};", prefix, propertyDef.Type, propertyDef.Name, propertyDef.Default);
					if (index + 1 < classDef.Properties.Count)
						writer.NewLine();
				}
			}

			writer.Dedent();
			writer.WriteLine("}");
		}

		private static void SaveComments(ICommentable commentable, CodeWriter writer)
		{
			foreach (String comment in commentable.Comments)
				writer.WriteLine("/// {0}", comment);
		}

		private static void SaveDecorators(IDecoratable decoratable, CodeWriter writer)
		{
			foreach (Decorator decorator in decoratable.Decorators)
			{
				DecoratorDef decoratorDef = decorator.Definition;
				writer.Write("[{0}", decoratorDef.Name);

				if (decorator.Arguments.Count > 0 && decoratorDef.Properties.Count > 0)
				{
					writer.Write("(");

					Boolean isFirst = true;
					for (Int32 i = 0; i < decorator.Arguments.Count; i += 1)
					{
						DecoratorPropertyDef propertyDef = decorator.Arguments[i].Definition;
						if (!decorator.Arguments[i].Value.Equals(propertyDef.Default))
						{
							if (!isFirst)
								writer.Write(", ");
							else
								isFirst = false;

							writer.Write("{0} = {1}", propertyDef.Name, decorator.Arguments[i].Value);
						}
					}

					writer.Write(")");
				}
				writer.WriteLine("]");
			}
		}

		private static void SaveConstants(IConstantScope constantScope, CodeWriter writer)
		{
			for (Int32 index = 0; index < constantScope.DefinedConstants.Count; index += 1)
			{
				ConstantDef constantDef = constantScope.DefinedConstants[index];
				SaveComments(constantDef, writer);
				SaveDecorators(constantDef, writer);
				writer.WriteLine("const {0} {1} = {2};", constantDef.Type, constantDef.Name, constantDef.Value);
				if (index + 1 < constantScope.DefinedConstants.Count)
					writer.NewLine();
			}
		}
	}
}
