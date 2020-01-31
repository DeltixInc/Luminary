using System;
using System.Collections.Generic;
using System.Globalization;
using Antlr4.Runtime;
using Antlr4.Runtime.Misc;
using Antlr4.Runtime.Tree;

namespace Deltix.Luminary.Implementation
{
	internal class LuminaryListener : LuminaryParserBaseListener
	{
		private readonly CommonTokenStream _tokens;
		private Int32 _numberOfErrors;

		private readonly Stack<ITypeContainerNode> _types = new Stack<ITypeContainerNode>();

		private ClassNode _class;
		private ClassPropertyNode _classProperty;
		private InterfaceNode _interface;
		private InterfacePropertyNode _interfaceProperty;
		private EnumerationNode _enumeration;
		private EnumerationMemberNode _enumerationMember;
		private DecoratorNode _decorator;
		private DecoratorPropertyNode _decoratorProperty;
		private InterfaceMethodNode _interfaceMethod;
		private FormalParameterNode _formalParameter;

		private List<DecoratorValueNode> _appliedDecorators;
		private List<NameValuePair> _appliedDecoratorArguments;

		private readonly Stack<LiteralList> _lists = new Stack<LiteralList>();
		private Literal _literal;

		public LuminaryListener(CommonTokenStream tokens)
		{
			_tokens = tokens;
			_numberOfErrors = 0;
		}

		public override void EnterProtocol([NotNull] LuminaryParser.ProtocolContext context)
		{
			File = new FileNode();
			_types.Push(File);
		}

		public override void EnterClassDefinition([NotNull] LuminaryParser.ClassDefinitionContext context)
		{
			_class = new ClassNode(context.IDENTIFIER().GetText(), context.FINAL() != null, ExtractSupertypes(context.supertypeList()),
				ExtractDocumentation(context));
			_types.Push(_class);
		}

		private void RestoreCurrentTypeContainer()
		{
			if (_types.Peek() is ClassNode)
				_class = _types.Peek() as ClassNode;
			if (_types.Peek() is InterfaceNode)
				_interface = _types.Peek() as InterfaceNode;
			if (_types.Peek() is DecoratorNode)
				_decorator = _types.Peek() as DecoratorNode;
		}

		public override void ExitClassDefinition([NotNull] LuminaryParser.ClassDefinitionContext context)
		{
			_types.Pop();
			_types.Peek().DefinedClasses.Add(Swap(ref _class, null));
			RestoreCurrentTypeContainer();
		}

		public override void EnterInterfaceDefinition([NotNull] LuminaryParser.InterfaceDefinitionContext context)
		{
			_interface = new InterfaceNode(context.IDENTIFIER().GetText(), ExtractDocumentation(context),
				ExtractSupertypes(context.supertypeList()));
			_types.Push(_interface);
		}

		public override void ExitInterfaceDefinition([NotNull] LuminaryParser.InterfaceDefinitionContext context)
		{
			_types.Pop();
			_types.Peek().DefinedInterfaces.Add(Swap(ref _interface, null));
			RestoreCurrentTypeContainer();
		}

		public override void EnterDecoratorDefinition(LuminaryParser.DecoratorDefinitionContext context)
		{
			_decorator = new DecoratorNode(context.IDENTIFIER().GetText(), ExtractDocumentation(context));
			_types.Push(_decorator);
		}

		public override void ExitDecoratorDefinition(LuminaryParser.DecoratorDefinitionContext context)
		{
			_types.Pop();
			_types.Peek().DefinedDecorators.Add(Swap(ref _decorator, null));
			RestoreCurrentTypeContainer();
		}

		public override void EnterEnumerationDefinition([NotNull] LuminaryParser.EnumerationDefinitionContext context)
		{
			_enumeration = new EnumerationNode(context.IDENTIFIER().GetText(),
				context.enumerationUnderlyingType()?.GetText() ?? "Int32", ExtractDocumentation(context));
		}

		public override void ExitEnumerationDefinition([NotNull] LuminaryParser.EnumerationDefinitionContext context)
		{
			_types.Peek().DefinedEnumerations.Add(Swap(ref _enumeration, null));
		}

