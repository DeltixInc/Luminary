using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class Float32Def : IConstantScope, ITypeDef<TypeFloat32>
	{
		private Float32Def()
		{
			DefinedConstants.Add(new ConstantDef(this, "MIN_VALUE", TypeFloat32.Instance, new LiteralFloat32(Single.MinValue)));
			DefinedConstants.Add(new ConstantDef(this, "MAX_VALUE", TypeFloat32.Instance, new LiteralFloat32(Single.MaxValue)));
			DefinedConstants.Add(new ConstantDef(this, "NaN", TypeFloat32.Instance, new LiteralFloat32(Single.NaN)));
			DefinedConstants.Add(new ConstantDef(this, "POSITIVE_INFINITY", TypeFloat32.Instance, new LiteralFloat32(Single.PositiveInfinity)));
			DefinedConstants.Add(new ConstantDef(this, "NEGATIVE_INFINITY", TypeFloat32.Instance, new LiteralFloat32(Single.NegativeInfinity)));
		}

		public static Float32Def Instance = new Float32Def();

		public String Name => "Float32";

		public String FullName => Name;

		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public TypeFloat32 Type => TypeFloat32.Instance;

		public FileDef File => null;

		public ITypeScope Parent => null;
	}
}
