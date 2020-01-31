using System;
using System.Collections.Generic;
using System.IO;
using JetBrains.Annotations;

namespace Deltix.Luminary
{
	/// <summary>
	/// Represents an entire Luminary project.
	/// </summary>
	public class ProjectDef
	{
		/// <summary>
		/// Initializes a new instance of <see cref="ProjectDef"/> with specified project file path.
		/// </summary>
		/// <param name="directory">Path to the project directory.</param>
		/// <param name="fileName">Name of the project file.</param>
		public ProjectDef([CanBeNull] String directory, [CanBeNull] String fileName)
		{
			Directory = directory;
			FileName = fileName;
		}

		/// <summary>
		/// Project file name. Can be <c>null</c> for built-in project.
		/// </summary>
		[CanBeNull]
		public String FileName { get; }

		/// <summary>
		/// Path to the project's directory. Can be <c>null</c> for built-in project.
		/// </summary>
		[CanBeNull]
		public String Directory { get; }

		/// <summary>
		/// Collection of referenced projects.
		/// </summary>
		[NotNull]
		public Dictionary<String, ProjectDef> References { get; } = new Dictionary<String, ProjectDef>();

		/// <summary>
		/// Collection of files belonging to this project.
		/// </summary>
		[NotNull]
		public SortedDictionary<String, NamespaceDef> Namespaces { get; } = new SortedDictionary<String, NamespaceDef>();

		/// <summary>
		/// Computes the full path to the project file.
		/// </summary>
		/// <returns>Returns the full path to the project file. If project is a built-in one - <c>null</c> is returned.</returns>
		[CanBeNull]
		public String GetPath()
		{
			return Directory != null && FileName != null ? Path.Combine(Directory, FileName) : null;
		}
	}
}
