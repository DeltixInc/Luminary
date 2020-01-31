using System;

namespace Deltix.Luminary
{
	public sealed class LiteralTimestamp : Literal, ILiteral<String>
	{
		public const String MinValueAsString = "#MIN";
		public const String MaxValueAsString = "#MAX";

		public LiteralTimestamp(String value)
			: base(LiteralKind.Timestamp)
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
