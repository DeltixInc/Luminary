using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Diagnostics.CodeAnalysis;
using System.IO;
using System.Linq;
using Antlr4.Runtime;
using Antlr4.Runtime.Misc;
using Antlr4.Runtime.Tree;
using Deltix.Luminary.Implementation;
using JetBrains.Annotations;
using Newtonsoft.Json;

namespace Deltix.Luminary
{
	public static class Ext
	{
		public delegate TOutput Converter<in TInput, out TOutput>(TInput input);

		public static List<TOutput> ConvertAll<TInput, TOutput>(this List<TInput> list, Converter<TInput, TOutput> converter)
		{
			if (list == null)
				throw new ArgumentNullException(nameof(list));
			if (converter == null)
				throw new ArgumentNullException(nameof(converter));

			List<TOutput> newList = new List<TOutput>(list.Count);
			for (Int32 i = 0; i < list.Count; i += 1)
				newList.Add(converter(list[i]));
			return newList;
		}
	}

	public class ProjectLoader
	{
		private static readonly DecoratorDef DecoratorUsageDef;

		private readonly String _currentDirectory;
		private readonly HashSet<String> _parsedProjects = new HashSet<String>();
		private readonly Dictionary<String, ProjectDef> _loadedProjects = new Dictionary<String, ProjectDef>();

		[SuppressMessage("ReSharper", "ObjectCreationAsStatement")]
		static ProjectLoader()
		{
			ProjectDef builtinProject = new ProjectDef(null, null);
			NamespaceDef builtinNamespace = new NamespaceDef(builtinProject, "");
			FileDef builtinFile = new FileDef(builtinNamespace, "");

			EnumerationDef decoratorTargetDef = new EnumerationDef(builtinFile, "DecoratorTarget", IntegralType.Int32);
			new EnumerationMemberDef(decoratorTargetDef, "ENUMERATION", new LiteralInt32(1));
			new EnumerationMemberDef(decoratorTargetDef, "ENUMERATION_MEMBER", new LiteralInt32(2));
			new EnumerationMemberDef(decoratorTargetDef, "INTERFACE", new LiteralInt32(3));
			new EnumerationMemberDef(decoratorTargetDef, "INTERFACE_PROPERTY", new LiteralInt32(4));
			new EnumerationMemberDef(decoratorTargetDef, "CLASS", new LiteralInt32(5));
			new EnumerationMemberDef(decoratorTargetDef, "CLASS_PROPERTY", new LiteralInt32(6));
			new EnumerationMemberDef(decoratorTargetDef, "DECORATOR", new LiteralInt32(7));
			new EnumerationMemberDef(decoratorTargetDef, "DECORATOR_PROPERTY", new LiteralInt32(8));

			DecoratorUsageDef = new DecoratorDef(builtinFile, "DecoratorUsage");

			LiteralList defaultValidOn = new LiteralList();
			foreach (EnumerationMemberDef enumerationMemberDef in decoratorTargetDef.Members)
				defaultValidOn.Value.Add(new LiteralEnumerationValue(enumerationMemberDef));
			// ReSharper disable once ObjectCreationAsStatement
			new DecoratorPropertyDef(DecoratorUsageDef, "ValidOn", new TypeList(decoratorTargetDef.Type), defaultValidOn);
			// ReSharper disable once ObjectCreationAsStatement
			new DecoratorPropertyDef(DecoratorUsageDef, "Repeatable", TypeBoolean.Instance, LiteralBoolean.False);

			PredefinedTypes.Add(decoratorTargetDef.Name, decoratorTargetDef.Type);
			PredefinedTypes.Add(DecoratorUsageDef.Name, DecoratorUsageDef.Type);
		}

		public ProjectLoader()
		{
			_currentDirectory = Directory.GetCurrentDirectory();
		}

		public ProjectDef Load(String path)
		{
			return Load(_currentDirectory, path);
		}

		public ProjectDef Load(String workingDirectory, String path)
		{
			return Load(workingDirectory, path, 0);
		}

		private ProjectDef Load(String workingDirectory, String path, Int32 level)
		{
			// Normalize path to the project file. Thus we will be able to detect when two projects import the same project
			// by different relative paths.
			String projectPath = Locate(workingDirectory, path);
			if (projectPath == null)
				throw new InvalidOperationException($"Project file '{path}' is not found.");

			// Project has already been loaded (and thus the projects imported by it).
			ProjectDef projectDef;
			if (_loadedProjects.TryGetValue(projectPath, out projectDef))
				return projectDef;

			// First parse the project file and attempt to load imported projects recursively.
			ProjectFile projectFile = LoadProjectFile(projectPath, level);
			projectFile.Path = projectPath;
			String projectDirectory = Path.GetDirectoryName(projectPath);
			Dictionary<String, ProjectDef> references = new Dictionary<String, ProjectDef>();
			foreach (String reference in projectFile.References)
				references.Add(reference, Load(projectDirectory, reference, level + 1));

			// When all dependencies have been loaded correctly, we can analyze the project itself.
			projectDef = LoadProject(projectFile, references, level);
			_loadedProjects.Add(projectPath, projectDef);
			return projectDef;
		}

		private String Locate(String workingDirectory, String path)
		{
			String normalizedPath;
			if (Path.IsPathRooted(path))
			{
				normalizedPath = NormalizePath(path);
				return File.Exists(normalizedPath) ? normalizedPath : null;
			}

			normalizedPath = NormalizePath(workingDirectory, path);
			if (File.Exists(normalizedPath))
				return normalizedPath;

			String searchPath = Environment.GetEnvironmentVariable("LUMINARY_SEARCH_PATH");
			if (searchPath == null)
				return null;

			foreach (String searchEntry in searchPath.Split(';'))
			{
				String fullPath = Path.Combine(searchEntry, path);
				if (File.Exists(fullPath))
					return NormalizePath(workingDirectory, fullPath);
			}

			return null;
		}

		private ProjectFile LoadProjectFile(String path, Int32 level)
		{
			Console.WriteLine($"{String.Concat(Enumerable.Repeat("    ", level))}Loading project file '{path}'...");
			if (_parsedProjects.Contains(path))
				throw new InvalidOperationException(
					$"Project '{path}' is already parsed but not analyzed. This indicates a cycle of imports.");

			ProjectFile projectFile;
			try
			{
				projectFile = ParseProjectFile(path);
			}
			catch (Exception exception)
			{
				throw new InvalidOperationException($"{path}: Failed to parse JSON.", exception);
			}

			_parsedProjects.Add(path);
			return projectFile;
		}

