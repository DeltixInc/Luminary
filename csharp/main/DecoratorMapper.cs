using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;

namespace Deltix.Luminary
{
	public static class DecoratorMapper
	{
		/* TODO:
		public static T GetDecoratorAs<T>(this IDecoratable decoratable)
			where T : class, new()
		{
			return DecoratorMapper.MapDecorator<T>(decoratable.Decorators);
		}

		public static T MapDecorator<T>(List<Decorator> decoratorList) where T : class, new()
		{
			System.Type type = typeof(T);
			T result = new T();

			Decorator source = decoratorList.FirstOrDefault(x => x.Definition.Name.Equals(type.Name));
			if (source == null)
				return null;

			foreach (DecoratorPropertyValue pair in source.Arguments)
			{
				PropertyInfo property = type.GetProperty(pair.Definition.Name);
				Literal literal = pair.Value;

				switch (literal.Kind)
				{
					case LiteralKind.Boolean:
						if (property.PropertyType != typeof(Boolean))
							throw new InvalidCastException(
								$"Cannot cast literal type {literal.Kind} to a decorator property type {property.PropertyType} for {type.Name}.{property.Name}.");
						property.SetMethod.Invoke(result, new Object[] {literal.ValueAs<Boolean>()});
						break;

					case LiteralKind.Text:
						if (property.PropertyType != typeof(String))
							throw new InvalidCastException(
								$"Cannot cast literal type {literal.Kind} to a decorator property type {property.PropertyType} for {type.Name}.{property.Name}.");
						property.SetMethod.Invoke(result, new Object[] {literal.ValueAs<String>()});
						break;

					case LiteralKind.UInt64:
						if (property.PropertyType != typeof(UInt64))
							throw new InvalidCastException(
								$"Cannot cast literal type {literal.Kind} to a decorator property type {property.PropertyType} for {type.Name}.{property.Name}.");
						property.SetMethod.Invoke(result, new Object[] {literal.ValueAs<UInt64>()});
						break;

					case LiteralKind.UInt32:
						if (property.PropertyType != typeof(UInt32))
							throw new InvalidCastException(
								$"Cannot cast literal type {literal.Kind} to a decorator property type {property.PropertyType} for {type.Name}.{property.Name}.");
						property.SetMethod.Invoke(result, new Object[] {literal.ValueAs<UInt32>()});
						break;

					case LiteralKind.UInt16:
						if (property.PropertyType != typeof(UInt16))
							throw new InvalidCastException(
								$"Cannot cast literal type {literal.Kind} to a decorator property type {property.PropertyType} for {type.Name}.{property.Name}.");
						property.SetMethod.Invoke(result, new Object[] {literal.ValueAs<UInt16>()});
						break;

					case LiteralKind.UInt8:
						if (property.PropertyType != typeof(Byte))
							throw new InvalidCastException(
								$"Cannot cast literal type {literal.Kind} to a decorator property type {property.PropertyType} for {type.Name}.{property.Name}.");
						property.SetMethod.Invoke(result, new Object[] {literal.ValueAs<Byte>()});
						break;

					case LiteralKind.SInt32:
						if (property.PropertyType != typeof(Int32))
							throw new InvalidCastException(
								$"Cannot cast literal type {literal.Kind} to a decorator property type {property.PropertyType} for {type.Name}.{property.Name}.");
						property.SetMethod.Invoke(result, new Object[] {literal.ValueAs<Int32>()});
						break;

					case LiteralKind.SInt64:
						if (property.PropertyType != typeof(Int64))
							throw new InvalidCastException(
								$"Cannot cast literal type {literal.Kind} to a decorator property type {property.PropertyType} for {type.Name}.{property.Name}.");
						property.SetMethod.Invoke(result, new Object[] {literal.ValueAs<Int64>()});
						break;

					case LiteralKind.SInt16:
						if (property.PropertyType != typeof(Int16))
							throw new InvalidCastException(
								$"Cannot cast literal type {literal.Kind} to a decorator property type {property.PropertyType} for {type.Name}.{property.Name}.");
						property.SetMethod.Invoke(result, new Object[] {literal.ValueAs<Int16>()});
						break;

					case LiteralKind.SInt8:
						if (property.PropertyType != typeof(SByte))
							throw new InvalidCastException(
								$"Cannot cast literal type {literal.Kind} to a decorator property type {property.PropertyType} for {type.Name}.{property.Name}.");
						property.SetMethod.Invoke(result, new Object[] {literal.ValueAs<SByte>()});
						break;
				}
			}

			return result;
		}
	*/
	}
}
