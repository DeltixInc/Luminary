namespace Deltix.Luminary
{
	public interface ILiteral<out T>
	{
		T Value { get; }
	}
}