		private ProjectDef LoadProject(ProjectFile projectFile, Dictionary<String, ProjectDef> references, Int32 level)
		{
			FileInfo projectFileInfo = new FileInfo(projectFile.Path);
			ProjectDef projectDef = new ProjectDef(projectFileInfo.DirectoryName, projectFileInfo.Name);
			foreach (KeyValuePair<String, ProjectDef> reference in references)
				projectDef.References.Add(reference.Key, reference.Value);

			Dictionary<String, FileNode> files = new Dictionary<String, FileNode>();
			foreach (Tuple<String, String> fileInfo in EnumerateSources(projectFile))
			{
				String path = Path.Combine(fileInfo.Item1, fileInfo.Item2);
				Console.WriteLine(
					$"{String.Concat(Enumerable.Repeat("    ", level + 1))}Loading source file '{fileInfo.Item2}'...");

				FileNode fileNode;
				try
				{
					fileNode = ParseSourceFile(path);
				}
				catch (Exception exception)
				{
					throw new InvalidOperationException($"File {path}: failed to parse Luminary.", exception);
				}

				// Make sure that file's relative path matches with the namespace.
				String directory = Path.GetDirectoryName(fileInfo.Item2);
				if (directory == null)
					throw new InvalidOperationException(
						$"File '{path}' is placed within the root directory. Empty namespaces are not allowed.");
				if (!directory.Replace(Path.DirectorySeparatorChar, '.').Equals(fileNode.Namespace))
					throw new InvalidOperationException(
						$"File '{path}' has invalid namespace: '{directory.Replace(Path.DirectorySeparatorChar, '.')}' is expected.");

				files.Add(path, fileNode);
			}

			// Accumulate types and constants defined within the project.

			foreach (KeyValuePair<String, FileNode> file in files)
			{
				NamespaceDef namespaceDef;
				if (!projectDef.Namespaces.TryGetValue(file.Value.Namespace, out namespaceDef))
					namespaceDef = new NamespaceDef(projectDef, file.Value.Namespace);

				FileDef fileDef = new FileDef(namespaceDef, new FileInfo(file.Key).Name);
				fileDef.Options.AddRange(file.Value.Options);

				CollectTypes(fileDef, file.Value, fileDef.Namespace);
			}

			// Resolve imports.

			foreach (KeyValuePair<String, FileNode> file in files)
			{
				NamespaceDef namespaceDef = projectDef.Namespaces[file.Value.Namespace];
				FileDef fileDef = namespaceDef.Files[new FileInfo(file.Key).Name];

				for (Int32 i = 0; i < file.Value.Imports.Count; i += 1)
				{
					ImportNode importNode = file.Value.Imports[i];

					List<NamespaceDef> namespaceCandidates = new List<NamespaceDef>();
					NamespaceDef importedNamespaceDef;
					if (projectDef.Namespaces.TryGetValue(importNode.Namespace, out importedNamespaceDef))
						namespaceCandidates.Add(importedNamespaceDef);

					foreach (ProjectDef referencedProjectDef in projectDef.References.Values)
						if (referencedProjectDef.Namespaces.TryGetValue(importNode.Namespace, out importedNamespaceDef))
							namespaceCandidates.Add(importedNamespaceDef);

					if (importNode.TypeName == null)
					{
						if (namespaceCandidates.Count == 0)
							throw new Exception(
								$"{fileDef.GetPath()}: namespace '{importNode.Namespace}' is not defined within this project or referenced projects.");
						if (namespaceCandidates.Count > 1)
							throw new Exception(
								$"{fileDef.GetPath()}: namespace '{importNode.Namespace}' is defined multiple times in the following projects: {String.Join(",", namespaceCandidates.ConvertAll(x => $"'{x.Project.GetPath()}'"))}");

						fileDef.Imports.Add(new ImportNamespaceDef(namespaceCandidates[0]));
						continue;
					}

					List<TypeCustom> typeCandidates = new List<TypeCustom>();
					foreach (NamespaceDef candidate in namespaceCandidates)
					{
						TypeCustom type;
						if (candidate.DefinedTypes.TryGetValue(importNode.TypeName, out type))
							typeCandidates.Add(type);
					}

					if (typeCandidates.Count == 0)
						throw new Exception(
							$"{fileDef.GetPath()}: type '{importNode.Namespace}.{importNode.TypeName}' is not defined within this project or referenced projects.");
					if (typeCandidates.Count > 1)
						throw new Exception(
							$"{fileDef.GetPath()}: type '{importNode.Namespace}.{importNode.TypeName}' is defined multiple times in the following projects: {String.Join(",", typeCandidates.ConvertAll(x => $"'{x.Project.GetPath()}'"))}");

					if (importNode.Alias == null)
					{
						for (Int32 j = i + 1; j < file.Value.Imports.Count; j += 1)
							if (importNode.TypeName.Equals(file.Value.Imports[j].TypeName) && file.Value.Imports[j].Alias == null)
								throw new Exception(
									$"{fileDef.GetPath()}: type '{importNode.TypeName}' imported more than once: '{importNode.Namespace}.{importNode.TypeName}' and '{file.Value.Imports[j].Namespace}.{file.Value.Imports[j].TypeName}'.");
					}
					else
					{
						for (Int32 j = i + 1; j < file.Value.Imports.Count; j += 1)
							if (importNode.Alias.Equals(file.Value.Imports[j].Alias))
								throw new Exception(
									$"{fileDef.GetPath()}: alias '{importNode.Alias}' defined more than once: '{importNode.Namespace}.{importNode.TypeName}' and '{file.Value.Imports[j].Namespace}.{file.Value.Imports[j].TypeName}'.");
					}

					fileDef.Imports.Add(new ImportTypeDef(typeCandidates[0], importNode.Alias));
				}
			}

			// Collect all constants defined within a project.

			foreach (KeyValuePair<String, FileNode> file in files)
			{
				NamespaceDef namespaceDef = projectDef.Namespaces[file.Value.Namespace];
				CollectConstants(namespaceDef.Files[new FileInfo(file.Key).Name], file.Value);
			}

			// Check values of all constants.

			foreach (KeyValuePair<String, FileNode> file in files)
			{
				NamespaceDef namespaceDef = projectDef.Namespaces[file.Value.Namespace];
				CheckConstantValues(namespaceDef.Files[new FileInfo(file.Key).Name]);
			}

			// Check custom type definitions.

			foreach (KeyValuePair<String, FileNode> file in files)
			{
				NamespaceDef namespaceDef = projectDef.Namespaces[file.Value.Namespace];
				CheckEnumerationMembers(namespaceDef.Files[new FileInfo(file.Key).Name]);
			}

			// Check custom type definitions.

			foreach (KeyValuePair<String, FileNode> file in files)
			{
				NamespaceDef namespaceDef = projectDef.Namespaces[file.Value.Namespace];
				BuildTypeDefinitions(namespaceDef.Files[new FileInfo(file.Key).Name], file.Value);
			}

			// Check interface method definitions.

			foreach (KeyValuePair<String, FileNode> file in files)
			{
				NamespaceDef namespaceDef = projectDef.Namespaces[file.Value.Namespace];
				BuildInterfaceMethods(namespaceDef.Files[new FileInfo(file.Key).Name], file.Value);
			}

			// Check decorators.

			foreach (KeyValuePair<String, FileNode> file in files)
			{
				NamespaceDef namespaceDef = projectDef.Namespaces[file.Value.Namespace];
				CheckDecorators(namespaceDef.Files[new FileInfo(file.Key).Name], file.Value);
			}

			// Resolve all unresolved literals.

			foreach (NamespaceDef namespaceDef in projectDef.Namespaces.Values)
				foreach (FileDef fileDef in namespaceDef.Files.Values)
					ResolveLiterals(fileDef);

			// TODO: Check that there is no cycles within inheritance graph.

			foreach (NamespaceDef namespaceDef in projectDef.Namespaces.Values)
				foreach (FileDef fileDef in namespaceDef.Files.Values)
				{
					foreach (ClassDef classDef in fileDef.DefinedClasses)
						CheckInheritance(classDef);
					foreach (InterfaceDef interfaceDef in fileDef.DefinedInterfaces)
						CheckInheritance(interfaceDef);
				}

			return projectDef;
		}

		private void ResolveDecoratorValues(ITypeScope scope, IDecoratable decoratable)
		{
			foreach (Decorator decorator in decoratable.Decorators)
				foreach (DecoratorPropertyValue decoratorPropertyValue in decorator.Arguments)
					decoratorPropertyValue.Value = ResolveLiteral(scope, decoratorPropertyValue.Value);
		}

		private void ResolveConstantValues(IConstantAndTypeContainer scope)
		{
			foreach (ConstantDef constantDef in scope.DefinedConstants)
			{
				constantDef.Value = ResolveLiteral(scope, constantDef.Value);
				ResolveDecoratorValues(scope, constantDef);
			}
		}

		private void ResolveLiterals(ITypeScope scope)
		{
			foreach (ClassDef classDef in scope.DefinedClasses)
			{
				ResolveDecoratorValues(classDef, classDef);
				ResolveConstantValues(classDef);

				foreach (ClassPropertyDef propertyDef in classDef.Properties)
				{
					if (propertyDef.Default != null)
						propertyDef.Default = ResolveLiteral(classDef, propertyDef.Default);
					ResolveDecoratorValues(classDef, propertyDef);
				}

				ResolveLiterals(classDef);
			}

			foreach (DecoratorDef decoratorDef in scope.DefinedDecorators)
			{
				ResolveDecoratorValues(decoratorDef, decoratorDef);
				ResolveConstantValues(decoratorDef);

				foreach (DecoratorPropertyDef propertyDef in decoratorDef.Properties)
				{
					if (propertyDef.Default != null)
						propertyDef.Default = ResolveLiteral(decoratorDef, propertyDef.Default);
					ResolveDecoratorValues(decoratorDef, propertyDef);
				}

				ResolveLiterals(decoratorDef);
			}

			foreach (InterfaceDef interfaceDef in scope.DefinedInterfaces)
			{
				ResolveDecoratorValues(interfaceDef, interfaceDef);
				ResolveLiterals(interfaceDef);

				foreach (InterfacePropertyDef propertyDef in interfaceDef.Properties)
					ResolveDecoratorValues(interfaceDef, propertyDef);
			}

			foreach (EnumerationDef enumerationDef in scope.DefinedEnumerations)
			{
				ResolveDecoratorValues(scope, enumerationDef);
				foreach (EnumerationMemberDef memberDef in enumerationDef.Members)
				{
					memberDef.RawValue = ResolveLiteral(scope, memberDef.RawValue);
					ResolveDecoratorValues(scope, memberDef);
				}
			}
		}

		private static String GetOuterTypeName(ITypeScope scope)
		{
			ClassDef classDef = scope as ClassDef;
			if (classDef != null)
				return classDef.Name;

			InterfaceDef def = scope as InterfaceDef;
			if (def != null)
				return def.Name;

			DecoratorDef decoratorDef = scope as DecoratorDef;
			return decoratorDef?.Name;
		}

