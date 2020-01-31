using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public class InterfacePropertyDef : PropertyDef<InterfaceDef>
	{
		public InterfacePropertyDef([NotNull] InterfaceDef interfaceDef, [NotNull] String name, [NotNull] Type type, Boolean isOverride, Boolean isReadable, Boolean isWritable, [CanBeNull, ItemNotNull] List<String> comments = null)
			: base(interfaceDef, name, type, comments)
		{
			IsReadable = isReadable;
			IsWritable = isWritable;
			IsOverride = isOverride;

			FullName = $"{interfaceDef.FullName}.{Name}";

			interfaceDef.Properties.Add(this);
		}

		/// <summary>
		/// Indicates whether this property overrides some property from the base class.
		/// </summary>
		public Boolean IsOverride { get; }

		public Boolean IsReadable { get; }

		public Boolean IsWritable { get; }

		public override String FullName { get; }
	}
}
