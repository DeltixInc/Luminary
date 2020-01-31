using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public abstract class CompositeTypeScope<TProperty, TType> : CompositeTypeDef<TProperty>, ITypeScope, ITypeDef<TType>
		where TType : Type
	{
		protected CompositeTypeScope([NotNull] String name, [NotNull] ITypeScope parent, [CanBeNull, ItemNotNull] List<String> comments)
			: base(name, comments)
		{
			Parent = parent;
			File = parent.File;

			DefinedClasses = new List<ClassDef>();
			DefinedEnumerations = new List<EnumerationDef>();
			DefinedInterfaces = new List<InterfaceDef>();
			DefinedDecorators = new List<DecoratorDef>();
		}

		public List<ClassDef> DefinedClasses { get; }

		public List<InterfaceDef> DefinedInterfaces { get; }

		public List<EnumerationDef> DefinedEnumerations { get; }

		public List<DecoratorDef> DefinedDecorators { get; }

		[NotNull]
		public FileDef File { get; }

		[NotNull]
		public ITypeScope Parent { get; }

		[NotNull]
		public abstract TType Type { get; }
	}
}