		private static void CheckForTypeConflicts(ITypeScope parent, String name)
		{
			if (PredefinedTypes.ContainsKey(name))
				throw new Exception($"{parent.File.GetPath()}: type name '{name}' conflicts with built-in type of the same name.");
			if (parent.DefinedEnumerations.FirstOrDefault(x => x.Name.Equals(name)) != null)
				throw new Exception($"{parent.File.GetPath()}: type name '{name}' is already defined within the current scope.");
			if (parent.DefinedClasses.FirstOrDefault(x => x.Name.Equals(name)) != null)
				throw new Exception($"{parent.File.GetPath()}: type name '{name}' is already defined within the current scope.");
			if (parent.DefinedInterfaces.FirstOrDefault(x => x.Name.Equals(name)) != null)
				throw new Exception($"{parent.File.GetPath()}: type name '{name}' is already defined within the current scope.");
			if (parent.DefinedDecorators.FirstOrDefault(x => x.Name.Equals(name)) != null)
				throw new Exception($"{parent.File.GetPath()}: type name '{name}' is already defined within the current scope.");
			if (name.Equals(GetOuterTypeName(parent)))
				throw new Exception($"{parent.File.GetPath()}: type name '{name}' conflicts with its outer type name.");
		}

		private static void CollectType(NamespaceDef namespaceDef, String fullName, TypeCustom type)
		{
			TypeCustom existingType;
			if (namespaceDef.DefinedTypes.TryGetValue(fullName, out existingType))
			{
				Debug.Assert(existingType.File != null, "existingType.File != null");
				Debug.Assert(type.File != null, "type.File != null");
				throw new Exception(
					$"Namespace '{namespaceDef.Namespace}' contains two different types with name '{fullName}': one in file '{existingType.File.GetPath()}' and another in '{type.File.GetPath()}'");
			}
			namespaceDef.DefinedTypes.Add(fullName, type);
		}

		private void CollectTypes(ITypeScope parent, ITypeContainerNode typeContainerNode, NamespaceDef namespaceDef)
		{
			foreach (EnumerationNode enumerationNode in typeContainerNode.DefinedEnumerations)
			{
				CheckForTypeConflicts(parent, enumerationNode.Name);

				EnumerationDef enumerationDef = new EnumerationDef(parent, enumerationNode.Name,
					ExtractUnderlyingType(enumerationNode.UnderlyingType), enumerationNode.Comments);
				foreach (EnumerationMemberNode enumerationMemberNode in enumerationNode.Members)
				{
					EnumerationMemberDef memberDef =
						new EnumerationMemberDef(enumerationDef, enumerationMemberNode.Name, enumerationMemberNode.Comments)
						{
							RawValue = enumerationMemberNode.Value
						};
				}

				CollectType(namespaceDef, enumerationDef.FullName, enumerationDef.Type);
			}
			foreach (DecoratorNode decoratorNode in typeContainerNode.DefinedDecorators)
			{
				CheckForTypeConflicts(parent, decoratorNode.Name);

				DecoratorDef decoratorDef = new DecoratorDef(parent, decoratorNode.Name, decoratorNode.Comments);

				CollectType(namespaceDef, decoratorDef.FullName, decoratorDef.Type);

				CollectTypes(decoratorDef, decoratorNode, namespaceDef);
			}
			foreach (ClassNode classNode in typeContainerNode.DefinedClasses)
			{
				CheckForTypeConflicts(parent, classNode.Name);

				ClassDef classDef = new ClassDef(parent, classNode.Name, classNode.IsFinal, classNode.Comments);

				CollectType(namespaceDef, classDef.FullName, classDef.Type);

				CollectTypes(classDef, classNode, namespaceDef);
			}
			foreach (InterfaceNode interfaceNode in typeContainerNode.DefinedInterfaces)
			{
				CheckForTypeConflicts(parent, interfaceNode.Name);

				InterfaceDef interfaceDef = new InterfaceDef(parent, interfaceNode.Name, interfaceNode.Comments);

				CollectType(namespaceDef, interfaceDef.FullName, interfaceDef.Type);

				CollectTypes(interfaceDef, interfaceNode, namespaceDef);
			}
		}

		private void CollectConstants(ITypeScope parent, ITypeContainerNode typeContainerNode)
		{
			for (Int32 i = 0; i < parent.DefinedDecorators.Count; i += 1)
			{
				DecoratorDef decoratorDef = parent.DefinedDecorators[i];
				DecoratorNode decoratorNode = typeContainerNode.DefinedDecorators[i];

				foreach (ConstantNode constantNode in decoratorNode.DefinedConstants)
					decoratorDef.DefinedConstants.Add(CheckConstant(constantNode, decoratorDef));

				CollectConstants(decoratorDef, decoratorNode);
			}

			for (Int32 i = 0; i < parent.DefinedClasses.Count; i += 1)
			{
				ClassDef decoratorDef = parent.DefinedClasses[i];
				ClassNode decoratorNode = typeContainerNode.DefinedClasses[i];

				foreach (ConstantNode constantNode in decoratorNode.DefinedConstants)
					decoratorDef.DefinedConstants.Add(CheckConstant(constantNode, decoratorDef));

				CollectConstants(decoratorDef, decoratorNode);
			}

			for (Int32 i = 0; i < parent.DefinedInterfaces.Count; i += 1)
			{
				InterfaceDef decoratorDef = parent.DefinedInterfaces[i];
				InterfaceNode decoratorNode = typeContainerNode.DefinedInterfaces[i];

				CollectConstants(decoratorDef, decoratorNode);
			}
		}

		private void CheckEnumerationMembers(ITypeScope scope)
		{
			foreach (EnumerationDef enumerationDef in scope.DefinedEnumerations)
			{
				foreach (EnumerationMemberDef enumerationMemberDef in enumerationDef.Members)
				{
					Literal rawValue = ResolveLiteral(scope, enumerationMemberDef.RawValue);
					while (rawValue is LiteralConstant)
						rawValue = ((LiteralConstant)rawValue).Value.Value;
					LiteralInteger integralValue = rawValue as LiteralInteger;

					if (integralValue == null)
						throw new Exception(
							$"{scope.File.GetPath()}: enumeration member '{enumerationMemberDef.FullName}' cannot be initialized with value '{rawValue}'.");
					enumerationMemberDef.Value = integralValue.CastTo(enumerationDef.UnderlyingType);
				}
			}

			foreach (ClassDef classDef in scope.DefinedClasses)
				CheckEnumerationMembers(classDef);

			foreach (DecoratorDef decoratorDef in scope.DefinedDecorators)
				CheckEnumerationMembers(decoratorDef);

			foreach (InterfaceDef interfaceDef in scope.DefinedInterfaces)
				CheckEnumerationMembers(interfaceDef);
		}

		private void CheckConstantValues(ITypeScope scopeDef)
		{
			foreach (DecoratorDef decoratorDef in scopeDef.DefinedDecorators)
			{
				foreach (ConstantDef constantDef in decoratorDef.DefinedConstants)
				{
					Literal rawValue = ResolveLiteral(decoratorDef, constantDef.Value);
					Literal value = CheckLiteral(rawValue, constantDef.Type);
					if (value == null)
						throw new InvalidOperationException(
							$"{scopeDef.File.GetPath()}: constant '{constantDef.FullName}' of type '{constantDef.Type}' cannot be initialized with value '{constantDef.Value}'.");

					constantDef.Value = value;
				}
			}

			foreach (ClassDef classDef in scopeDef.DefinedClasses)
			{
				foreach (ConstantDef constantDef in classDef.DefinedConstants)
				{
					Literal rawValue = ResolveLiteral(classDef, constantDef.Value);
					Literal value = CheckLiteral(rawValue, constantDef.Type);
					if (value == null)
						throw new InvalidOperationException(
							$"{scopeDef.File.GetPath()}: constant '{constantDef.FullName}' of type '{constantDef.Type}' cannot be initialized with value '{constantDef.Value}'.");

					constantDef.Value = value;
				}
			}
		}

		private ConstantDef CheckConstant(ConstantNode constantNode, IConstantAndTypeContainer ownerDef)
		{
			Type type = ResolveType(ownerDef, constantNode.Type);
			if (type == null)
				throw new Exception($"{ownerDef.File.GetPath()}: type '{constantNode.Type}' is not defined.");

			return new ConstantDef(ownerDef, constantNode.Name, type, constantNode.Value, constantNode.Comments);
		}

		[JetBrains.Annotations.NotNull]
		private static HashSet<TypeCustom> TryResolveCustomType([JetBrains.Annotations.NotNull] ITypeScope scope, [JetBrains.Annotations.NotNull] String name)
		{
			return name.Contains(".")
				? TryResolveCustomTypeByQualifiedName(scope, name)
				: TryResolveCustomTypeByName(scope, name);
		}

