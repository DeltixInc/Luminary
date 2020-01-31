using System;

#pragma warning disable 659

namespace Deltix.Luminary
{
	public sealed class TypeClass : TypeCustom, IEquatable<TypeClass>
	{
		public TypeClass(ClassDef definition)
			: base(TypeKind.Class)
		{
			Definition = definition;
		}

		public ClassDef Definition { get; }

		/// <summary>Determines whether the specified object is equal to the current object.</summary>
		/// <returns><c>true</c>if the specified object is equal to the current object; otherwise, <c>false</c>.</returns>
		/// <param name="obj">The object to compare with the current object. </param>
		public override Boolean Equals(Object obj) => Equals(obj as TypeClass);

		/// <summary>Indicates whether the current object is equal to another object of the same type.</summary>
		/// <returns>true if the current object is equal to the <paramref name="other" /> parameter; otherwise, false.</returns>
		/// <param name="other">An object to compare with this object.</param>
		public Boolean Equals(TypeClass that) => that != null && Definition.Equals(that.Definition);

		/// <summary>Returns a string that represents the current object.</summary>
		/// <returns>A string that represents the current object.</returns>
		public override String ToString() => Definition.Name;

		/// <summary>File containing the definition of this type.</summary>
		public override FileDef File => Definition.File;

		/// <summary>Name of the custom type without any qualifiers (i.e. just an identifier).</summary>
		public override String Name => Definition.Name;
	}
}
