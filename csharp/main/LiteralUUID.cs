using System;

namespace Deltix.Luminary
{
	// ReSharper disable once InconsistentNaming
	public sealed class LiteralUUID : Literal, ILiteral<Guid>
	{
		public static Guid MinValue = Guid.Empty;
		public static Guid MaxValue = new Guid(-1, -1, -1, Byte.MaxValue, Byte.MaxValue, Byte.MaxValue, Byte.MaxValue, Byte.MaxValue, Byte.MaxValue, Byte.MaxValue, Byte.MaxValue);

		public LiteralUUID(Guid value)
			: base(LiteralKind.UUID)
		{
			Value = value;
		}

		public Guid Value { get; }

		public override String ToString()
		{
			return Value.ToString();
		}
	}
}
