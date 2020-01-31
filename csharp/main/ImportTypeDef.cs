using System;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public class ImportTypeDef : ImportDef
	{
		public ImportTypeDef([NotNull] TypeCustom target, [CanBeNull] String alias = null)
			: base(ImportKind.Type)
		{
			Target = target;
			Alias = alias;
		}

		[NotNull]
		public TypeCustom Target { get; }

		[CanBeNull]
		public String Alias { get; }
	}
}
