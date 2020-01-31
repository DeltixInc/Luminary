using System;

namespace Deltix.Luminary
{
	public class NameValuePair
	{
		public NameValuePair(String name, Literal value)
		{
			Name = name;
			Value = value;
		}

		public String Name { get; }

		public Literal Value { get; }
	}
}
