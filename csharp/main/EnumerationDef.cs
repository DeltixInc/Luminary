using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public sealed class EnumerationDef : ItemDef, ITypeDef<TypeEnumeration>
	{
		public EnumerationDef([NotNull] ITypeScope parent, [NotNull] String name, IntegralType underlyingType, [CanBeNull, ItemNotNull] List<String> comments = null)
			: base(name, comments)
		{
			UnderlyingType = underlyingType;
			Parent = parent;

			File = parent.File;
			Members = new List<EnumerationMemberDef>();
			Type = new TypeEnumeration(this);
			FullName = parent is FileDef ? Name : $"{((ItemDef) parent).FullName}.{Name}";

			parent.DefinedEnumerations.Add(this);
		}

		public IntegralType UnderlyingType { get; }

		[NotNull]
		public FileDef File { get; }

		[NotNull]
		public ITypeScope Parent { get; }

		[NotNull, ItemNotNull]
		public List<EnumerationMemberDef> Members { get; }

		[NotNull]
		public TypeEnumeration Type { get; }

		public override String FullName { get; }
	}
}
