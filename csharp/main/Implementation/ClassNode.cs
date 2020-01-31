using System;
using System.Collections.Generic;

namespace Deltix.Luminary.Implementation
{
	internal class ClassNode : CompositeTypeNode<ClassPropertyNode>
	{
		public ClassNode(String name, Boolean isFinal, List<String> supertypes, List<String> comments)
			: base(name, comments)
		{
			Supertypes = supertypes;
			IsFinal = isFinal;
			DefinedConstants = new List<ConstantNode>();
		}

		public List<String> Supertypes { get; }

		public List<ConstantNode> DefinedConstants { get; }
		public Boolean IsFinal { get; }
	}
}
