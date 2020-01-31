using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary.Implementation
{
	internal class FormalParameterNode : PropertyNode
	{
		public FormalParameterNode([NotNull] String name, [NotNull] String type, Boolean isParameterArray, [CanBeNull] List<String> comments)
			: base(name, type, comments)
		{
			IsParameterArray = isParameterArray;
		}

		public Boolean IsParameterArray { get; }
	}
}
