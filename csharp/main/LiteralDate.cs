using System;

namespace Deltix.Luminary
{
	public sealed class LiteralDate : Literal, ILiteral<String>
	{
		public const String MinValueAsString = "#MIN";
		public const String MaxValueAsString = "#MAX";

		public LiteralDate(String value)
			: base(LiteralKind.Date)
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
