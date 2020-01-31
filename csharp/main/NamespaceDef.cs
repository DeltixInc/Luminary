using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public class NamespaceDef
	{
		public NamespaceDef([NotNull] ProjectDef projectDef, [NotNull] String @namespace)
		{
			Namespace = @namespace;
			Project = projectDef;
			projectDef.Namespaces.Add(Namespace, this);
		}

		[NotNull]
		public String Namespace { get; }

		[NotNull]
		public SortedDictionary<String, FileDef> Files { get; } = new SortedDictionary<String, FileDef>();

		[NotNull]
		public ProjectDef Project { get; }

		[NotNull]
		public Dictionary<String, TypeCustom> DefinedTypes = new Dictionary<String, TypeCustom>();

		/// <inheritdoc />
		public override String ToString()
		{
			return Namespace;
		}
	}
}
