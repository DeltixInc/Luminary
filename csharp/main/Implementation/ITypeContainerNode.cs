using System.Collections.Generic;

namespace Deltix.Luminary.Implementation
{
	internal interface ITypeContainerNode
	{
		List<ClassNode> DefinedClasses { get; }

		List<EnumerationNode> DefinedEnumerations { get; }

		List<InterfaceNode> DefinedInterfaces { get; }

		List<DecoratorNode> DefinedDecorators { get; }
	}
}
