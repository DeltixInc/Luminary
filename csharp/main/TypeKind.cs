using System;

namespace Deltix.Luminary
{
	public enum TypeKind
	{
		Type,
		Boolean,
		Int8,
		UInt8,
		Int16,
		UInt16,
		Int32,
		UInt32,
		Int64,
		UInt64,
		Float32,
		Float64,
		Decimal,
		Text,
		Data,
		Timestamp,
		Duration,
		Time,
		Date,
		// ReSharper disable once InconsistentNaming
		UUID,
		Nullable,
		List,
		Map,
		Action,
		Function,
		Enumeration,
		Interface,
		Class,
		Decorator
	}

	public static class TypeKindExtensions
	{
		public static IntegralType ToIntegralType(this TypeKind type)
		{
			switch (type)
			{
				case TypeKind.Int8:
				case TypeKind.UInt8:
				case TypeKind.Int16:
				case TypeKind.UInt16:
				case TypeKind.Int32:
				case TypeKind.UInt32:
				case TypeKind.Int64:
				case TypeKind.UInt64:
					return (IntegralType)(Int32)type;

				default:
					throw new InvalidOperationException($"Type {type} is not integral.");
			}
		}
	}
}
