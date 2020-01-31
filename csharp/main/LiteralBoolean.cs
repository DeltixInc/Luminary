using System;

namespace Deltix.Luminary
{
	public sealed class LiteralBoolean : Literal, ILiteral<Boolean>
	{
		private LiteralBoolean(Boolean value)
			: base(LiteralKind.Boolean)
		{
			Value = value;
		}

		public Boolean Value { get; }

		public override String ToString()
		{
			return Value ? "true" : "false";
		}

		public static readonly LiteralBoolean True = new LiteralBoolean(true);
		public static readonly LiteralBoolean False = new LiteralBoolean(false);
	}
}
