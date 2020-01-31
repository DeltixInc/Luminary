using System;

namespace Deltix.Luminary
{
	public sealed class LiteralUInt32 : LiteralInteger, ILiteral<UInt32>
	{
		public LiteralUInt32(UInt32 value)
			: base(LiteralKind.UInt32)
		{
			Value = value;
		}

		public UInt32 Value { get; }

		public override LiteralInteger CastTo(IntegralType type)
		{
			switch (type)
			{
				case IntegralType.Int64:
					return new LiteralInt64(Value);

				case IntegralType.UInt64:
					return new LiteralUInt64(Value);

				case IntegralType.Int32:
					if (Value > Int32.MaxValue)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'Int32'.");
					return new LiteralInt32((Int32) Value);

				case IntegralType.UInt32:
					return this;

				case IntegralType.Int16:
					if (Value > Int16.MaxValue)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'Int16'.");
					return new LiteralInt16((Int16) Value);

				case IntegralType.UInt16:
					if (Value > UInt16.MaxValue)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'UInt16'.");
					return new LiteralUInt16((UInt16) Value);

				case IntegralType.Int8:
					if (Value > SByte.MaxValue)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'Int8'.");
					return new LiteralInt8((SByte) Value);

				case IntegralType.UInt8:
					if (Value > Byte.MaxValue)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'UInt8'.");
					return new LiteralUInt8((Byte) Value);

				default:
					throw new ArgumentOutOfRangeException(nameof(type), type, null);
			}
		}

		public override String ToString()
		{
			return $"{Value}u32";
		}
	}
}
