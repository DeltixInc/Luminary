using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary.Implementation
{
	internal class InterfaceMethodNode : ItemNode
	{
		public InterfaceMethodNode([NotNull] String name, [CanBeNull] String returnType, [CanBeNull] List<String> comments)
			: base(name, comments)
		{
			ReturnType = returnType;
			FormalParameters = new List<FormalParameterNode>();
		}

		[CanBeNull]
		public String ReturnType { get; }

		public List<FormalParameterNode> FormalParameters { get; }
	}
}
