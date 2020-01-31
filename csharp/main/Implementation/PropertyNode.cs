using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary.Implementation
{
	internal class PropertyNode : ItemNode
	{
		public PropertyNode([NotNull] String name, [NotNull] String type, [CanBeNull] List<String> comments)
			: base(name, comments)
		{
			Type = type;
		}

		public String Type { get; }
	}
}
