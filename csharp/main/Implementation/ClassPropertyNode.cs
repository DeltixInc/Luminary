using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary.Implementation
{
	internal class ClassPropertyNode : PropertyNode
	{
		public ClassPropertyNode([NotNull] String name, [NotNull] String type, Boolean isOverride, Boolean isFinal, [CanBeNull] List<String> comments)
			: base(name, type, comments)
		{
			IsOverride = isOverride;
			IsFinal = isFinal;
		}

		public Boolean IsOverride { get; }

		[CanBeNull]
		public Literal Default { get; internal set; }

		public Boolean IsFinal { get; set; }
	}
}
