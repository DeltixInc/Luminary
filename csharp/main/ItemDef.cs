using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public abstract class ItemDef : ICommentable, IDecoratable
	{
		protected ItemDef([NotNull] String name, [CanBeNull, ItemNotNull] List<String> comments)
		{
			Name = name;
			if (comments != null)
				Comments.AddRange(comments);
		}

		[NotNull]
		public String Name { get; }

		[NotNull, ItemNotNull]
		public List<Decorator> Decorators { get; } = new List<Decorator>();

		[NotNull, ItemNotNull]
		public List<String> Comments { get; } = new List<String>();

		[NotNull]
		public abstract String FullName { get; }
	}
}
