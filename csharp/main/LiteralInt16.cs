using System;

namespace Deltix.Luminary
{
	public sealed class LiteralInt16 : LiteralInteger, ILiteral<Int16>
	{
		public LiteralInt16(Int16 value)
			: base(LiteralKind.SInt16)
		{
			Value = value;
		}

		public Int16 Value { get; }

		public override LiteralInteger CastTo(IntegralType type)
		{
			switch (type)
			{
				case IntegralType.Int64:
					return new LiteralInt64(Value);

				case IntegralType.UInt64:
					if (Value < 0)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'UInt64'.");
					return new LiteralUInt64((UInt64) Value);

				case IntegralType.Int32:
					return new LiteralInt32(Value);

				case IntegralType.UInt32:
					if (Value < 0)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'UInt32'.");
					return new LiteralUInt32((UInt32) Value);

				case IntegralType.Int16:
					return this;

				case IntegralType.UInt16:
					if (Value < 0)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'UInt16'.");
					return new LiteralUInt16((UInt16) Value);

				case IntegralType.Int8:
					if (Value > SByte.MaxValue || Value < SByte.MinValue)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'SInt8'.");
					return new LiteralInt8((SByte) Value);

				case IntegralType.UInt8:
					if (Value < Byte.MinValue || Value > Byte.MaxValue)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'UInt8'.");
					return new LiteralUInt8((Byte) Value);

				default:
					throw new ArgumentOutOfRangeException(nameof(type), type, null);
			}
		}

		public override String ToString()
		{
			return $"{Value}i16";
		}
	}
}
