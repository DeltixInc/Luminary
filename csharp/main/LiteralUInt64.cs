using System;

namespace Deltix.Luminary
{
	public sealed class LiteralUInt64 : LiteralInteger, ILiteral<UInt64>
	{
		public LiteralUInt64(UInt64 value)
			: base(LiteralKind.UInt64)
		{
			Value = value;
		}

		public UInt64 Value { get; }

		public override LiteralInteger CastTo(IntegralType type)
		{
			switch (type)
			{
				case IntegralType.Int64:
					if (Value > Int64.MaxValue)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'Int64'.");
					return new LiteralInt64((Int64) Value);

				case IntegralType.UInt64:
					return this;

				case IntegralType.Int32:
					if (Value > Int32.MaxValue)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'Int32'.");
					return new LiteralInt32((Int32) Value);

				case IntegralType.UInt32:
					if (Value > UInt32.MaxValue)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'UInt32'.");
					return new LiteralUInt32((UInt32) Value);

				case IntegralType.Int16:
					if (Value > (UInt64) Int16.MaxValue)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'Int16'.");
					return new LiteralInt16((Int16) Value);

				case IntegralType.UInt16:
					if (Value > UInt16.MaxValue)
						throw new InvalidCastException($"Value '{Value}' cannot be safely cast to 'UInt16'.");
					return new LiteralUInt16((UInt16) Value);

				case IntegralType.Int8:
					if (Value > (UInt64) SByte.MaxValue)
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
			return $"{Value}u64";
		}
	}
}
