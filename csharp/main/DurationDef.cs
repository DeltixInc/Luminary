using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class DurationDef : IConstantScope, ITypeDef<TypeDuration>
	{
		private DurationDef()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeDuration.Instance, new LiteralDuration(LiteralDuration.MinValueAsString)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeDuration.Instance, new LiteralDuration(LiteralDuration.MaxValueAsString)));
		}

		public static DurationDef Instance = new DurationDef();

		public String Name => "Duration";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeDuration Type => TypeDuration.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
