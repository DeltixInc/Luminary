using System;

namespace Deltix.Luminary
{
	public sealed class LiteralFloat64 : Literal, ILiteral<Double>
	{
		public LiteralFloat64(Double value)
			: base(LiteralKind.Float64)
		{
			Value = value;
		}

		public Double Value { get; }

		public override String ToString()
		{
			if (Double.IsNaN(Value))
				return "Float64.NaN";
			if (Double.IsPositiveInfinity(Value))
				return "Float64.POSITIVE_INFINITY";
			if (Double.IsNegativeInfinity(Value))
				return "Float64.NEGATIVE_INFINITY";
			return $"{Value:G17}f64";
		}
	}
}