		[JetBrains.Annotations.NotNull]
		private static HashSet<TypeCustom> TryResolveCustomTypeByName([JetBrains.Annotations.NotNull] ITypeScope scope, [JetBrains.Annotations.NotNull] String name)
		{
			if (name.Contains("."))
				throw new InvalidOperationException("Type name cannot be qualified at this point.");

			HashSet<TypeCustom> candidates = new HashSet<TypeCustom>();
			FileDef fileDef = scope.File;

			// Lookup definition in the current and all parent scopes.

			for (ITypeScope currentScope = scope; currentScope != null; currentScope = currentScope.Parent)
			{
				foreach (ClassDef classDef in currentScope.DefinedClasses)
					if (classDef.Name.Equals(name))
						candidates.Add(classDef.Type);
				foreach (InterfaceDef interfaceDef in currentScope.DefinedInterfaces)
					if (interfaceDef.Name.Equals(name))
						candidates.Add(interfaceDef.Type);
				foreach (DecoratorDef decoratorDef in currentScope.DefinedDecorators)
					if (decoratorDef.Name.Equals(name))
						candidates.Add(decoratorDef.Type);
				foreach (EnumerationDef enumerationDef in currentScope.DefinedEnumerations)
					if (enumerationDef.Name.Equals(name))
						candidates.Add(enumerationDef.Type);
				if (candidates.Count > 0)
					return candidates;
			}

			// Lookup type definition in the same namespace.

			NamespaceDef namespaceDef = fileDef.Namespace;
			TypeCustom type;
			if (namespaceDef.DefinedTypes.TryGetValue(name, out type))
			{
				candidates.Add(type);
				return candidates;
			}

			// Prefer aliased types over regular imports.

			foreach (ImportDef importDef in fileDef.Imports)
			{
				if (importDef.Kind == ImportKind.Type)
				{
					ImportTypeDef importTypeDef = (ImportTypeDef) importDef;
					if (name.Equals(importTypeDef.Alias))
						candidates.Add(importTypeDef.Target);
				}
			}
			if (candidates.Count > 0)
				return candidates;

			// Other import directives.

			foreach (ImportDef importDef in fileDef.Imports)
			{
				if (importDef.Kind == ImportKind.Type)
				{
					ImportTypeDef importTypeDef = (ImportTypeDef)importDef;
					if (name.Equals(importTypeDef.Target.Name) && importTypeDef.Alias == null)
						candidates.Add(importTypeDef.Target);
				}
				else
				{
					ImportNamespaceDef importNamespaceDef = (ImportNamespaceDef)importDef;
					if (importNamespaceDef.Target.DefinedTypes.TryGetValue(name, out type))
						candidates.Add(type);
				}
			}

			return candidates;
		}

		[JetBrains.Annotations.NotNull]
		private static HashSet<TypeCustom> TryResolveCustomTypeByQualifiedName([JetBrains.Annotations.NotNull] ITypeScope scope, [JetBrains.Annotations.NotNull] String qualifiedName)
		{
			if (!qualifiedName.Contains("."))
				throw new InvalidOperationException("Type name must be qualified at this point.");

			HashSet<TypeCustom> candidates = new HashSet<TypeCustom>();
			Int32 i = qualifiedName.LastIndexOf('.');
			String outerName = qualifiedName.Substring(0, i);
			String typeName = qualifiedName.Substring(i + 1);

			// Case 1: `outerName` is the name of the type itself.

			HashSet<TypeCustom> parentCandidates = TryResolveCustomType(scope, outerName);
			foreach (TypeCustom parent in parentCandidates)
			{
				ITypeScope parentScope;
				if (parent.Kind == TypeKind.Class)
					parentScope = ((TypeClass) parent).Definition;
				else if (parent.Kind == TypeKind.Decorator)
					parentScope = ((TypeDecorator) parent).Definition;
				else if (parent.Kind == TypeKind.Interface)
					parentScope = ((TypeInterface) parent).Definition;
				else
					continue;

				foreach (ClassDef classDef in parentScope.DefinedClasses)
					if (classDef.Name.Equals(typeName))
						candidates.Add(classDef.Type);
				foreach (InterfaceDef interfaceDef in parentScope.DefinedInterfaces)
					if (interfaceDef.Name.Equals(typeName))
						candidates.Add(interfaceDef.Type);
				foreach (DecoratorDef decoratorDef in parentScope.DefinedDecorators)
					if (decoratorDef.Name.Equals(typeName))
						candidates.Add(decoratorDef.Type);
				foreach (EnumerationDef enumerationDef in parentScope.DefinedEnumerations)
					if (enumerationDef.Name.Equals(typeName))
						candidates.Add(enumerationDef.Type);
			}

			// Case 2: `outerName` is the namespace in current project and/or referenced projects.

			ProjectDef projectDef = scope.File.Namespace.Project;
			TypeCustom type = TryLookUpType(projectDef, outerName, typeName);
			if (type != null)
				candidates.Add(type);
			foreach (ProjectDef referencedProjectDef in projectDef.References.Values)
			{
				type = TryLookUpType(referencedProjectDef, outerName, typeName);
				if (type != null)
					candidates.Add(type);
			}

			return candidates;
		}

		[CanBeNull]
		private static TypeCustom TryLookUpType(ProjectDef projectDef, String @namespace, String name)
		{
			NamespaceDef namespaceDef;
			TypeCustom type;
			return projectDef.Namespaces.TryGetValue(@namespace, out namespaceDef)
				? (namespaceDef.DefinedTypes.TryGetValue(name, out type) ? type : null)
				: null;
		}

		[JetBrains.Annotations.NotNull]
		private static TypeCustom ResolveCustomType([JetBrains.Annotations.NotNull] ITypeScope scope, [JetBrains.Annotations.NotNull] String name)
		{
			HashSet<TypeCustom> candidates = TryResolveCustomType(scope, name);

			if (candidates.Count == 0)
				throw new Exception($"{scope.File.GetPath()}: type '{name}' is undefined.");
			if (candidates.Count > 1)
				throw new Exception($"{scope.File.GetPath()}: type '{name}' is ambiguous: {String.Join(",", candidates.ToList().ConvertAll(x => $"'{x.Project.GetPath()}'"))}");

			return candidates.ToList()[0];
		}

		[JetBrains.Annotations.NotNull]
		private List<Type> ResolveTypeList([JetBrains.Annotations.NotNull] ITypeScope scope, [JetBrains.Annotations.NotNull] String name)
		{
			Int32 index = 0;
			Int32 angles = 0;

			while (index < name.Length)
			{
				Char c = name[index];
				if (c == '<')
					angles += 1;
				else if (c == '>')
					angles -= 1;
				else if (c == ',')
					if (angles == 0)
						break;
				index += 1;
			}

			Type type = ResolveType(scope, name.Substring(0, index));
			if (index >= name.Length)
				return new List<Type> { type };

			List<Type> remainingTypes = ResolveTypeList(scope, name.Substring(index + 1));
			remainingTypes.Insert(0, type);
			return remainingTypes;
		}

		[JetBrains.Annotations.NotNull]
		private Type ResolveType([JetBrains.Annotations.NotNull] ITypeScope scope, [JetBrains.Annotations.NotNull] String name)
		{
			if (name.EndsWith("?"))
				return new TypeNullable(ResolveType(scope, name.Substring(0, name.Length - 1)));

			if (name.EndsWith(">"))
			{
				Int32 index = name.IndexOf("<", StringComparison.Ordinal);
				String genericType = name.Substring(0, index);
				String typeParameters = name.Substring(index + 1, name.Length - index - 2);

				List<Type> typeList = ResolveTypeList(scope, typeParameters);
				if (genericType.Equals("List"))
				{
					if (typeList.Count != 1)
						throw new InvalidOperationException("List must have exactly one generic parameter.");
					return new TypeList(typeList[0]);
				}
				if (genericType.Equals("Map"))
				{
					if (typeList.Count != 2)
						throw new InvalidOperationException("Map must have exactly two generic parameter.");
					return new TypeMap(typeList[0], typeList[1]);
				}
				if (genericType.Equals("Action"))
				{
					return new TypeAction(typeList);
				}
				if (genericType.Equals("Function"))
				{
					if (typeList.Count < 1)
						throw new InvalidOperationException("Function has have at least one generic parameter.");
					Type returnType = typeList[typeList.Count - 1];
					typeList.RemoveAt(typeList.Count - 1);
					return new TypeFunction(typeList, returnType);
				}
				throw new InvalidOperationException($"{scope.File.GetPath()}: generic type {genericType} is undefined.");
			}

			if (!name.Contains("."))
			{
				Type type;
				if (PredefinedTypes.TryGetValue(name, out type))
					return type;

				if (name.Equals("Action"))
					return new TypeAction(new List<Type>());
			}

			return ResolveCustomType(scope, name);
		}

		private void BuildTypeDefinitions(ITypeScope scopeDef, ITypeContainerNode scopeNode)
		{
			for (Int32 i = 0; i < scopeNode.DefinedDecorators.Count; i += 1)
				CheckDecoratorDefinition(scopeDef.DefinedDecorators[i], scopeNode.DefinedDecorators[i]);
			for (Int32 i = 0; i < scopeNode.DefinedClasses.Count; i += 1)
				CheckClassDefinition(scopeDef.DefinedClasses[i], scopeNode.DefinedClasses[i]);
			for (Int32 i = 0; i < scopeNode.DefinedInterfaces.Count; i += 1)
				CheckInterfaceDefinition(scopeDef.DefinedInterfaces[i], scopeNode.DefinedInterfaces[i]);
		}

