using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public abstract class PropertyDef<T> : ItemDef
	{
		protected PropertyDef([NotNull] T owner, [NotNull] String name, [NotNull] Type type, [CanBeNull, ItemNotNull] List<String> comments = null)
			: base(name, comments)
		{
			Type = type;
			Owner = owner;
		}

		[NotNull]
		public Type Type { get; }

		[NotNull]
		public T Owner { get; }
	}
}
