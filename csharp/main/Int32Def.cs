using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class Int32Def : IConstantScope, ITypeDef<TypeInt32>
	{
		private Int32Def()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeInt32.Instance, new LiteralInt32(Int32.MinValue)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeInt32.Instance, new LiteralInt32(Int32.MaxValue)));
		}

		public static Int32Def Instance = new Int32Def();

		public String Name => "Int32";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeInt32 Type => TypeInt32.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