		private void DoBuildInterfaceMethods(InterfaceDef interfaceDef, InterfaceNode interfaceNode)
		{
			foreach (InterfaceMethodNode interfaceMethodNode in interfaceNode.Methods)
			{
				Type returnType = null;
				if (interfaceMethodNode.ReturnType != null)
					returnType = ResolveType(interfaceDef, interfaceMethodNode.ReturnType);
				InterfaceMethodDef interfaceMethodDef = new InterfaceMethodDef(interfaceMethodNode.Name, returnType,
					interfaceDef, interfaceNode.Comments);

				foreach (FormalParameterNode formalParameterNode in interfaceMethodNode.FormalParameters)
				{
					Type type = ResolveType(interfaceDef, formalParameterNode.Type);
					FormalParameterDef formalParameterDef = new FormalParameterDef(interfaceMethodDef, formalParameterNode.Name, type,
						formalParameterNode.IsParameterArray);
					formalParameterDef.Comments.AddRange(formalParameterNode.Comments);
					FormalParameterDef duplicate =
						interfaceMethodDef.Parameters.FirstOrDefault(x => x.Name.Equals(formalParameterDef.Name));
					if (duplicate != null)
						throw new InvalidOperationException(
							$"{interfaceDef.File.GetPath()}: interface method '{interfaceMethodDef.FullName}' has two formal parameters with the same name '{formalParameterDef.Name}'.");
					interfaceMethodDef.Parameters.Add(formalParameterDef);
				}

				interfaceDef.Methods.Add(interfaceMethodDef);
			}
		}

		private void BuildInterfaceMethods(ITypeScope scopeDef, ITypeContainerNode scopeNode)
		{
			for (Int32 i = 0; i < scopeNode.DefinedDecorators.Count; i += 1)
				BuildInterfaceMethods(scopeDef.DefinedDecorators[i], scopeNode.DefinedDecorators[i]);
			for (Int32 i = 0; i < scopeNode.DefinedClasses.Count; i += 1)
				BuildInterfaceMethods(scopeDef.DefinedClasses[i], scopeNode.DefinedClasses[i]);
			for (Int32 i = 0; i < scopeNode.DefinedInterfaces.Count; i += 1)
			{
				DoBuildInterfaceMethods(scopeDef.DefinedInterfaces[i], scopeNode.DefinedInterfaces[i]);
				BuildInterfaceMethods(scopeDef.DefinedInterfaces[i], scopeNode.DefinedInterfaces[i]);
			}
		}

		private void CheckDecoratorPropertyType(Type type)
		{
			// TODO: Implement this.
		}

		private void CheckDecoratorDefinition(DecoratorDef decoratorDef, DecoratorNode decoratorNode)
		{
			foreach (DecoratorPropertyNode propertyNode in decoratorNode.Properties)
			{
				Type propertyType = ResolveType(decoratorDef, propertyNode.Type);
				CheckDecoratorPropertyType(propertyType);

				Literal defaultValue = null;
				if (propertyNode.Default != null)
				{
					Literal rawDefaultValue = ResolveLiteral(decoratorDef, propertyNode.Default);
					defaultValue = CheckLiteral(rawDefaultValue, propertyType);
					if (defaultValue == null)
						throw new InvalidOperationException(
							$"{decoratorDef.File.GetPath()}: field '{decoratorNode.Name}.{propertyNode.Name}' cannot have default value '{propertyNode.Default}'.");
				}

				DecoratorPropertyDef propertyDef =
					new DecoratorPropertyDef(decoratorDef, propertyNode.Name, propertyType, defaultValue, propertyNode.Comments);
			}

			BuildTypeDefinitions(decoratorDef, decoratorNode);
		}

		private void CheckInterfaceDefinition(InterfaceDef interfaceDef, InterfaceNode interfaceNode)
		{
			if (interfaceNode.Supertypes != null)
			{
				foreach (String supertypeName in interfaceNode.Supertypes.GroupBy(item => item).Select(group => group.First()))
				{
					Type supertype = ResolveType(interfaceDef, supertypeName);
					if (supertype.Kind != TypeKind.Interface)
						throw new InvalidOperationException(
							$"{interfaceDef.File.GetPath()}: interface '{interfaceNode.Name}' cannot be derived from a message.");

					interfaceDef.SuperInterfaces.Add(((TypeInterface)supertype).Definition);
				}
			}

			foreach (InterfacePropertyNode interfacePropertyNode in interfaceNode.Properties)
			{
				Type type = ResolveType(interfaceDef, interfacePropertyNode.Type);

				InterfacePropertyDef propertyDef = new InterfacePropertyDef(interfaceDef, interfacePropertyNode.Name, type,
					interfacePropertyNode.IsOverride, interfacePropertyNode.IsGettable, interfacePropertyNode.IsSettable,
					interfacePropertyNode.Comments);
			}

			BuildTypeDefinitions(interfaceDef, interfaceNode);
		}

		private void CheckClassDefinition(ClassDef classDef, ClassNode classNode)
		{
			if (classNode.Supertypes != null)
			{
				foreach (String supertypeName in classNode.Supertypes.GroupBy(item => item).Select(group => group.First()))
				{
					Type type = ResolveType(classDef, supertypeName);
					if (type.Kind != TypeKind.Class && type.Kind != TypeKind.Interface)
						throw new InvalidOperationException(
							$"{classDef.File.GetPath()}: supertype '{supertypeName}' must be a class or an interface.");

					if (type.Kind == TypeKind.Class)
					{
						if (classDef.SuperClass != null)
							throw new InvalidOperationException(
								$"{classDef.File.GetPath()}: class '{classNode.Name}' cannot have more than one superclass.");
						ClassDef superClass = ((TypeClass) type).Definition;
						if (superClass.IsFinal)
							throw new InvalidOperationException($"{classDef.File.GetPath()}: cannot inherit class '{classDef.FullName}' from final '{superClass.FullName}'");
						classDef.SuperClass = superClass;
					}
					else
						classDef.SuperInterfaces.Add(((TypeInterface)type).Definition);
				}
			}

			foreach (ClassPropertyNode propertyNode in classNode.Properties)
			{
				Type propertyType = ResolveType(classDef, propertyNode.Type);

				Literal defaultValue = null;
				if (propertyNode.Default != null)
				{
					Literal rawDefaultValue = ResolveLiteral(classDef, propertyNode.Default);
					defaultValue = CheckLiteral(rawDefaultValue, propertyType);
					if (defaultValue == null)
						throw new InvalidOperationException(
							$"{classDef.File.GetPath()}: field '{classNode.Name}.{propertyNode.Name}' cannot have default value '{propertyNode.Default}'.");
				}

				ClassPropertyDef propertyDef = new ClassPropertyDef(classDef, propertyNode.Name, propertyType, defaultValue,
					propertyNode.IsOverride, propertyNode.IsFinal, propertyNode.Comments);
			}

			BuildTypeDefinitions(classDef, classNode);
		}

		private Literal ResolveMemberReference(ITypeScope scope, String text)
		{
			Int32 index = text.LastIndexOf('.');

			String memberName;
			Type type;

			if (index > 0)
			{
				String typeName = text.Substring(0, index);
				type = ResolveType(scope, typeName);
				memberName = text.Substring(index + 1);
			}
			else
			{
				if (scope is ClassDef)
					type = ((ClassDef)scope).Type;
				else if (scope is InterfaceDef)
					type = ((InterfaceDef)scope).Type;
				else if (scope is DecoratorDef)
					type = ((DecoratorDef)scope).Type;
				else
					throw new InvalidOperationException("Something went completely wrong.");
				memberName = text;
			}

			IConstantScope constantScope = null;
			switch (type.Kind)
			{
				case TypeKind.Enumeration:
					{
						EnumerationDef enumerationDef = ((TypeEnumeration)type).Definition;
						EnumerationMemberDef memberDef = enumerationDef.Members.FirstOrDefault(x => x.Name.Equals(memberName));
						if (memberDef == null)
							throw new InvalidOperationException(
								$"{scope.File.GetPath()}: enumeration '{type}' does not contain member with name '{memberName}'.");

						return new LiteralEnumerationValue(memberDef);
					}

				case TypeKind.Class:
					constantScope = ((TypeClass)type).Definition;
					break;

				case TypeKind.Decorator:
					constantScope = ((TypeDecorator)type).Definition;
					break;

				case TypeKind.Int8:
					constantScope = Int8Def.Instance;
					break;

				case TypeKind.Int16:
					constantScope = Int16Def.Instance;
					break;

				case TypeKind.Int32:
					constantScope = Int32Def.Instance;
					break;

				case TypeKind.Int64:
					constantScope = Int64Def.Instance;
					break;

				case TypeKind.UInt8:
					constantScope = UInt8Def.Instance;
					break;

				case TypeKind.UInt16:
					constantScope = UInt16Def.Instance;
					break;

				case TypeKind.UInt32:
					constantScope = UInt32Def.Instance;
					break;

				case TypeKind.UInt64:
					constantScope = UInt64Def.Instance;
					break;

				case TypeKind.Float32:
					constantScope = Float32Def.Instance;
					break;

				case TypeKind.Float64:
					constantScope = Float64Def.Instance;
					break;

				case TypeKind.Decimal:
					constantScope = DecimalDef.Instance;
					break;

				case TypeKind.Timestamp:
					constantScope = TimestampDef.Instance;
					break;

				case TypeKind.Duration:
					constantScope = DurationDef.Instance;
					break;

				case TypeKind.Date:
					constantScope = DateDef.Instance;
					break;

				case TypeKind.Time:
					constantScope = TimeDef.Instance;
					break;

				case TypeKind.UUID:
					constantScope = UUIDDef.Instance;
					break;
			}

			if (constantScope != null)
			{
				ConstantDef constantDef = constantScope.DefinedConstants.FirstOrDefault(x => x.Name.Equals(memberName));
				if (constantDef == null)
					throw new InvalidOperationException(
						$"{scope.File.GetPath()}: type {type} does not contain constant with name '{memberName}'.");
				return new LiteralConstant(constantDef);
			}

			throw new InvalidOperationException(
				$"{scope.File.GetPath()}: type '{type}' does not provide any constant definitions.");
		}

