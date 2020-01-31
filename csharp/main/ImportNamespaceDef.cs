using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public class ImportNamespaceDef : ImportDef
	{
		public ImportNamespaceDef([NotNull] NamespaceDef target)
			: base(ImportKind.Namespace)
		{
			Target = target;
		}

		[NotNull]
		public NamespaceDef Target { get; }
	}
}
