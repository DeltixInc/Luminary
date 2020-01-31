using System;

namespace Deltix.Luminary
{
	/// <summary>Base class for all Luminary types.</summary>
	public abstract class Type : IEquatable<Type>
	{
		/// <summary>Initializes a new type of specified kind.</summary>
		/// <param name="kind">Kind of type to initialize.</param>
		protected Type(TypeKind kind)
		{
			Kind = kind;
		}

		/// <summary>Kind of type.</summary>
		public TypeKind Kind { get; }

		/// <summary>Serves as the default hash function.</summary>
		/// <returns>A hash code for the current object.</returns>
		public override Int32 GetHashCode() => Kind.GetHashCode();

		/// <summary>Determines whether the specified object is equal to the current object.</summary>
		/// <returns><c>true</c>if the specified object is equal to the current object; otherwise, <c>false</c>.</returns>
		/// <param name="obj">The object to compare with the current object. </param>
		public override Boolean Equals(Object obj) => Equals(obj as Type);

		/// <summary>Indicates whether the current object is equal to another object of the same type.</summary>
		/// <returns>true if the current object is equal to the <paramref name="other" /> parameter; otherwise, false.</returns>
		/// <param name="other">An object to compare with this object.</param>
		public Boolean Equals(Type other) => other != null && Kind == other.Kind;
	}
}
