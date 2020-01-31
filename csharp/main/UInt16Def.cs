using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class UInt16Def : IConstantScope, ITypeDef<TypeUInt16>
	{
		private UInt16Def()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeUInt16.Instance, new LiteralUInt16(UInt16.MinValue)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeUInt16.Instance, new LiteralUInt16(UInt16.MaxValue)));
		}

		public static UInt16Def Instance = new UInt16Def();

		public String Name => "UInt16";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeUInt16 Type => TypeUInt16.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
