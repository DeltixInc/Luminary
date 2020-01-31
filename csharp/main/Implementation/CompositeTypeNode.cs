using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary.Implementation
{
	internal class CompositeTypeNode<T> : ItemNode, ITypeContainerNode
		where T: PropertyNode
	{
		public CompositeTypeNode([NotNull] String name, [CanBeNull] List<String> comments)
			: base(name, comments)
		{}

		public List<T> Properties { get; } = new List<T>();

		public List<ClassNode> DefinedClasses { get; } = new List<ClassNode>();

		public List<EnumerationNode> DefinedEnumerations { get; } = new List<EnumerationNode>();

		public List<InterfaceNode> DefinedInterfaces { get; } = new List<InterfaceNode>();

		public List<DecoratorNode> DefinedDecorators { get; } = new List<DecoratorNode>();
	}
}
