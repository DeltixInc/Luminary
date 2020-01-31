using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public sealed class ClassDef : ClassOrInterfaceDef<ClassPropertyDef, TypeClass>, IConstantAndTypeContainer
	{
		public ClassDef([NotNull] ITypeScope parent, [NotNull] String name, Boolean isFinal, [CanBeNull, ItemNotNull] List<String> comments = null)
			: base(name, parent, comments)
		{
			Type = new TypeClass(this);
			FullName = parent is FileDef ? Name : $"{((ItemDef) parent).FullName}.{Name}";
			IsFinal = isFinal;

			parent.DefinedClasses.Add(this);
		}

		[CanBeNull]
		public ClassDef SuperClass { get; set; }

		/// <summary>
		/// Indicates whether this class is final (cannot be subclassed) or not.
		/// </summary>
		public Boolean IsFinal { get; }

		public override TypeClass Type { get; }

		public override String FullName { get; }

		[NotNull, ItemNotNull]
		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();
	}
}
