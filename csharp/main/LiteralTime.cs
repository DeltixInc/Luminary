using System;

namespace Deltix.Luminary
{
	public sealed class LiteralTime : Literal, ILiteral<String>
	{
		public const String MinValueAsString = "#MIN";
		public const String MaxValueAsString = "#MAX";

		public LiteralTime(String value)
			: base(LiteralKind.Time)
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