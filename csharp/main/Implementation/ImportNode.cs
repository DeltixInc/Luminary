using System;
using JetBrains.Annotations;

namespace Deltix.Luminary.Implementation
{
	internal class ImportNode
	{
		private ImportNode(String @namespace)
		{
			Namespace = @namespace;
		}

		[CanBeNull]
		public String TypeName { get; private set; }

		[CanBeNull]
		public String Alias { get; private set; }

		[NotNull]
		public String Namespace { get; }

		public static ImportNode Type(String @namespace, String name)
		{
			return new ImportNode(@namespace) {TypeName = name};
		}

		public static ImportNode TypeWithAlias(String @namespace, String name, String alias)
		{
			return new ImportNode(@namespace)
			{
				TypeName = name,
				Alias = alias
			};
		}

		public static ImportNode Everything(String @namespace)
		{
			return new ImportNode(@namespace);
		}
	}
}
