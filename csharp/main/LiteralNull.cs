using System;

namespace Deltix.Luminary
{
	public class LiteralNull : Literal
	{
		private LiteralNull()
			: base(LiteralKind.Null)
		{}

		public override String ToString()
		{
			return "null";
		}

		public static readonly LiteralNull Instance = new LiteralNull();
	}
}
