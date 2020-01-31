using System;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	/// <summary>
	/// Definition of method's formal parameter.
	/// </summary>
	public class FormalParameterDef : PropertyDef<InterfaceMethodDef>
	{
		/// <summary>
		/// Constructs instance of this class with given parameters.
		/// </summary>
		/// <param name="owner">Interface method declaration this formal parameter is part of.</param>
		/// <param name="name">Name of the formal parameter.</param>
		/// <param name="type">Type of the formal parameter.</param>
		/// <param name="isParameterArray">Boolean flag that indicates whether this is a parameter array.</param>
		public FormalParameterDef([NotNull] InterfaceMethodDef owner, [NotNull] String name, [NotNull] Type type, Boolean isParameterArray)
			: base(owner, name, type)
		{
			FullName = $"{owner.FullName}:{name}";
			IsParameterArray = isParameterArray;
		}

		/// <summary>
		/// Indicates whether this is a parameter array.
		/// </summary>
		public Boolean IsParameterArray { get; }

		/// <summary>
		/// Full name of the parameter. The same as <see cref="ItemDef.Name"/>.
		/// </summary>
		public override String FullName { get; }
	}
}
