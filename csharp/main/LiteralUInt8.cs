using System;

namespace Deltix.Luminary
{
	public sealed class LiteralUInt8 : LiteralInteger, ILiteral<Byte>
	{
		public LiteralUInt8(Byte value)
			: base(LiteralKind.UInt8)
		{
			Value = value;
		}

		public Byte Value { get; }

		public override LiteralInteger CastTo(IntegralType type)
		{
			switch (type)
			{
				case IntegralType.Int64:
					return new LiteralInt64(Value);

				case IntegralType.UInt64:
					return new LiteralUInt64(Value);

				case IntegralType.Int32:
					return new LiteralInt32(Value);

				case IntegralType.UInt32:
					return new LiteralUInt32(Value);

				case IntegralType.Int16:
					return new LiteralInt16(Value);

				case IntegralType.UInt16:
					return new LiteralUInt16(Value);

				case IntegralType.Int8:
					if (Value > SByte.MaxValue)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'Int8'.");
					return new LiteralInt8((SByte) Value);

				case IntegralType.UInt8:
					return this;

				default:
					throw new ArgumentOutOfRangeException(nameof(type), type, null);
			}
		}

		public override String ToString()
		{
			return $"{Value}u8";
		}
	}
}
