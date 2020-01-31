using System;

namespace Deltix.Luminary
{
	public sealed class LiteralDecimal : Literal, ILiteral<Decimal?>
	{
		private readonly String _specialName;

		public LiteralDecimal(Decimal value)
			: base(LiteralKind.Decimal)
		{
			Value = value;
		}

		public LiteralDecimal(String name)
			: base(LiteralKind.Decimal)
		{
			_specialName = name;
		}

		/// <summary>
		/// The value of the literal or <c>null</c> if this is literal represents special constant like
		/// NaN, NULL, or infinities.
		/// </summary>
		public Decimal? Value { get; }

		public override String ToString()
		{
			return Value != null ? $"{Value:G29}d64" : _specialName;
		}
	}
}
