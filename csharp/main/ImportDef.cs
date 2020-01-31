namespace Deltix.Luminary
{
	public abstract class ImportDef
	{
		protected ImportDef(ImportKind kind)
		{
			Kind = kind;
		}

		public ImportKind Kind { get; }
	}
}