		public override void ExitConstantDefinition(LuminaryParser.ConstantDefinitionContext context)
		{
			ConstantNode constantNode = new ConstantNode(context.IDENTIFIER().GetText(), context.type().GetText(), _literal,
				ExtractDocumentation(context));
			if (context.Parent is LuminaryParser.ClassEntryContext)
				((ClassNode) _types.Peek()).DefinedConstants.Add(constantNode);
			else if (context.Parent is LuminaryParser.DecoratorEntryContext)
				((DecoratorNode)_types.Peek()).DefinedConstants.Add(constantNode);
		}

		public override void EnterClassProperty([NotNull] LuminaryParser.ClassPropertyContext context)
		{
			_classProperty = new ClassPropertyNode(context.IDENTIFIER().GetText(), context.type().GetText(),
				context.OVERRIDE() != null, context.FINAL() != null, ExtractDocumentation(context));
		}

		public override void EnterInterfaceProperty([NotNull] LuminaryParser.InterfacePropertyContext context)
		{
			_interfaceProperty = new InterfacePropertyNode(context.IDENTIFIER().GetText(),
				context.type().GetText(), context.OVERRIDE() != null, context.GET() != null, context.SET() != null, ExtractDocumentation(context));
		}

		public override void EnterEnumerationMember([NotNull] LuminaryParser.EnumerationMemberContext context)
		{
			_enumerationMember = new EnumerationMemberNode(context.IDENTIFIER().GetText(), ExtractDocumentation(context));
		}

		public override void ExitEnumerationMember([NotNull] LuminaryParser.EnumerationMemberContext context)
		{
			_enumerationMember.Value = Swap(ref _literal, null);
			_enumeration.Members.Add(Swap(ref _enumerationMember, null));
		}

		public override void EnterDecoratorProperty(LuminaryParser.DecoratorPropertyContext context)
		{
			_decoratorProperty = new DecoratorPropertyNode(context.IDENTIFIER().GetText(), context.type().GetText(),
				ExtractDocumentation(context));
		}

		public override void ExitDecoratorProperty(LuminaryParser.DecoratorPropertyContext context)
		{
			_decorator.Properties.Add(Swap(ref _decoratorProperty, null));
		}

		public override void EnterDecorators([NotNull] LuminaryParser.DecoratorsContext context)
		{
			_appliedDecorators = new List<DecoratorValueNode>();
		}

		public override void EnterDecorator([NotNull] LuminaryParser.DecoratorContext context)
		{
			_appliedDecoratorArguments = new List<NameValuePair>();
		}

		public override void ExitNormalDecorator(LuminaryParser.NormalDecoratorContext context)
		{
			_appliedDecorators.Add(new DecoratorValueNode(context.qualifiedName().GetText(),
				Swap(ref _appliedDecoratorArguments, null)));
		}

		public override void ExitMarkerDecorator(LuminaryParser.MarkerDecoratorContext context)
		{
			_appliedDecorators.Add(new DecoratorValueNode(context.qualifiedName().GetText(), null));
		}

		public override void ExitSingleElementDecorator(LuminaryParser.SingleElementDecoratorContext context)
		{
			_appliedDecorators.Add(new DecoratorValueNode(context.qualifiedName().GetText(), new List<NameValuePair> {new NameValuePair("Value", _literal)}));
			_literal = null;
		}

		public override void ExitKeyValuePair([NotNull] LuminaryParser.KeyValuePairContext context)
		{
			_appliedDecoratorArguments.Add(new NameValuePair(context.IDENTIFIER().GetText(), Swap(ref _literal, null)));
		}

