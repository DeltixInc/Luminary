using System.Collections.Generic;

namespace Deltix.Luminary
{
	public interface IDecoratable
	{
		List<Decorator> Decorators { get; }
	}
}
