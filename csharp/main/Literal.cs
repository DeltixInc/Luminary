namespace Deltix.Luminary
{
	public abstract class Literal
	{
		protected Literal(LiteralKind kind)
		{
			Kind = kind;
		}

		public LiteralKind Kind { get; }

		public ILiteral<T> As<T>()
		{
			return (ILiteral<T>) this;
		}

		public T ValueAs<T>()
		{
			return ((ILiteral<T>) this).Value;
		}
	}
}
