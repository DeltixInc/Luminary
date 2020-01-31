using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class TimestampDef : IConstantScope, ITypeDef<TypeTimestamp>
	{
		private TimestampDef()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeTimestamp.Instance, new LiteralTimestamp(LiteralTimestamp.MinValueAsString)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeTimestamp.Instance, new LiteralTimestamp(LiteralTimestamp.MaxValueAsString)));
		}

		public static TimestampDef Instance = new TimestampDef();

		public String Name => "Timestamp";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeTimestamp Type => TypeTimestamp.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
