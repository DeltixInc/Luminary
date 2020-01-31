using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class LiteralList : Literal, ILiteral<List<Literal>>
	{
		public LiteralList()
			: base(LiteralKind.List)
		{
			Value = new List<Literal>();
		}

		public List<Literal> Value { get; }

		public override String ToString()
		{
			return $"{{{String.Join(", ", Value)}}}";
		}
	}
}
