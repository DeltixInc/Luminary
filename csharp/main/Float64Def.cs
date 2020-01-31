using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class Float64Def : IConstantScope, ITypeDef<TypeFloat64>
	{
		private Float64Def()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeFloat64.Instance, new LiteralFloat64(Double.MinValue)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeFloat64.Instance, new LiteralFloat64(Double.MaxValue)));
			DefinedConstants.Add(new ConstantDef(this, "NaN", TypeFloat64.Instance, new LiteralFloat64(Double.NaN)));
			DefinedConstants.Add(new ConstantDef(this, "POSITIVE_INFINITY", TypeFloat64.Instance, new LiteralFloat64(Double.PositiveInfinity)));
			DefinedConstants.Add(new ConstantDef(this, "NEGATIVE_INFINITY", TypeFloat64.Instance, new LiteralFloat64(Double.NegativeInfinity)));
		}

		public static Float64Def Instance = new Float64Def();

		public String Name => "Float64";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeFloat64 Type => TypeFloat64.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
