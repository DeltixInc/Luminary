using System;
using System.IO;
using System.Text;

namespace Deltix.Luminary
{
	public class CodeWriter
	{
		private readonly StringBuilder _buffer = new StringBuilder(0x1000);
		private readonly String _indentString;
		private UInt32 _indentLevel;
		private Boolean _isNewLine = true;

		public CodeWriter()
		{
			_indentString = "\t";
		}

		public CodeWriter(Int32 tabSize)
		{
			if (tabSize < 2 || tabSize > 16)
				throw new ArgumentOutOfRangeException(nameof(tabSize), "Tabulation size should be within range 2..16.");
			_indentString = new StringBuilder().Append(' ', tabSize).ToString();
		}

		public void Indent()
		{
			_indentLevel += 1;
		}

		public void Dedent()
		{
			if (_indentLevel == 0)
				throw new InvalidOperationException("Cannot decrease indentation anymore.");

			_indentLevel -= 1;
		}

		public void NewLine()
		{
			_buffer.AppendLine();
			_isNewLine = true;
		}

		public void Write(String text)
		{
			if (text.Contains("\r"))
				throw new ArgumentException("Text should not contain '\\r' character.");

			String[] lines = text.Split('\n');
			for (Int32 i = 0; i < lines.Length; i += 1)
			{
				String line = lines[i];

				if (line.Length > 0 && _isNewLine)
					for (Int32 j = 0; j < _indentLevel; j += 1)
						_buffer.Append(_indentString);

				_buffer.Append(line.Replace("\t", _indentString));
				if (i + 1 < lines.Length)
					_buffer.AppendLine();
				_isNewLine = (i + 1 < lines.Length) || (line.Length == 0);
			}
		}

		public void Write(String format, Object arg0)
		{
			Write(String.Format(format, arg0));
		}

		public void Write(String format, Object arg0, Object arg1)
		{
			Write(String.Format(format, arg0, arg1));
		}

		public void Write(String format, Object arg0, Object arg1, Object arg2)
		{
			Write(String.Format(format, arg0, arg1, arg2));
		}

		public void Write(String format, params Object[] args)
		{
			Write(String.Format(format, args));
		}

		public void WriteLine(String text)
		{
			Write(text);
			NewLine();
		}

		public void WriteLine(String format, Object arg0)
		{
			WriteLine(String.Format(format, arg0));
		}

		public void WriteLine(String format, Object arg0, Object arg1)
		{
			WriteLine(String.Format(format, arg0, arg1));
		}

		public void WriteLine(String format, Object arg0, Object arg1, Object arg2)
		{
			WriteLine(String.Format(format, arg0, arg1, arg2));
		}

		public void WriteLine(String format, params Object[] args)
		{
			WriteLine(String.Format(format, args));
		}

		public void Clear()
		{
			_buffer.Clear();
			_indentLevel = 0;
			_isNewLine = true;
		}

		public void SaveTo(StreamWriter stream)
		{
			if (!_isNewLine)
				NewLine();
			stream.Write(_buffer.ToString());
		}
	}
}
