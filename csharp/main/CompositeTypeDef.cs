using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public abstract class CompositeTypeDef<T> : ItemDef
	{
		protected CompositeTypeDef([NotNull] String name, [CanBeNull, ItemNotNull] List<String> comments)
			: base(name, comments)
		{}

		[NotNull, ItemNotNull]
		public List<T> Properties { get; } = new List<T>();
	}
}
