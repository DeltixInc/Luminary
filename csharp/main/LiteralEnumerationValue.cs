using System;

namespace Deltix.Luminary
{
	public class LiteralEnumerationValue : Literal, ILiteral<EnumerationMemberDef>
	{
		public LiteralEnumerationValue(EnumerationMemberDef value)
			: base(LiteralKind.EnumerationValue)
		{
			Value = value;
		}

		public EnumerationMemberDef Value { get; }

		public override String ToString()
		{
			return $"{Value.Owner.Name}.{Value.Name}";
		}
	}
}
