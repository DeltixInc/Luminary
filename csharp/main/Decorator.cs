using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public sealed class Decorator
	{
		public Decorator([NotNull] DecoratorDef definition, [CanBeNull] List<DecoratorPropertyValue> arguments)
		{
			Definition = definition;
			Arguments = arguments ?? new List<DecoratorPropertyValue>();
		}

		[NotNull]
		public DecoratorDef Definition { get; }

		[NotNull, ItemNotNull]
		public List<DecoratorPropertyValue> Arguments { get; }
	}
}
