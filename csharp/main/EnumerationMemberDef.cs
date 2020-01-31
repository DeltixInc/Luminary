using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public sealed class EnumerationMemberDef : ItemDef
	{
		internal EnumerationMemberDef([NotNull] EnumerationDef owner, [NotNull] String name, [CanBeNull, ItemNotNull] List<String> comments = null)
			: base(name, comments)
		{
			Owner = owner;

			FullName = $"{owner.FullName}.{Name}";

			owner.Members.Add(this);
		}

		public EnumerationMemberDef([NotNull] EnumerationDef owner, [NotNull] String name, [NotNull] LiteralInteger value, [CanBeNull, ItemNotNull] List<String> comments = null)
			: base(name, comments)
		{
			Value = value;
			Owner = owner;

			FullName = $"{owner.FullName}.{Name}";

			owner.Members.Add(this);
		}

		[NotNull]
		public EnumerationDef Owner { get; }

		internal Literal RawValue { get; set; }

		public LiteralInteger Value { get; internal set; }

		[NotNull]
		public ILiteral<T> GetValue<T>()
		{
			return (ILiteral<T>) Value;
		}

		public override String FullName { get; }
	}
}