		public override void ExitDecorators([NotNull] LuminaryParser.DecoratorsContext context)
		{
			if (context.Parent is LuminaryParser.ClassPropertyContext)
				_classProperty.Decorators.AddRange(Swap(ref _appliedDecorators, null));
			else if (context.Parent is LuminaryParser.ClassDefinitionContext)
				_class.Decorators.AddRange(Swap(ref _appliedDecorators, null));
			else if (context.Parent is LuminaryParser.InterfacePropertyContext)
				_interfaceProperty.Decorators.AddRange(Swap(ref _appliedDecorators, null));
			else if (context.Parent is LuminaryParser.InterfaceDefinitionContext)
				_interface.Decorators.AddRange(Swap(ref _appliedDecorators, null));
			else if (context.Parent is LuminaryParser.EnumerationMemberContext)
				_enumerationMember.Decorators.AddRange(Swap(ref _appliedDecorators, null));
			else if (context.Parent is LuminaryParser.EnumerationDefinitionContext)
				_enumeration.Decorators.AddRange(Swap(ref _appliedDecorators, null));
			else if (context.Parent is LuminaryParser.DecoratorPropertyContext)
				_decoratorProperty.Decorators.AddRange(Swap(ref _appliedDecorators, null));
			else if (context.Parent is LuminaryParser.DecoratorDefinitionContext)
				_decorator.Decorators.AddRange(Swap(ref _appliedDecorators, null));
			else if (context.Parent is LuminaryParser.FormalArrayParameterContext)
				_formalParameter.Decorators.AddRange(Swap(ref _appliedDecorators, null));
			else if (context.Parent is LuminaryParser.FormalFixedParameterContext)
				_formalParameter.Decorators.AddRange(Swap(ref _appliedDecorators, null));
			else if (context.Parent is LuminaryParser.InterfaceMethodContext)
				_interfaceMethod.Decorators.AddRange(Swap(ref _appliedDecorators, null));
		}

		public override void EnterImportEverything(LuminaryParser.ImportEverythingContext context)
		{
			File.Imports.Add(ImportNode.Everything(context.qualifiedName().GetText()));
		}

		public override void EnterImportType(LuminaryParser.ImportTypeContext context)
		{
			File.Imports.Add(ImportNode.Type(context.qualifiedName().GetText(), context.IDENTIFIER().GetText()));
		}

		public override void EnterImportTypeWithAlias(LuminaryParser.ImportTypeWithAliasContext context)
		{
			File.Imports.Add(ImportNode.TypeWithAlias(context.qualifiedName().GetText(), context.IDENTIFIER(0).GetText(), context.IDENTIFIER(1).GetText()));
		}

		public override void ExitNamespace([NotNull] LuminaryParser.NamespaceContext context)
		{
			File.Namespace = context.qualifiedName().GetText();
		}

		public override void EnterArray(LuminaryParser.ArrayContext context)
		{
			_lists.Push(new LiteralList());
		}

		public override void ExitArray(LuminaryParser.ArrayContext context)
		{
			LiteralList list = _lists.Pop();
			if (_lists.Count > 0)
				_lists.Peek().Value.Add(list);
			else
				_literal = list;
			TryConsumeLiteral(context.Parent);
		}

		public override void EnterFormalArrayParameter(LuminaryParser.FormalArrayParameterContext context)
		{
			_formalParameter = new FormalParameterNode(context.IDENTIFIER().GetText(),
				context.type().GetText(), true, ExtractDocumentation(context));
		}

		public override void ExitFormalArrayParameter(LuminaryParser.FormalArrayParameterContext context)
		{
			_interfaceMethod.FormalParameters.Add(Swap(ref _formalParameter, null));
		}

		public override void EnterFormalFixedParameter(LuminaryParser.FormalFixedParameterContext context)
		{
			_formalParameter = new FormalParameterNode(context.IDENTIFIER().GetText(),
				context.type().GetText(), false, ExtractDocumentation(context));
		}

		public override void ExitFormalFixedParameter(LuminaryParser.FormalFixedParameterContext context)
		{
			_interfaceMethod.FormalParameters.Add(Swap(ref _formalParameter, null));
		}

		public override void EnterInterfaceMethod(LuminaryParser.InterfaceMethodContext context)
		{
			_interfaceMethod = new InterfaceMethodNode(context.IDENTIFIER().GetText(),
				context.returnType().VOID() != null ? null : context.returnType().GetText(), ExtractDocumentation(context));
		}

