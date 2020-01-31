namespace Deltix.Luminary
{
	public abstract class LiteralInteger : Literal
	{
		protected LiteralInteger(LiteralKind kind) : base(kind)
		{}

		public abstract LiteralInteger CastTo(IntegralType type);
	}
}
