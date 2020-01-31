using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class UInt64Def : IConstantScope, ITypeDef<TypeUInt64>
	{
		private UInt64Def()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeUInt64.Instance, new LiteralUInt64(UInt64.MinValue)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeUInt64.Instance, new LiteralUInt64(UInt64.MaxValue)));
		}

		public static UInt64Def Instance = new UInt64Def();

		public String Name => "UInt64";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeUInt64 Type => TypeUInt64.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
