using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary.Implementation
{
	internal class DecoratorPropertyNode : PropertyNode
	{
		public DecoratorPropertyNode([NotNull] String name, [NotNull] String type, [CanBeNull] List<String> comments)
			: base(name, type, comments)
		{
		}

		[CanBeNull]
		public Literal Default { get; internal set; }
	}
}
