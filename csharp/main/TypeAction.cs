using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Deltix.Luminary
{
	public class TypeAction : Type
	{
		public TypeAction(List<Type> argumentTypes)
			: base(TypeKind.Action)
		{
			ArgumentTypes = argumentTypes;
		}

		public List<Type> ArgumentTypes { get; }

		public override Int32 GetHashCode()
		{
			return ArgumentTypes.Aggregate(base.GetHashCode(), (current, type) => current ^ type.GetHashCode());
		}

		public Boolean Equals(TypeAction that)
		{
			if (ArgumentTypes.Count != that?.ArgumentTypes.Count)
				return false;
			for (Int32 i = 0; i < ArgumentTypes.Count; i += 1)
				if (!ArgumentTypes[i].Equals(that.ArgumentTypes[i]))
					return false;
			return true;
		}

		public override Boolean Equals(Object that)
		{
			return Equals(that as TypeAction);
		}

		public override String ToString()
		{
			StringBuilder builder = new StringBuilder("Action");
			if (ArgumentTypes.Count <= 0)
				return builder.ToString();
			builder.Append($"<{ArgumentTypes[0]}");
			foreach (Type argumentType in ArgumentTypes.Skip(1))
				builder.Append($", {argumentType}");
			builder.Append(">");
			return builder.ToString();
		}
	}
}
