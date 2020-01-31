using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	public class TypeFunction : Type
	{
		public TypeFunction([NotNull] List<Type> argumentTypes, [NotNull] Type returnType)
			: base(TypeKind.Function)
		{
			ArgumentTypes = argumentTypes;
			ReturnType = returnType;
		}

		[NotNull]
		public List<Type> ArgumentTypes { get; }

		[NotNull]
		public Type ReturnType { get; }

		public override Int32 GetHashCode()
		{
			return ArgumentTypes.Aggregate(base.GetHashCode() ^ ReturnType.GetHashCode(),
				(current, type) => current ^ type.GetHashCode());
		}

		public Boolean Equals(TypeFunction that)
		{
			if (ArgumentTypes.Count != that?.ArgumentTypes.Count)
				return false;
			if (!ReturnType.Equals(that.ReturnType))
				return false;
			for (Int32 i = 0; i < ArgumentTypes.Count; i += 1)
				if (!ArgumentTypes[i].Equals(that.ArgumentTypes[i]))
					return false;
			return true;
		}

		public override Boolean Equals(Object that)
		{
			return Equals(that as TypeFunction);
		}

		public override String ToString()
		{
			StringBuilder builder = new StringBuilder("Function");
			builder.Append("<");
			foreach (Type argumentType in ArgumentTypes)
				builder.Append($"{argumentType}, ");
			builder.Append($"{ReturnType}>");
			return builder.ToString();
		}
	}
}
