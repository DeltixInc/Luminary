using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public sealed class DecoratorDef : CompositeTypeScope<DecoratorPropertyDef, TypeDecorator>, IConstantAndTypeContainer
	{
		public DecoratorDef([NotNull] ITypeScope parent, [NotNull] String name, [CanBeNull, ItemNotNull] List<String> comments = null)
			: base(name, parent, comments)
		{
			Type = new TypeDecorator(this);
			FullName = parent is FileDef ? Name : $"{((ItemDef) parent).FullName}.{Name}";

			parent.DefinedDecorators.Add(this);
		}

		public override TypeDecorator Type { get; }

		[NotNull, ItemNotNull]
		public List<ConstantDef> DefinedConstants { get; } = new List<ConstantDef>();

		public override String FullName { get; }
	}
}
