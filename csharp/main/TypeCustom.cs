using System;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public abstract class TypeCustom : Type
	{
		protected TypeCustom(TypeKind kind)
			: base(kind)
		{}

		/// <summary>Name of the custom type without any qualifiers (i.e. just an identifier).</summary>
		public abstract String Name { get; }

		/// <summary>File containing the definition of this type.</summary>
		[NotNull]
		public abstract FileDef File { get; }

		/// <summary>Namespace containing the definition of this type.</summary>
		[NotNull]
		public NamespaceDef Namespace => File.Namespace;

		/// <summary>Project containing the definition of this type.</summary>
		[NotNull]
		public ProjectDef Project => File.Namespace.Project;
	}
}
