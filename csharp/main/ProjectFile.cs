using System;
using JetBrains.Annotations;
using Newtonsoft.Json;

namespace Deltix.Luminary
{
	[JsonObject]
	internal class ProjectFile
	{
		[NotNull, JsonProperty(Required = Required.Always)]
		public ProjectSource[] Sources { get; set; } = new ProjectSource[0];

		[NotNull, JsonProperty(Required = Required.DisallowNull)]
		public String[] References { get; set; } = new String[0];

		[JsonIgnore]
		public String Path { get; set; }
	}
}
