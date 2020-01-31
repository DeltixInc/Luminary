using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class DecimalDef : IConstantScope, ITypeDef<TypeDecimal>
	{
		private DecimalDef()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeDecimal.Instance, new LiteralDecimal(Decimal.MinValue)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeDecimal.Instance, new LiteralDecimal(Decimal.MaxValue)));
			DefinedConstants.Add(new ConstantDef(this, "NaN", TypeDecimal.Instance, new LiteralDecimal("Decimal.NaN")));
			DefinedConstants.Add(new ConstantDef(this, "POSITIVE_INFINITY", TypeDecimal.Instance, new LiteralDecimal("Decimal.POSITIVE_INFINITY")));
			DefinedConstants.Add(new ConstantDef(this, "NEGATIVE_INFINITY", TypeDecimal.Instance, new LiteralDecimal("Decimal.NEGATIVE_INFINITY")));
			DefinedConstants.Add(new ConstantDef(this, "NULL", TypeDecimal.Instance, new LiteralDecimal("Decimal.NULL")));
		}

		public static DecimalDef Instance = new DecimalDef();

		public String Name => "Decimal";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeDecimal Type => TypeDecimal.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