		private LiteralType ResolveTypeReference(ITypeScope scope, String text)
		{
			Type type = ResolveType(scope, text);
			if (type == null)
				throw new InvalidOperationException($"{scope.File.GetPath()}: type '{text}' is not defined.");

			return new LiteralType(type);
		}

		private Literal ResolveLiteral(ITypeScope scope, Literal literal)
		{
			LiteralUnresolved unresolved = literal as LiteralUnresolved;
			if (unresolved != null)
				return ResolveUnresolvedLiteral(scope, unresolved);

			LiteralList list = literal as LiteralList;
			if (list != null)
				for (Int32 i = 0; i < list.Value.Count; i += 1)
					list.Value[i] = ResolveLiteral(scope, list.Value[i]);

			return literal;
		}

		private Literal ResolveUnresolvedLiteral(ITypeScope scope, LiteralUnresolved literal)
		{
			switch (literal.Kind)
			{
				case LiteralKind.EnumerationValue:
				case LiteralKind.Constant:
					return ResolveMemberReference(scope, literal.Value);

				case LiteralKind.Type:
					return ResolveTypeReference(scope, literal.Value);

				default:
					throw new InvalidOperationException("Kind of unresolved literal is unknown.");
			}
		}

		private Boolean DoCheckLiteral(LiteralConstant literal, Type expectedType, Queue<LiteralConstant> visited)
		{
			if (visited.Contains(literal))
				return false;
			visited.Enqueue(literal);

			Literal value = literal.Value.Value;
			LiteralConstant constant = value as LiteralConstant;
			if (constant != null)
				return DoCheckLiteral(constant, expectedType, visited);

			return CheckLiteral(value, expectedType) != null;
		}

		private Literal CheckLiteral(Literal literal, Type expectedType)
		{
			LiteralConstant constant = literal as LiteralConstant;
			if (constant != null)
				return !DoCheckLiteral(constant, expectedType, new Queue<LiteralConstant>()) ? null : literal;

			switch (expectedType.Kind)
			{
				case TypeKind.Type:
					return literal.Kind == LiteralKind.Type ? literal : null;

				case TypeKind.Boolean:
					return literal.Kind == LiteralKind.Boolean ? literal : null;

				case TypeKind.Int8:
				case TypeKind.UInt8:
				case TypeKind.Int16:
				case TypeKind.UInt16:
				case TypeKind.Int32:
				case TypeKind.UInt32:
				case TypeKind.Int64:
				case TypeKind.UInt64:
					return (literal as LiteralInteger)?.CastTo(expectedType.Kind.ToIntegralType());

				case TypeKind.Float32:
					return literal.Kind == LiteralKind.Float32 ? literal : null;

				case TypeKind.Float64:
					return literal.Kind == LiteralKind.Float64 ? literal : null;

				case TypeKind.Decimal:
					return literal.Kind == LiteralKind.Decimal ? literal : null;

				case TypeKind.Timestamp:
					return literal.Kind == LiteralKind.Timestamp ? literal : null;

				case TypeKind.Duration:
					return literal.Kind == LiteralKind.Duration ? literal : null;

				case TypeKind.Date:
					return literal.Kind == LiteralKind.Date ? literal : null;

				case TypeKind.Time:
					return literal.Kind == LiteralKind.Time ? literal : null;

				case TypeKind.UUID:
					return literal.Kind == LiteralKind.UUID ? literal : null;

				case TypeKind.Text:
					return literal.Kind == LiteralKind.Text ? literal : null;

				case TypeKind.Nullable:
					return literal.Kind == LiteralKind.Null
						? literal
						: CheckLiteral(literal, ((TypeNullable)expectedType).UnderlyingType);

				case TypeKind.Enumeration:
					if (literal.Kind == LiteralKind.EnumerationValue)
					{
						LiteralEnumerationValue typedValue = (LiteralEnumerationValue)literal;
						return typedValue.Value.Owner.Equals(((TypeEnumeration)expectedType).Definition) ? literal : null;
					}
					return null;

				case TypeKind.List:
					if (literal.Kind == LiteralKind.List)
					{
						TypeList listType = (TypeList)expectedType;
						LiteralList listLiteral = (LiteralList)literal;
						if (listLiteral.Value.Count == 0)
							return literal;

						LiteralList result = new LiteralList();
						foreach (Literal value in listLiteral.Value)
						{
							Literal item = CheckLiteral(value, listType.UnderlyingType);
							if (item == null)
								return null;

							result.Value.Add(item);
						}

						return result;
					}
					return null;

				default:
					return null;
			}
		}

		private void CheckDecorators(ITypeScope scope, IDecoratable decoratable, List<DecoratorValueNode> decorators,
			DecoratorTarget target, String name)
		{
			foreach (DecoratorValueNode decoratorValueNode in decorators)
			{
				Type decoratorType = ResolveType(scope, decoratorValueNode.Name);
				if (decoratorType.Kind != TypeKind.Decorator)
					throw new InvalidOperationException(
						$"{scope.File.GetPath()}: error while processing '{name}': decorator '{decoratorValueNode.Name}' is not defined.");
				DecoratorDef decoratorDef = ((TypeDecorator)decoratorType).Definition;

				DecoratorUsage decoratorUsage = RetrieveDecoratorUsage(decoratorDef);
				if (!decoratorUsage.ValidOn.Contains(target))
					throw new InvalidOperationException(
						$"{scope.File.GetPath()}: error while processing '{name}': decorator '{decoratorDef.FullName}' cannot be applied to {target}.");
				if (!decoratorUsage.Repeatable && decoratable.Decorators.FirstOrDefault(x => x.Definition == decoratorDef) != null)
					throw new InvalidOperationException(
						$"{scope.File.GetPath()}: error while processing '{name}': decorator '{decoratorDef.FullName}' cannot be applied more than once.");

				List<DecoratorPropertyValue> arguments = new List<DecoratorPropertyValue>();
				foreach (DecoratorPropertyDef propertyDef in decoratorDef.Properties)
				{
					NameValuePair argument = decoratorValueNode.Arguments.FirstOrDefault(x => x.Name.Equals(propertyDef.Name));
					if (argument == null && propertyDef.Default == null)
						throw new InvalidOperationException(
							$"{scope.File.GetPath()}: error while processing '{name}': decorator argument '{propertyDef.FullName}' is not set.");

					Literal value = propertyDef.Default;
					if (argument != null)
					{
						value = CheckLiteral(ResolveLiteral(scope, argument.Value), propertyDef.Type);
						if (value == null)
							throw new InvalidOperationException(
								$"{scope.File.GetPath()}: error while processing '{name}': cannot cast value '{argument.Value}' to type '{propertyDef.Type}' for decorator property '{decoratorDef.Name}.{propertyDef.Name}'.");
					}

					arguments.Add(new DecoratorPropertyValue(propertyDef, value, argument == null));
				}
				foreach (NameValuePair argument in decoratorValueNode.Arguments)
					if (null == decoratorDef.Properties.FirstOrDefault(x => x.Name.Equals(argument.Name)))
						throw new InvalidOperationException(
							$"{scope.File.GetPath()}: error while processing '{name}': decorator '{decoratorDef.Name}' does not contain property with name '{argument.Name}'.");
				decoratable.Decorators.Add(new Decorator(decoratorDef, arguments));
			}
		}

