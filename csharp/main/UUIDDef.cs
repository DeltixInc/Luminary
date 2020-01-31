using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	// ReSharper disable once InconsistentNaming
	public class UUIDDef : IConstantScope, ITypeDef<TypeUUID>
	{
		private UUIDDef()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeTime.Instance, new LiteralUUID(LiteralUUID.MinValue)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeTime.Instance, new LiteralUUID(LiteralUUID.MaxValue)));
		}

		public static UUIDDef Instance = new UUIDDef();

		public String Name => "UUID";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeUUID Type => TypeUUID.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
