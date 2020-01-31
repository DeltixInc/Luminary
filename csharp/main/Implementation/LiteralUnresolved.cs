using System;

namespace Deltix.Luminary.Implementation
{
	internal class LiteralUnresolved : Literal, ILiteral<String>
	{
		public LiteralUnresolved(LiteralKind kind, String value)
			: base(kind)
		{
			Value = value;
		}

		public String Value { get; }

		public override String ToString()
		{
			return Value;
		}
	}
}