		private void CheckDecorators(ITypeScope scope, ITypeContainerNode scopeNode)
		{
			for (Int32 i = 0; i < scopeNode.DefinedDecorators.Count; i += 1)
				DoCheckDecorators(scope, scope.DefinedDecorators[i], scopeNode.DefinedDecorators[i]);
			for (Int32 i = 0; i < scopeNode.DefinedClasses.Count; i += 1)
				DoCheckDecorators(scope, scope.DefinedClasses[i], scopeNode.DefinedClasses[i]);
			for (Int32 i = 0; i < scopeNode.DefinedInterfaces.Count; i += 1)
				DoCheckDecorators(scope, scope.DefinedInterfaces[i], scopeNode.DefinedInterfaces[i]);
			for (Int32 i = 0; i < scopeNode.DefinedEnumerations.Count; i += 1)
				DoCheckDecorators(scope, scope.DefinedEnumerations[i], scopeNode.DefinedEnumerations[i]);
		}

		private void DoCheckDecorators(ITypeScope scope, EnumerationDef enumerationDef, EnumerationNode enumerationNode)
		{
			CheckDecorators(scope, enumerationDef, enumerationNode.Decorators, DecoratorTarget.Enumeration,
				enumerationDef.FullName);

			for (Int32 i = 0; i < enumerationDef.Members.Count; i += 1)
				CheckDecorators(scope, enumerationDef.Members[i], enumerationNode.Members[i].Decorators,
					DecoratorTarget.EnumerationMember, enumerationDef.Members[i].FullName);
		}

		private void DoCheckDecorators(ITypeScope scope, DecoratorDef decoratorDef, DecoratorNode decoratorNode)
		{
			CheckDecorators(scope, decoratorDef, decoratorNode.Decorators, DecoratorTarget.Decorator, decoratorDef.FullName);

			for (Int32 i = 0; i < decoratorNode.Properties.Count; i += 1)
				CheckDecorators(decoratorDef, decoratorDef.Properties[i], decoratorNode.Properties[i].Decorators,
					DecoratorTarget.DecoratorProperty, decoratorDef.Properties[i].FullName);

			CheckDecorators(decoratorDef, decoratorNode);
		}

		private void DoCheckDecorators(ITypeScope scope, ClassDef classDef, ClassNode classNode)
		{
			CheckDecorators(scope, classDef, classNode.Decorators, DecoratorTarget.Class, classDef.FullName);

			for (Int32 i = 0; i < classNode.Properties.Count; i += 1)
				CheckDecorators(classDef, classDef.Properties[i], classNode.Properties[i].Decorators, DecoratorTarget.ClassProperty,
					classDef.Properties[i].FullName);

			CheckDecorators(classDef, classNode);
		}

		private void DoCheckDecorators(ITypeScope scope, InterfaceDef interfaceDef, InterfaceNode interfaceNode)
		{
			CheckDecorators(scope, interfaceDef, interfaceNode.Decorators, DecoratorTarget.Interface, interfaceDef.FullName);

			for (Int32 i = 0; i < interfaceNode.Properties.Count; i += 1)
				CheckDecorators(interfaceDef, interfaceDef.Properties[i], interfaceNode.Properties[i].Decorators,
					DecoratorTarget.InterfaceProperty, interfaceDef.Properties[i].FullName);

			for (Int32 i = 0; i < interfaceNode.Methods.Count; i += 1)
			{
				InterfaceMethodDef interfaceMethodDef = interfaceDef.Methods[i];
				InterfaceMethodNode interfaceMethodNode = interfaceNode.Methods[i];
				CheckDecorators(interfaceDef, interfaceMethodDef, interfaceMethodNode.Decorators,
					DecoratorTarget.InterfaceMethod, interfaceMethodDef.FullName);

				for (Int32 j = 0; j < interfaceMethodDef.Parameters.Count; j += 1)
					CheckDecorators(interfaceDef, interfaceMethodDef.Parameters[j],
						interfaceMethodNode.FormalParameters[j].Decorators, DecoratorTarget.FormalParameter,
						interfaceMethodDef.Parameters[j].Name);
			}

			CheckDecorators(interfaceDef, interfaceNode);
		}

		private static void AccumulateInheritedProperties(String name, Dictionary<String, InterfacePropertyDef> properties,
			InterfaceDef interfaceDef)
		{
			foreach (InterfaceDef super in interfaceDef.SuperInterfaces)
				AccumulateInheritedProperties(name, properties, super);
			foreach (InterfacePropertyDef propertyDef in interfaceDef.Properties)
			{
				InterfacePropertyDef inheritedPropertyDef;
				if (properties.TryGetValue(propertyDef.Name, out inheritedPropertyDef))
				{
					if (!inheritedPropertyDef.Type.Equals(propertyDef.Type))
						throw new InvalidOperationException(
							$"Type {name} inherited property with name {propertyDef.Name} twice: one from {propertyDef.Owner.Name} and one from {inheritedPropertyDef.Owner.Name}.");
					if (inheritedPropertyDef.IsReadable != propertyDef.IsReadable ||
						inheritedPropertyDef.IsWritable != propertyDef.IsWritable)
					{
						// TODO: Decide what to do with different profile of getters and setters.
					}
				}
				properties[propertyDef.Name] = propertyDef;
			}
		}

		private static void CheckInheritance(InterfaceDef interfaceDef)
		{
			Dictionary<String, InterfacePropertyDef> interfaceProperties = new Dictionary<String, InterfacePropertyDef>();
			foreach (InterfaceDef super in interfaceDef.SuperInterfaces)
				AccumulateInheritedProperties(interfaceDef.Name, interfaceProperties, super);

			foreach (InterfacePropertyDef propertyDef in interfaceDef.Properties)
			{
				InterfacePropertyDef inheritedPropertyDef;
				if (interfaceProperties.TryGetValue(propertyDef.Name, out inheritedPropertyDef))
				{
					if (!propertyDef.IsOverride)
						throw new InvalidOperationException($"'{propertyDef.FullName}' hides inherited property {inheritedPropertyDef.FullName}");
				}
				else
				{
					if (propertyDef.IsOverride)
						throw new InvalidOperationException($"'{propertyDef.FullName}': no suitable property found to override.");
				}
			}
		}

		private static void AccumulateInheritedProperties(String name, Dictionary<String, ClassPropertyDef> properties,
			ClassDef classDef)
		{
			if (classDef.SuperClass != null)
				AccumulateInheritedProperties(name, properties, classDef.SuperClass);
			foreach (ClassPropertyDef propertyDef in classDef.Properties)
			{
				ClassPropertyDef inheritedPropertyDef;
				if (properties.TryGetValue(propertyDef.Name, out inheritedPropertyDef))
				{
					if (!propertyDef.IsOverride || !propertyDef.Type.Equals(inheritedPropertyDef.Type))
						throw new InvalidOperationException(
							$"Type {name} inherited property with name {propertyDef.Name} twice: one from {propertyDef.Owner.Name} and one from {inheritedPropertyDef.Owner.Name}.");
					if (inheritedPropertyDef.IsFinal)
						throw new InvalidOperationException($"Type {name} cannot override property {propertyDef.Name} as it is declared as final in the base class {inheritedPropertyDef.Owner.Name}");
				}
				else if (propertyDef.IsOverride)
					throw new InvalidOperationException($"'{propertyDef.FullName}': no suitable property found to override.");
				properties[propertyDef.Name] = propertyDef;
			}
		}


		private static void CheckInterfaceImplementation(String name, Dictionary<String, ClassPropertyDef> classProperties,
			InterfaceDef interfaceDef)
		{
			foreach (InterfacePropertyDef interfacePropertyDef in interfaceDef.Properties)
			{
				ClassPropertyDef classPropertyDef;
				if (!classProperties.TryGetValue(interfacePropertyDef.Name, out classPropertyDef))
					throw new InvalidOperationException(
						$"Class '{name}' does not contains property '{interfacePropertyDef.Name}' inherited from '{interfacePropertyDef.Owner.Name}'");

				if (!classPropertyDef.Type.Equals(interfacePropertyDef.Type))
					throw new InvalidOperationException(
						$"Type of the property '{name}.{classPropertyDef.Name}' differs from the '{interfacePropertyDef.Owner.Name}.{interfacePropertyDef.Name}'");
			}

			foreach (InterfaceDef superInterfaceDef in interfaceDef.SuperInterfaces)
				CheckInterfaceImplementation(name, classProperties, superInterfaceDef);
		}

		private static void CheckInheritance(ClassDef classDef)
		{
			// Check that fields inherited from the super messages do not conflict with ours.

			Dictionary<String, ClassPropertyDef> classProperties = new Dictionary<String, ClassPropertyDef>();
			AccumulateInheritedProperties(classDef.FullName, classProperties, classDef);

			// Check that we have implemented all inherited getters and setters.

			foreach (InterfaceDef superInterfaceDef in classDef.SuperInterfaces)
				CheckInterfaceImplementation(classDef.FullName, classProperties, superInterfaceDef);
		}

