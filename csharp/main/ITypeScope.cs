using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public interface ITypeScope
	{
		[NotNull, ItemNotNull]
		List<ClassDef> DefinedClasses { get; }

		[NotNull, ItemNotNull]
		List<EnumerationDef> DefinedEnumerations { get; }

		[NotNull, ItemNotNull]
		List<InterfaceDef> DefinedInterfaces { get; }

		[NotNull, ItemNotNull]
		List<DecoratorDef> DefinedDecorators { get; }

		ITypeScope Parent { get; }

		FileDef File { get; }
	}
}
