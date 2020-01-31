using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	/// <summary>
	/// Definition of the Luminary interface.
	/// </summary>
	public class InterfaceDef : ClassOrInterfaceDef<InterfacePropertyDef, TypeInterface>
	{
		public InterfaceDef([NotNull] ITypeScope parent, [NotNull] String name, [CanBeNull, ItemNotNull] List<String> comments = null)
			: base(name, parent, comments)
		{
			Type = new TypeInterface(this);

			FullName = parent is FileDef ? Name : $"{((ItemDef) parent).FullName}.{Name}";
			Methods = new List<InterfaceMethodDef>();

			parent.DefinedInterfaces.Add(this);
		}

		/// <summary>
		/// List of methods defined by this interface.
		/// </summary>
		[NotNull]
		public List<InterfaceMethodDef> Methods { get; }

		public override TypeInterface Type { get; }

		/// <summary>
		/// Full name of the interface.
		/// </summary>
		public override String FullName { get; }
	}
}
