using System;
using System.Collections.Generic;
using System.Threading;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public sealed class ClassPropertyDef : PropertyDef<ClassDef>
	{
		public ClassPropertyDef([NotNull] ClassDef classDef, [NotNull] String name, [NotNull] Type type, [CanBeNull] Literal @default = null, Boolean isOverride = false, Boolean isFinal = false, [CanBeNull, ItemNotNull] List<String> comments = null)
			: base(classDef, name, type, comments)
		{
			Default = @default;
			IsOverride = isOverride;
			IsFinal = isFinal;

			FullName = $"{classDef.FullName}.{Name}";

			classDef.Properties.Add(this);
		}

		[CanBeNull]
		public Literal Default { get; internal set; }

		public override String FullName { get; }

		/// <summary>
		/// Indicates whether this property overrides some property from the base class.
		/// </summary>
		public Boolean IsOverride { get; }

		/// <summary>
		/// Indicates whether this property is final (cannot be overriden).
		/// </summary>
		public Boolean IsFinal { get; }
	}
}
