using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class Int16Def : IConstantScope, ITypeDef<TypeInt16>
	{
		private Int16Def()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeInt16.Instance, new LiteralInt16(Int16.MinValue)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeInt16.Instance, new LiteralInt16(Int16.MaxValue)));
		}

		public static Int16Def Instance = new Int16Def();

		public String Name => "Int16";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeInt16 Type => TypeInt16.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
