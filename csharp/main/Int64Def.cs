using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class Int64Def : IConstantScope, ITypeDef<TypeInt64>
	{
		private Int64Def()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeInt64.Instance, new LiteralInt64(Int64.MinValue)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeInt64.Instance, new LiteralInt64(Int64.MaxValue)));
		}

		public static Int64Def Instance = new Int64Def();

		public String Name => "Int64";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeInt64 Type => TypeInt64.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
