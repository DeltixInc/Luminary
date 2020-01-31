using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public sealed class DecoratorPropertyDef : PropertyDef<DecoratorDef>
	{
		public DecoratorPropertyDef([NotNull] DecoratorDef owner, [NotNull] String name, [NotNull] Type type, [CanBeNull] Literal @default, [CanBeNull, ItemNotNull] List<String> comments = null)
			: base(owner, name, type, comments)
		{
			Default = @default;

			FullName = $"{owner.FullName}.{Name}";

			owner.Properties.Add(this);
		}

		[CanBeNull]
		public Literal Default { get; internal set; }

		public override String FullName { get; }
	}
}
