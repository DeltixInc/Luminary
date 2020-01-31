using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public abstract class ClassOrInterfaceDef<TProperty, TType> : CompositeTypeScope<TProperty, TType>
		where TType : Type
	{
		protected ClassOrInterfaceDef([NotNull] String name, [NotNull] ITypeScope parent, [CanBeNull, ItemNotNull] List<String> comments)
			: base(name, parent, comments)
		{
			SuperInterfaces = new List<InterfaceDef>();
		}

		[NotNull, ItemNotNull]
		public List<InterfaceDef> SuperInterfaces { get; }
	}
}
