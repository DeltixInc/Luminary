using System;
using System.Collections.Generic;
using System.Text;

namespace Deltix.Luminary
{
	public sealed class TypeMap : Type, IEquatable<TypeMap>
	{
		private readonly String _string;

		public TypeMap(Type keyType, Type valueType)
			: base(TypeKind.Map)
		{
			KeyType = keyType;
			ValueType = valueType;
			_string = $"Map<{KeyType}, {ValueType}>";
		}

		public Type KeyType { get; }
		public Type ValueType { get; }

		/// <summary>Serves as the default hash function.</summary>
		/// <returns>A hash code for the current object.</returns>
		public override Int32 GetHashCode() => base.GetHashCode() ^ KeyType.GetHashCode() ^ ValueType.GetHashCode();

		/// <summary>Determines whether the specified object is equal to the current object.</summary>
		/// <returns><c>true</c>if the specified object is equal to the current object; otherwise, <c>false</c>.</returns>
		/// <param name="obj">The object to compare with the current object. </param>
		public override Boolean Equals(Object obj) => Equals(obj as TypeList);

		/// <summary>Indicates whether the current object is equal to another object of the same type.</summary>
		/// <returns>true if the current object is equal to the <paramref name="other" /> parameter; otherwise, false.</returns>
		/// <param name="other">An object to compare with this object.</param>
		public Boolean Equals(TypeMap other) => other != null && KeyType.Equals(other.KeyType) && ValueType.Equals(other.ValueType);

		/// <summary>Returns a string that represents the current object.</summary>
		/// <returns>A string that represents the current object.</returns>
		public override String ToString() => _string;
	}
}
