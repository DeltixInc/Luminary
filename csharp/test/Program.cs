using System;
using System.Collections.Generic;
using System.IO;

namespace Deltix.Luminary.Test
{
	class Program
	{
		private static void SplitPattern(String pattern, out String head, out String tail)
		{
			Int32 index = pattern.IndexOf("**", StringComparison.Ordinal);
			if (index < 0)
			{
				head = null;
				tail = pattern;
				return;
			}

			if (index > 0 && pattern[index - 1] != '/')
				throw new InvalidOperationException($"File pattern '{pattern}' is invalid.");
			if (index + 2 < pattern.Length && pattern[index + 2] != '/')
				throw new InvalidOperationException($"File pattern '{pattern}' is invalid.");

			head = index > 0 ? pattern.Substring(0, index - 1) : String.Empty;
			tail = pattern.Substring(index + 3);
		}

		private static HashSet<String> EnumerationFiles(String directory, String includePattern, String excludePattern)
		{
			// TODO: Exclude pattern is currently not implemented.
			HashSet<String> files = new HashSet<String>();

			String includeHead, includeTail;
			SplitPattern(includePattern, out includeHead, out includeTail);
			if (includeHead == null)
			{
				files.UnionWith(Directory.EnumerateFiles(directory, includeTail));
				return files;
			}

			if (includeHead.Length != 0)
			{
				foreach (String subdirectory in Directory.EnumerateDirectories(directory, "*", SearchOption.AllDirectories))
				{
				}
			}
			else
			{
			}

			return files;
		}

		static void Main(String[] args)
		{
			ProjectLoader loader = new ProjectLoader();
			ProjectDef projectDef = loader.Load("samples/Test.json");
		}
	}
}
