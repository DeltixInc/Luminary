using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary.Implementation
{
	internal abstract class ItemNode
	{
		protected ItemNode([NotNull] String name, [CanBeNull] List<String> comments)
		{
			Name = name;
			Comments = comments ?? new List<String>();
		}

		[NotNull]
		public String Name { get; }

		[NotNull]
		public List<DecoratorValueNode> Decorators { get; } = new List<DecoratorValueNode>();

		[NotNull]
		public List<String> Comments { get; }
	}
}
