using System;

namespace Deltix.Luminary
{
	public sealed class LiteralType : Literal, ILiteral<Type>
	{
		public LiteralType(Type value)
			: base(LiteralKind.Type)
		{
			Value = value;
		}

		public Type Value { get; }

		public override String ToString()
		{
			return $"typeof({Value})";
		}
	}
}
