using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class UInt32Def : IConstantScope, ITypeDef<TypeUInt32>
	{
		private UInt32Def()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeUInt32.Instance, new LiteralUInt32(UInt32.MinValue)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeUInt32.Instance, new LiteralUInt32(UInt32.MaxValue)));
		}

		public static UInt32Def Instance = new UInt32Def();

		public String Name => "UInt32";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeUInt32 Type => TypeUInt32.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
