using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public class ConstantDef : ItemDef
	{
		public ConstantDef([NotNull] IConstantScope owner, [NotNull] String name, [NotNull] Type type, [NotNull] Literal value, [CanBeNull, ItemNotNull] List<String> comments = null)
			: base(name, comments)
		{
			Value = value;
			Type = type;
			Owner = owner;

			FullName = $"{owner.FullName}.{Name}";
		}

		[NotNull]
		public Type Type { get; }

		[NotNull]
		public IConstantScope Owner { get; }

		[NotNull]
		public Literal Value { get; internal set; }

		public override String FullName { get; }
	}
}
