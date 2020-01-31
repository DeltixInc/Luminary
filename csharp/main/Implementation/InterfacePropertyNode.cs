using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary.Implementation
{
	internal class InterfacePropertyNode : PropertyNode
	{
		public InterfacePropertyNode([NotNull] String name, [NotNull] String type, Boolean isOverride, Boolean isGettable, Boolean isSettable,
			[CanBeNull] List<String> comments)
			: base(name, type, comments)
		{
			IsOverride = isOverride;
			IsGettable = isGettable;
			IsSettable = isSettable;
		}

		public Boolean IsOverride { get; }

		public Boolean IsGettable { get; }

		public Boolean IsSettable { get; }
	}
}
