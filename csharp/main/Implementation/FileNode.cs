using System;
using System.Collections.Generic;

namespace Deltix.Luminary.Implementation
{
	internal class FileNode : ITypeContainerNode
	{
		public String FilePath { get; set; }

		public String Namespace { get; set; }

		public List<ImportNode> Imports { get; } = new List<ImportNode>();

		public List<NameValuePair> Options { get; } = new List<NameValuePair>();

		public List<ClassNode> DefinedClasses { get; } = new List<ClassNode>();

		public List<EnumerationNode> DefinedEnumerations { get; } = new List<EnumerationNode>();

		public List<InterfaceNode> DefinedInterfaces { get; } = new List<InterfaceNode>();

		public List<DecoratorNode> DefinedDecorators { get; } = new List<DecoratorNode>();
	}
}
