using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public interface IConstantScope
	{
		[NotNull]
		String Name { get; }

		[NotNull]
		String FullName { get; }

		[NotNull, ItemNotNull]
		List<ConstantDef> DefinedConstants { get; }
	}
}
