using System;
using System.Collections.Generic;
using System.IO;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public class FileDef : ITypeScope
	{
		/// <summary>
		/// Creates an instance that describes a particular file within the give namespace.
		/// </summary>
		/// <param name="namespace">Instance of the <see cref="NamespaceDef"/> this file belongs to.</param>
		/// <param name="fileName">Name of the file.</param>
		public FileDef([NotNull] NamespaceDef @namespace, [NotNull] String fileName)
		{
			Namespace = @namespace;
			FileName = fileName;
			Namespace.Files.Add(fileName, this);
		}

		[NotNull]
		public ProjectDef Project => Namespace.Project;

		[NotNull]
		public NamespaceDef Namespace { get; }

		/// <summary>
		/// Name of the file.
		/// </summary>
		[NotNull]
		public String FileName { get; }

		[NotNull, ItemNotNull]
		public List<ImportDef> Imports { get; } = new List<ImportDef>();

		[NotNull, ItemNotNull]
		public List<NameValuePair> Options { get; } = new List<NameValuePair>();

		public List<DecoratorDef> DefinedDecorators { get; } = new List<DecoratorDef>();

		public List<ClassDef> DefinedClasses { get; } = new List<ClassDef>();

		public List<EnumerationDef> DefinedEnumerations { get; } = new List<EnumerationDef>();

		public List<InterfaceDef> DefinedInterfaces { get; } = new List<InterfaceDef>();

		public Dictionary<String, TypeCustom> DefinedTypes { get; } = new Dictionary<String, TypeCustom>();

		[CanBeNull]
		public ITypeScope Parent => null;

		[NotNull]
		public FileDef File => this;

		/// <summary>
		/// Returns the path to this file on disk relative to the location of the project file.
		/// </summary>
		/// <returns>Relative path to this file on disk.</returns>
		[NotNull]
		public String GetPath()
		{
			// TODO: Should be Path.AltDirectorySeparatorChar or something similar.
			return Namespace.Namespace.Replace('.', '/') + FileName;
		}
	}
}
