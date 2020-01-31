using System;
using System.Collections.Generic;

namespace Deltix.Luminary
{
	public class DecoratorUsage
	{
		public List<DecoratorTarget> ValidOn { get; } = new List<DecoratorTarget>();
		public Boolean Repeatable { get; set; } = false;

		public static DecoratorUsage Default()
		{
			DecoratorUsage usage = new DecoratorUsage();
			foreach (DecoratorTarget target in Enum.GetValues(typeof(DecoratorTarget)))
				usage.ValidOn.Add(target);
			return usage;
		}
	}
}
