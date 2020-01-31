using System;

namespace Deltix.Luminary
{
	public class LiteralConstant : Literal, ILiteral<ConstantDef>
	{
		public LiteralConstant(ConstantDef value)
			: base(LiteralKind.Constant)
		{
			Value = value;
		}

		public ConstantDef Value { get; }

		public override String ToString()
		{
			return $"{Value.Owner.Name}.{Value.Name}";
		}
	}
}
