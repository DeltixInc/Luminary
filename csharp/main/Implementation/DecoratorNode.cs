using System;
using System.Collections.Generic;

namespace Deltix.Luminary.Implementation
{
	internal class DecoratorNode : CompositeTypeNode<DecoratorPropertyNode>
	{
		public DecoratorNode(String name, List<String> comments)
			: base(name, comments)
		{
			DefinedConstants = new List<ConstantNode>();
		}

		public List<ConstantNode> DefinedConstants { get; }
	}
}
