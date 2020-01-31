using System;

namespace Deltix.Luminary
{
	public sealed class LiteralText : Literal, ILiteral<String>
	{
		public LiteralText(String value)
			: base(LiteralKind.Text)
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
