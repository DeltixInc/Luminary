using System;

namespace Deltix.Luminary
{
	public sealed class DecoratorPropertyValue
	{
		public DecoratorPropertyValue(DecoratorPropertyDef definition, Literal value, Boolean isDefault)
		{
			Definition = definition;
			Value = value;
			IsDefault = isDefault;
		}

		public DecoratorPropertyDef Definition { get; }

		public Literal Value { get; internal set; }

		public Boolean IsDefault { get; }
	}
}
