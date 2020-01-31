using System;
using JetBrains.Annotations;
using Newtonsoft.Json;

namespace Deltix.Luminary
{
	[JsonObject(ItemRequired = Required.Always), JsonArray(false)]
	internal class ProjectSource
	{
		[NotNull, JsonProperty(Required = Required.Always)]
		public String Directory { get; set; } = ".";

		[NotNull, JsonProperty(Required = Required.DisallowNull)]
		public String[] Include { get; set; } = { "**/*.lux" };

		[NotNull, JsonProperty(Required = Required.DisallowNull)]
		public String[] Exclude { get; set; } = new String[0];
	}
}
