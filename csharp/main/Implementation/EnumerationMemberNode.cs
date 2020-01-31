using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary.Implementation
{
	internal class EnumerationMemberNode : ItemNode
	{
		public EnumerationMemberNode([NotNull] String name, [CanBeNull] List<String> comments)
			: base(name, comments)
		{}

		public Literal Value { get; internal set; }
	}
}
