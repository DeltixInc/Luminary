using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	/// <summary>
	/// Definition of interface's method.
	/// </summary>
	public class InterfaceMethodDef : ItemDef
	{
		/// <summary>
		/// Constructs instance of this class with given parameters.
		/// </summary>
		/// <param name="name">Name of the interface method.</param>
		/// <param name="returnType">Return type of the method. <c>null</c> if method does not return anything.</param>
		/// <param name="owner">Interface containing this method.</param>
		public InterfaceMethodDef([NotNull] String name, [CanBeNull] Type returnType, [NotNull] InterfaceDef owner, [CanBeNull, ItemNotNull] List<String> comments = null)
			: base(name, comments)
		{
			ReturnType = returnType;
			Owner = owner;
			Parameters = new List<FormalParameterDef>();
			FullName = $"{owner.FullName}.{name}";
		}

		/// <summary>
		/// Method's return type. Can be <c>null</c>, indicating that method does not have return value.
		/// </summary>
		[CanBeNull]
		public Type ReturnType { get; }

		/// <summary>
		/// List of formal parameters of the method.
		/// </summary>
		[NotNull, ItemNotNull]
		public List<FormalParameterDef> Parameters { get; }

		/// <summary>
		/// Interface containing the definition of this method.
		/// </summary>
		public InterfaceDef Owner { get; }

		/// <summary>
		/// Full name of the method.
		/// </summary>
		public override String FullName { get; }
	}
}
