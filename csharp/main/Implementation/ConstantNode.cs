using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary.Implementation
{
    internal class ConstantNode : ItemNode
    {
	    public ConstantNode([NotNull] String name, [NotNull] String type, [NotNull] Literal value, [CanBeNull] List<String> comments)
			: base(name, comments)
	    {
			Type = type;
			Value = value;
		}

		public String Type { get; }

		public Literal Value { get; }
    }
}