		public override void ExitInterfaceMethod(LuminaryParser.InterfaceMethodContext context)
		{
			_interface.Methods.Add(Swap(ref _interfaceMethod, null));
		}

		private void TryConsumeLiteral(RuleContext parent)
		{
			if (_lists.Count > 0)
				_lists.Peek().Value.Add(Swap(ref _literal, null));
			else
			{
				if (parent is LuminaryParser.DecoratorPropertyContext)
					_decoratorProperty.Default = Swap(ref _literal, null);
				else if (parent is LuminaryParser.ClassPropertyContext)
					_classProperty.Default = Swap(ref _literal, null);
			}
		}

		public override void ExitString([NotNull] LuminaryParser.StringContext context)
		{
			String text = context.STRING_LITERAL().GetText();
			_literal = new LiteralText(text.Substring(1, text.Length - 2));
			TryConsumeLiteral(context.Parent);
		}

		public override void ExitInteger([NotNull] LuminaryParser.IntegerContext context)
		{
			_literal = ExtractIntegralLiteral(context.GetText());
			TryConsumeLiteral(context.Parent);
		}

		public override void ExitFloat(LuminaryParser.FloatContext context)
		{
			_literal = ExtractBinaryFloatingPointLiteral(context.GetText());
			TryConsumeLiteral(context.Parent);
		}

		public override void ExitDecimal(LuminaryParser.DecimalContext context)
		{
			_literal = ExtractDecimalFloatingPointLiteral(context.GetText());
			TryConsumeLiteral(context.Parent);
		}

		public override void ExitTrue([NotNull] LuminaryParser.TrueContext context)
		{
			_literal = LiteralBoolean.True;
			TryConsumeLiteral(context.Parent);
		}

		public override void ExitFalse(LuminaryParser.FalseContext context)
		{
			_literal = LiteralBoolean.False;
			TryConsumeLiteral(context.Parent);
		}

		public override void ExitNull(LuminaryParser.NullContext context)
		{
			_literal = LiteralNull.Instance;
			TryConsumeLiteral(context.Parent);
		}

		public override void ExitMemberReference(LuminaryParser.MemberReferenceContext context)
		{
			_literal = new LiteralUnresolved(LiteralKind.Constant, context.GetText());
			TryConsumeLiteral(context.Parent);
		}

		public override void ExitTypeReference(LuminaryParser.TypeReferenceContext context)
		{
			_literal = new LiteralUnresolved(LiteralKind.Type, context.type().GetText());
			TryConsumeLiteral(context.Parent);
		}

		public override void ExitClassProperty([NotNull] LuminaryParser.ClassPropertyContext context)
		{
			_class.Properties.Add(Swap(ref _classProperty, null));
		}

		public override void ExitInterfaceProperty(LuminaryParser.InterfacePropertyContext context)
		{
			_interface.Properties.Add(Swap(ref _interfaceProperty, null));
		}

		public override void ExitOption([NotNull] LuminaryParser.OptionContext context)
		{
			ITypeContainerNode top = _types.Peek();
			if (!(top is FileNode))
				throw new Exception(@"Options must be specified at the top level.");
			(top as FileNode).Options.Add(new NameValuePair(context.IDENTIFIER().GetText(), Swap(ref _literal, null)));
		}

		public override void ExitProtocol([NotNull] LuminaryParser.ProtocolContext context)
		{
			if (File != _types.Pop() as FileNode || _types.Count != 0)
				throw new InvalidOperationException(@"Something went completely wrong.");
		}

		public override void VisitErrorNode([NotNull] IErrorNode node)
		{
			_numberOfErrors += 1;
		}

		public Int32 NumberOfErrors => _numberOfErrors;

		public FileNode File { get; private set; }

		private List<String> ExtractSupertypes(LuminaryParser.SupertypeListContext context)
		{
			if (context == null)
				return null;

			List<String> supertypes = new List<String>();
			while (context != null)
			{
				supertypes.Add(context.type().GetText());
				context = context.supertypeList();
			}

			return supertypes;
		}

