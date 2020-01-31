using System;

namespace Deltix.Luminary
{
	public sealed class LiteralFloat32 : Literal, ILiteral<Single>
	{
		public LiteralFloat32(Single value)
			: base(LiteralKind.Float32)
		{
			Value = value;
		}

		public Single Value { get; }

		public override String ToString()
		{
			if (Double.IsNaN(Value))
				return "Float32.NaN";
			if (Double.IsPositiveInfinity(Value))
				return "Float32.POSITIVE_INFINITY";
			if (Double.IsNegativeInfinity(Value))
				return "Float32.NEGATIVE_INFINITY";
			return $"{Value:G9}f32";
		}
	}
}
