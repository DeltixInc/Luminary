using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary.Implementation
{
	public class DecoratorValueNode
	{
		public DecoratorValueNode([NotNull] String name, [CanBeNull, ItemNotNull] List<NameValuePair> arguments)
		{
			Name = name;
			Arguments = arguments ?? new List<NameValuePair>();
		}

		[NotNull]
		public String Name { get; }

		[NotNull, ItemNotNull]
		public List<NameValuePair> Arguments { get; }
	}
}
