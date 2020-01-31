using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class TimeDef : IConstantScope, ITypeDef<TypeTime>
	{
		private TimeDef()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeTime.Instance, new LiteralTime(LiteralTime.MinValueAsString)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeTime.Instance, new LiteralTime(LiteralTime.MaxValueAsString)));
		}

		public static TimeDef Instance = new TimeDef();

		public String Name => "Time";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeTime Type => TypeTime.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
