namespace Deltix.Luminary
{
	public interface ITypeDef<out T>
		where T: Type
	{
		T Type { get; }

		FileDef File { get; }

		ITypeScope Parent { get; }
	}
}