		private DecoratorUsage RetrieveDecoratorUsage(DecoratorDef decoratorDef)
		{
			if (decoratorDef == DecoratorUsageDef)
				return DecoratorUsageDecoratorForItself();

			DecoratorUsage usage = null;
			foreach (Decorator decorator in decoratorDef.Decorators)
				if (decorator.Definition == DecoratorUsageDef)
				{
					if (usage != null)
						throw new InvalidOperationException(
							$"Error while processing decorator '{decoratorDef.FullName}': 'DecoratorUsage' cannot be applied more than once.");

					usage = new DecoratorUsage();

					// ReSharper disable once PossibleNullReferenceException
					LiteralList validOn =
						(LiteralList)decorator.Arguments.FirstOrDefault(x => x.Definition.Name.Equals("ValidOn")).Value;
					foreach (Literal item in validOn.Value)
					{
						LiteralEnumerationValue value = (LiteralEnumerationValue)item;
						switch (value.Value.Name)
						{
							case "ENUMERATION":
								usage.ValidOn.Add(DecoratorTarget.Enumeration);
								break;

							case "ENUMERATION_MEMBER":
								usage.ValidOn.Add(DecoratorTarget.EnumerationMember);
								break;

							case "INTERFACE":
								usage.ValidOn.Add(DecoratorTarget.Interface);
								break;

							case "INTERFACE_PROPERTY":
								usage.ValidOn.Add(DecoratorTarget.InterfaceProperty);
								break;

							case "CLASS":
								usage.ValidOn.Add(DecoratorTarget.Class);
								break;

							case "CLASS_PROPERTY":
								usage.ValidOn.Add(DecoratorTarget.ClassProperty);
								break;

							case "DECORATOR":
								usage.ValidOn.Add(DecoratorTarget.Decorator);
								break;

							case "DECORATOR_PROPERTY":
								usage.ValidOn.Add(DecoratorTarget.DecoratorProperty);
								break;

							case "INTERFACE_METHOD":
								usage.ValidOn.Add(DecoratorTarget.InterfaceMethod);
								break;

							case "FORMAL_PARAMETER":
								usage.ValidOn.Add(DecoratorTarget.FormalParameter);
								break;
						}
					}

					// ReSharper disable once PossibleNullReferenceException
					LiteralBoolean repeatable =
						(LiteralBoolean)decorator.Arguments.FirstOrDefault(x => x.Definition.Name.Equals("Repeatable")).Value;
					usage.Repeatable = repeatable.Value;
				}

			return usage ?? DecoratorUsage.Default();
		}

		private IEnumerable<Tuple<String, String>> EnumerateSources(ProjectFile projectFile)
		{
			// TODO: At the moment we just enumerate all *.lux files under root directory.

			foreach (ProjectSource projectSource in projectFile.Sources)
				if (projectSource != null)
				{
					String directory = NormalizePath(Path.GetDirectoryName(projectFile.Path), projectSource.Directory);
					foreach (String file in Directory.EnumerateFiles(directory, "*.lux", SearchOption.AllDirectories))
					{
						// Workaround against strange behavior of Directory.EnumerateFiles. See Remarks section on MSDN.
						if (!file.EndsWith(".lux"))
							continue;

						String path = Path.GetFullPath(new Uri(file).LocalPath);
						String relativePath = GetRelativePath(path, directory);
						yield return new Tuple<String, String>(directory, relativePath);
					}
				}
		}

		private static String NormalizePath(String path)
		{
			return NormalizePath(Directory.GetCurrentDirectory(), path);
		}

		private static String NormalizePath(String workingDirectory, String path)
		{
			if (!Path.IsPathRooted(path))
				path = Path.Combine(workingDirectory, path);
			return NormalizePathInternal(path);
		}

		private static String NormalizePathInternal(String path)
		{
			path = path.Replace('\\', '/');
			return Path.GetFullPath(new Uri(path, UriKind.Absolute).LocalPath).TrimEnd(Path.DirectorySeparatorChar, Path.AltDirectorySeparatorChar);
		}

		private String GetRelativePath(String path, String directory)
		{
			Uri pathUri = new Uri(path);
			if (!directory.EndsWith(Path.DirectorySeparatorChar.ToString()))
				directory += Path.DirectorySeparatorChar;
			Uri folderUri = new Uri(directory);
			return Uri.UnescapeDataString(folderUri.MakeRelativeUri(pathUri).ToString().Replace('/', Path.DirectorySeparatorChar));
		}

		private static ProjectFile ParseProjectFile(String path)
		{
			using (StreamReader stringReader = new StreamReader(new FileStream(path, FileMode.Open, FileAccess.Read)))
			{
				using (JsonTextReader jsonReader = new JsonTextReader(stringReader))
				{
					JsonSerializerSettings settings = new JsonSerializerSettings
					{
						MissingMemberHandling = MissingMemberHandling.Error
					};

					JsonSerializer serializer = JsonSerializer.Create(settings);
					return serializer.Deserialize<ProjectFile>(jsonReader);
				}
			}
		}

		private static IntegralType ExtractUnderlyingType(String text)
		{
			if ("Int64".Equals(text))
				return IntegralType.Int64;
			if ("UInt64".Equals(text))
				return IntegralType.UInt64;
			if ("Int32".Equals(text))
				return IntegralType.Int32;
			if ("UInt32".Equals(text))
				return IntegralType.UInt32;
			if ("Int16".Equals(text))
				return IntegralType.Int16;
			if ("UInt16".Equals(text))
				return IntegralType.UInt16;
			if ("Int8".Equals(text))
				return IntegralType.Int8;
			if ("UInt8".Equals(text))
				return IntegralType.UInt8;
			throw new InvalidOperationException(
				"Type 'Int8', 'Int16', 'Int32', 'Int64', 'UInt8', 'UInt16', 'UInt32' or 'UInt64' expected.");
		}

		public class ThrowingErrorListener : BaseErrorListener, IAntlrErrorListener<Int32>
		{
			public static readonly ThrowingErrorListener Instance = new ThrowingErrorListener();

			public override void SyntaxError(IRecognizer recognizer, IToken offendingSymbol, Int32 line, Int32 charPositionInLine, String msg,
				RecognitionException e)
			{
				throw new ParseCanceledException($"line {line}: {charPositionInLine}: {msg}");
			}

			public void SyntaxError(IRecognizer recognizer, Int32 offendingSymbol, Int32 line, Int32 charPositionInLine, String msg,
				RecognitionException e)
			{
				throw new ParseCanceledException($"line {line}: {charPositionInLine}: {msg}");
			}
		}

		private static FileNode ParseSourceFile(String path)
		{
			using (StreamReader inputStream = new StreamReader(new FileStream(path, FileMode.Open, FileAccess.Read)))
			{
				AntlrInputStream input = new AntlrInputStream(inputStream.ReadToEnd());

				LuminaryLexer lexer = new LuminaryLexer(input);
				lexer.RemoveErrorListeners();
				lexer.AddErrorListener(ThrowingErrorListener.Instance);

				CommonTokenStream tokens = new CommonTokenStream(lexer);
				LuminaryParser parser = new LuminaryParser(tokens);
				parser.RemoveErrorListeners();
				parser.AddErrorListener(ThrowingErrorListener.Instance);

				IParseTree tree = parser.protocol();
				ParseTreeWalker walker = new ParseTreeWalker();
				LuminaryListener visitor = new LuminaryListener(tokens);
				walker.Walk(visitor, tree);
				if (visitor.NumberOfErrors != 0)
					throw new ArgumentException($"File '{path}' does not contain a valid protocol definition.");

				FileNode fileNode = visitor.File;
				fileNode.FilePath = path;
				return fileNode;
			}
		}

		private static readonly Dictionary<String, Type> PredefinedTypes = new Dictionary<String, Type>()
		{
			{"Type", TypeType.Instance},
			{"Boolean", TypeBoolean.Instance},
			{"Int8", TypeInt8.Instance},
			{"UInt8", TypeUInt8.Instance},
			{"Int16", TypeInt16.Instance},
			{"UInt16", TypeUInt16.Instance},
			{"Int32", TypeInt32.Instance},
			{"UInt32", TypeUInt32.Instance},
			{"Int64", TypeInt64.Instance},
			{"UInt64", TypeUInt64.Instance},
			{"Float32", TypeFloat32.Instance},
			{"Float64", TypeFloat64.Instance},
			{"Decimal", TypeDecimal.Instance},
			{"Text", TypeText.Instance},
			{"Data", TypeData.Instance},
			{"Timestamp", TypeTimestamp.Instance},
			{"Duration", TypeDuration.Instance},
			{"Date", TypeDate.Instance},
			{"Time", TypeTime.Instance},
			{"UUID", TypeUUID.Instance}
		};

		public static DecoratorUsage DecoratorUsageDecoratorForItself()
		{
			DecoratorUsage usage = new DecoratorUsage();
			usage.ValidOn.Add(DecoratorTarget.Decorator);
			return usage;
		}
	}
}
