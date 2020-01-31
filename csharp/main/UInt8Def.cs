using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class UInt8Def : IConstantScope, ITypeDef<TypeUInt8>
	{
		private UInt8Def()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeUInt8.Instance, new LiteralUInt8(Byte.MinValue)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeUInt8.Instance, new LiteralUInt8(Byte.MaxValue)));
		}

		public static UInt8Def Instance = new UInt8Def();

		public String Name => "UInt8";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeUInt8 Type => TypeUInt8.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