		private List<String> ExtractDocumentation(ParserRuleContext context)
		{
			IList<IToken> tokens = _tokens.GetHiddenTokensToLeft(context.Start.TokenIndex);
			if (tokens == null)
				return null;

			List<String> comments = null;
			foreach (IToken token in tokens)
				if (token.Text.StartsWith("///"))
				{
					if (comments == null)
						comments = new List<String>();
					comments.Add(token.Text.Substring(3).Trim());
				}

			return comments;
		}

		private static T ParseInteger<T>(String text, Func<String, Int32, T> parser)
		{
			if (text.StartsWith("0x"))
				return parser.Invoke(text.Replace("0x", ""), 16);
			if (text.StartsWith("0o"))
				return parser.Invoke(text.Replace("0o", ""), 8);
			return text.StartsWith("0b") ? parser.Invoke(text.Replace("0b", ""), 2) : parser.Invoke(text, 10);
		}

		public LiteralInteger ExtractIntegralLiteral(String text)
		{
			text = text.Replace("_", "");

			Boolean negative = false;
			if (text.StartsWith("-"))
			{
				negative = true;
				text = text.Substring(1);
			}

			if (text.EndsWith("u64"))
			{
				if (negative)
					throw new InvalidOperationException("Value of type 'UInt64' cannot be negative.");
				return new LiteralUInt64(ParseInteger(text.Substring(0, text.Length - 3), Convert.ToUInt64));
			}
			if (text.EndsWith("u32"))
			{
				if (negative)
					throw new InvalidOperationException("Value of type 'UInt32' cannot be negative.");
				return new LiteralUInt32(ParseInteger(text.Substring(0, text.Length - 3), Convert.ToUInt32));
			}
			if (text.EndsWith("u16"))
			{
				if (negative)
					throw new InvalidOperationException("Value of type 'UInt16' cannot be negative.");
				return new LiteralUInt16(ParseInteger(text.Substring(0, text.Length - 3), Convert.ToUInt16));
			}
			if (text.EndsWith("u8"))
			{
				if (negative)
					throw new InvalidOperationException("Value of type 'UInt8' cannot be negative.");
				return new LiteralUInt8(ParseInteger(text.Substring(0, text.Length - 2), Convert.ToByte));
			}
			if (text.EndsWith("i64"))
			{
				Int64 value = ParseInteger(text.Substring(0, text.Length - 3), Convert.ToInt64);
				return new LiteralInt64(negative ? -value : value);
			}
			if (text.EndsWith("i16"))
			{
				Int16 value = ParseInteger(text.Substring(0, text.Length - 3), Convert.ToInt16);
				return new LiteralInt16((Int16) (negative ? -value : value));
			}
			if (text.EndsWith("i8"))
			{
				SByte value = ParseInteger(text.Substring(0, text.Length - 2), Convert.ToSByte);
				return new LiteralInt8((SByte) (negative ? -value : value));
			}
			{
				Int32 value = ParseInteger(text.EndsWith("i32") ? text.Substring(0, text.Length - 3) : text, Convert.ToInt32);
				return new LiteralInt32(negative ? -value : value);
			}
		}

		private static Literal ExtractBinaryFloatingPointLiteral(String text)
		{
			text = text.Replace("_", "");

			if (text.EndsWith("f32"))
			{
				text = text.Substring(0, text.Length - 3);
				return new LiteralFloat32(Single.Parse(text, CultureInfo.InvariantCulture));
			}

			text = text.EndsWith("f64") ? text.Substring(0, text.Length - 3) : text;
			return new LiteralFloat64(Double.Parse(text, CultureInfo.InvariantCulture));
		}

		private static Literal ExtractDecimalFloatingPointLiteral(String text)
		{
			text = text.Replace("_", "").Substring(0, text.Length - 3);
			return new LiteralDecimal(Decimal.Parse(text, CultureInfo.InvariantCulture));
		}

		private static T Swap<T>(ref T variable, T newValue)
		{
			T oldValue = variable;
			variable = newValue;
			return oldValue;
		}
	}
}
