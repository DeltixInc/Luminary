using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class DateDef : IConstantScope, ITypeDef<TypeDate>
	{
		private DateDef()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeDate.Instance, new LiteralDate(LiteralDate.MinValueAsString)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeDate.Instance, new LiteralDate(LiteralDate.MaxValueAsString)));
		}

		public static DateDef Instance = new DateDef();

		public String Name => "Date";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeDate Type => TypeDate.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
