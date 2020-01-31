using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class Int8Def : IConstantScope, ITypeDef<TypeInt8>
	{
		private Int8Def()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeInt8.Instance, new LiteralInt8(SByte.MinValue)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeInt8.Instance, new LiteralInt8(SByte.MaxValue)));
		}

		public static Int8Def Instance = new Int8Def();

		public String Name => "Int8";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeInt8 Type => TypeInt8.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
