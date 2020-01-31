using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary.Implementation
{
	internal class EnumerationNode : ItemNode
	{
		public EnumerationNode([NotNull] String name, [NotNull] String underlyingType,
			[CanBeNull, ItemNotNull] List<String> comments)
			: base(name, comments)
		{
			UnderlyingType = underlyingType;
		}

		public String UnderlyingType { get; }

		public List<EnumerationMemberNode> Members { get; } = new List<EnumerationMemberNode>();
	}
}
