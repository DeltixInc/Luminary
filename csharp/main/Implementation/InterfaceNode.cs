using System;
using System.Collections.Generic;

namespace Deltix.Luminary.Implementation
{
	internal class InterfaceNode : CompositeTypeNode<InterfacePropertyNode>
	{
		public InterfaceNode(String name, List<String> comments, List<String> supertypes)
			: base(name, comments)
		{
			Methods = new List<InterfaceMethodNode>();
			Supertypes = supertypes;
		}

		public List<InterfaceMethodNode> Methods { get; }

		public List<String> Supertypes { get; }
	}
}
