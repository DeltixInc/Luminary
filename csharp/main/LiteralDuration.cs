using System;

namespace Deltix.Luminary
{
	public sealed class LiteralDuration : Literal, ILiteral<String>
	{
		public const String MinValueAsString = "#MIN";
		public const String MaxValueAsString = "#MAX";

		public LiteralDuration(String value)
			: base(LiteralKind.Duration)
		{
			Value = value;
		}

		public String Value { get; }

		public override String ToString()
		{
			return $"\"{Value}\"";
		}
	}
}
