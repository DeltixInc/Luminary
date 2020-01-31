using System;

namespace Deltix.Luminary
{
	/// <summary>Represents the builtin Luminary type <c>Type</c>.</summary>
	public sealed class TypeType : Type, IEquatable<TypeType>
	{
		private TypeType()
			: base(TypeKind.Type)
		{}

		/// <summary>Singleton describing the builtin Luminary type <c>Type</c>.</summary>
		public static readonly TypeType Instance = new TypeType();

		/// <summary>Serves as the default hash function.</summary>
		/// <returns>A hash code for the current object.</returns>
		public override Int32 GetHashCode() => base.GetHashCode();

		/// <summary>Determines whether the specified object is equal to the current object.</summary>
		/// <returns><c>true</c>if the specified object is equal to the current object; otherwise, <c>false</c>.</returns>
		/// <param name="obj">The object to compare with the current object. </param>
		public override Boolean Equals(Object obj) => Equals(obj as TypeType);

		/// <summary>Indicates whether the current object is equal to another object of the same type.</summary>
		/// <returns>true if the current object is equal to the <paramref name="other" /> parameter; otherwise, false.</returns>
		/// <param name="other">An object to compare with this object.</param>
		public Boolean Equals(TypeType other) => other != null;

		/// <summary>Returns a string that represents the current object.</summary>
		/// <returns>A string that represents the current object.</returns>
		public override String ToString() => "Type";
	}
}
