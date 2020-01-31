package deltix.luminary.implementation;

import deltix.dfp.Decimal64Utils;
import deltix.luminary.*;
import deltix.luminary.parser.LuminaryParser;
import deltix.luminary.parser.LuminaryParserBaseListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LuminaryListenerImplementation extends LuminaryParserBaseListener {
    private final CommonTokenStream tokens;
    private int numberOfErrors = 0;

    private final Stack<TypeContainerNode> types = new Stack<>();

    private FileNode fileNode;
    private ClassNode classNode;
    private ClassPropertyNode classPropertyNode;
    private InterfaceNode interfaceNode;
    private InterfacePropertyNode interfacePropertyNode;
    private EnumerationNode enumerationNode;
    private EnumerationMemberNode enumerationMemberNode;
    private DecoratorNode decoratorNode;
    private DecoratorPropertyNode decoratorPropertyNode;
    private FormalParameterNode formalParameterNode;
    private InterfaceMethodNode interfaceMethodNode;

    private List<DecoratorValueNode> decorators;
    private List<NameValuePair> decoratorArguments;
    private final Stack<LiteralList> lists = new Stack<>();
    private Literal literal;

    public LuminaryListenerImplementation(CommonTokenStream tokens) {
        this.tokens = tokens;
    }

    public int getNumberOfErrors() {
        return numberOfErrors;
    }

    public FileNode getFile() {
        return fileNode;
    }

    @Override
    public void enterProtocol(LuminaryParser.ProtocolContext ctx) {
        fileNode = new FileNode();
        types.push(fileNode);
    }

    @Override
    public void exitProtocol(LuminaryParser.ProtocolContext ctx) {
        if (!(types.peek() instanceof FileNode) || fileNode != types.pop() || types.size() != 0)
            throw new IllegalStateException("Something went completely wrong.");
    }

    @Override
    public void exitNamespace(LuminaryParser.NamespaceContext ctx) {
        fileNode.setNamespace(ctx.qualifiedName().getText());
    }

    @Override
    public void enterImportType(LuminaryParser.ImportTypeContext ctx) {
        fileNode.getImports().add(ImportNode.type(ctx.qualifiedName().getText(), ctx.IDENTIFIER().getText()));
    }

    @Override
    public void enterImportTypeWithAlias(LuminaryParser.ImportTypeWithAliasContext ctx) {
        fileNode.getImports().add(ImportNode.typeWithAlias(ctx.qualifiedName().getText(), ctx.IDENTIFIER(0).getText(), ctx.IDENTIFIER(1).getText()));
    }

    @Override
    public void enterImportEverything(LuminaryParser.ImportEverythingContext ctx) {
        fileNode.getImports().add(ImportNode.everything(ctx.qualifiedName().getText()));
    }

    @Override
    public void enterDecorators(LuminaryParser.DecoratorsContext ctx) {
        decorators = new ArrayList<>();
    }

    @Override
    public void enterDecorator(LuminaryParser.DecoratorContext ctx) {
        decoratorArguments = new ArrayList<>();
    }

    @Override
    public void exitKeyValuePair(LuminaryParser.KeyValuePairContext ctx) {
        decoratorArguments.add(new NameValuePair(ctx.IDENTIFIER().getText(), literal));
        literal = null;
    }

    @Override
    public void exitNormalDecorator(LuminaryParser.NormalDecoratorContext ctx) {
        decorators.add(new DecoratorValueNode(ctx.qualifiedName().getText(), decoratorArguments));
        decoratorArguments = null;
    }

    @Override
    public void exitMarkerDecorator(LuminaryParser.MarkerDecoratorContext ctx) {
        decorators.add(new DecoratorValueNode(ctx.qualifiedName().getText(), null));
    }

    @Override
    public void exitSingleElementDecorator(LuminaryParser.SingleElementDecoratorContext ctx) {
        decoratorArguments = new ArrayList<>();
        decoratorArguments.add(new NameValuePair("Value", literal));
        literal = null;
        decorators.add(new DecoratorValueNode(ctx.qualifiedName().getText(), decoratorArguments));
        decoratorArguments = null;
    }

    @Override
    public void exitDecorators(LuminaryParser.DecoratorsContext ctx) {
        if (ctx.getParent() instanceof LuminaryParser.ClassPropertyContext)
            classPropertyNode.getDecorators().addAll(decorators);
        else if (ctx.getParent() instanceof LuminaryParser.ClassDefinitionContext)
            classNode.getDecorators().addAll(decorators);
        else if (ctx.getParent() instanceof LuminaryParser.InterfacePropertyContext)
            interfacePropertyNode.getDecorators().addAll(decorators);
        else if (ctx.getParent() instanceof LuminaryParser.InterfaceDefinitionContext)
            interfaceNode.getDecorators().addAll(decorators);
        else if (ctx.getParent() instanceof LuminaryParser.EnumerationMemberContext)
            enumerationMemberNode.getDecorators().addAll(decorators);
        else if (ctx.getParent() instanceof LuminaryParser.EnumerationDefinitionContext)
            enumerationNode.getDecorators().addAll(decorators);
        else if (ctx.getParent() instanceof LuminaryParser.DecoratorPropertyContext)
            decoratorPropertyNode.getDecorators().addAll(decorators);
        else if (ctx.getParent() instanceof LuminaryParser.DecoratorDefinitionContext)
            decoratorNode.getDecorators().addAll(decorators);
        else if (ctx.getParent() instanceof LuminaryParser.FormalFixedParameterContext)
            formalParameterNode.getDecorators().addAll(decorators);
        else if (ctx.getParent() instanceof LuminaryParser.FormalArrayParameterContext)
            formalParameterNode.getDecorators().addAll(decorators);
        else if (ctx.getParent() instanceof LuminaryParser.InterfaceMethodContext)
            interfaceMethodNode.getDecorators().addAll(decorators);
        decorators = null;
    }

    @Override
    public void exitOption(LuminaryParser.OptionContext ctx) {
        final TypeContainerNode top = types.peek();
        if (!(top instanceof FileNode))
            throw new IllegalArgumentException("Options must be specified at the top level.");
        ((FileNode) top).getOptions().add(new NameValuePair(ctx.IDENTIFIER().getText(), literal));
        literal = null;
    }

    @Override
    public void enterDecoratorDefinition(LuminaryParser.DecoratorDefinitionContext ctx) {
        decoratorNode = new DecoratorNode(ctx.IDENTIFIER().getText(), extractDocumentation(ctx));
    }

    @Override
    public void exitDecoratorDefinition(LuminaryParser.DecoratorDefinitionContext ctx) {
        fileNode.getDefinedDecorators().add(decoratorNode);
        decoratorNode = null;
    }

    @Override
    public void enterDecoratorProperty(LuminaryParser.DecoratorPropertyContext ctx) {
        decoratorPropertyNode = new DecoratorPropertyNode(ctx.IDENTIFIER().getText(), ctx.type().getText(), extractDocumentation(ctx));
    }

    @Override
    public void exitDecoratorProperty(LuminaryParser.DecoratorPropertyContext ctx) {
        decoratorNode.getProperties().add(decoratorPropertyNode);
        decoratorPropertyNode = null;
    }

    @Override
    public void enterClassDefinition(LuminaryParser.ClassDefinitionContext ctx) {
        classNode = new ClassNode(ctx.IDENTIFIER().getText(), ctx.FINAL() != null, extractSupertypes(ctx.supertypeList()),
            extractDocumentation(ctx));
        types.push(classNode);
    }

    @Override
    public void enterClassProperty(LuminaryParser.ClassPropertyContext ctx) {
        classPropertyNode = new ClassPropertyNode(ctx.IDENTIFIER().getText(), ctx.type().getText(),
            ctx.OVERRIDE() != null, ctx.FINAL() != null, extractDocumentation(ctx));
        classNode.getProperties().add(classPropertyNode);
    }

    @Override
    public void exitClassDefinition(LuminaryParser.ClassDefinitionContext ctx) {
        types.pop();
        types.peek().getDefinedClasses().add(classNode);
        classNode = null;

        if (types.peek() instanceof ClassNode)
            classNode = (ClassNode) types.peek();
        if (types.peek() instanceof InterfaceNode)
            interfaceNode = (InterfaceNode) types.peek();
    }

    @Override
    public void enterInterfaceDefinition(LuminaryParser.InterfaceDefinitionContext ctx) {
        interfaceNode = new InterfaceNode(ctx.IDENTIFIER().getText(), extractSupertypes(ctx.supertypeList()),
            extractDocumentation(ctx));
        types.push(interfaceNode);
    }

    @Override
    public void enterInterfaceProperty(LuminaryParser.InterfacePropertyContext ctx) {
        interfacePropertyNode = new InterfacePropertyNode(ctx.IDENTIFIER().getText(), ctx.type().getText(),
            ctx.OVERRIDE() != null, ctx.GET() != null, ctx.SET() != null, extractDocumentation(ctx));
        interfaceNode.getProperties().add(interfacePropertyNode);
    }

    @Override
    public void exitInterfaceDefinition(LuminaryParser.InterfaceDefinitionContext ctx) {
        types.pop();
        types.peek().getDefinedInterfaces().add(interfaceNode);
        interfaceNode = null;

        if (types.peek() instanceof ClassNode)
            classNode = (ClassNode) types.peek();
        if (types.peek() instanceof InterfaceNode)
            interfaceNode = (InterfaceNode) types.peek();
    }

    @Override
    public void enterEnumerationDefinition(LuminaryParser.EnumerationDefinitionContext ctx) {
        final String underlyingType = ctx.enumerationUnderlyingType() != null ?
            ctx.enumerationUnderlyingType().getText() : "Int32";
        enumerationNode = new EnumerationNode(ctx.IDENTIFIER().getText(), underlyingType, extractDocumentation(ctx));
    }

    @Override
    public void enterEnumerationMember(LuminaryParser.EnumerationMemberContext ctx) {
        enumerationMemberNode = new EnumerationMemberNode(ctx.IDENTIFIER().getText(), extractDocumentation(ctx));
    }

    @Override
    public void exitEnumerationMember(LuminaryParser.EnumerationMemberContext ctx) {
        enumerationMemberNode.setValue(literal);
        literal = null;
        enumerationNode.getMembers().add(enumerationMemberNode);
        enumerationMemberNode = null;
    }

    @Override
    public void exitEnumerationDefinition(LuminaryParser.EnumerationDefinitionContext ctx) {
        types.peek().getDefinedEnumerations().add(enumerationNode);
        enumerationNode = null;
    }

    @Override
    public void exitConstantDefinition(LuminaryParser.ConstantDefinitionContext ctx) {
        final ConstantNode constantNode = new ConstantNode(ctx.IDENTIFIER().getText(), ctx.type().getText(), literal, extractDocumentation(ctx));
        if (ctx.getParent() instanceof LuminaryParser.ClassEntryContext)
            ((ClassNode) types.peek()).getDefinedConstants().add(constantNode);
        else if (ctx.getParent() instanceof LuminaryParser.DecoratorEntryContext)
            ((DecoratorNode) types.peek()).getDefinedConstants().add(constantNode);
    }

    @Override
    public void enterArray(LuminaryParser.ArrayContext ctx) {
        lists.push(new LiteralList());
    }

    @Override
    public void exitArray(LuminaryParser.ArrayContext ctx) {
        final LiteralList list = lists.pop();
        if (lists.size() > 0)
            lists.peek().getValue().add(list);
        else
            literal = list;
        tryConsumeLiteral(ctx.parent);
    }

    @Override
    public void enterFormalFixedParameter(LuminaryParser.FormalFixedParameterContext ctx) {
        formalParameterNode = new FormalParameterNode(ctx.IDENTIFIER().getText(), ctx.type().getText(), false,
            extractDocumentation(ctx));
    }

    @Override
    public void exitFormalFixedParameter(LuminaryParser.FormalFixedParameterContext ctx) {
        interfaceMethodNode.getFormalParameters().add(formalParameterNode);
        formalParameterNode = null;
    }

    @Override
    public void enterFormalArrayParameter(LuminaryParser.FormalArrayParameterContext ctx) {
        formalParameterNode = new FormalParameterNode(ctx.IDENTIFIER().getText(), ctx.type().getText(), true,
            extractDocumentation(ctx));
    }

    @Override
    public void exitFormalArrayParameter(LuminaryParser.FormalArrayParameterContext ctx) {
        interfaceMethodNode.getFormalParameters().add(formalParameterNode);
        formalParameterNode = null;
    }

    @Override
    public void enterInterfaceMethod(LuminaryParser.InterfaceMethodContext ctx) {
        interfaceMethodNode = new InterfaceMethodNode(ctx.IDENTIFIER().getText(),
            ctx.returnType().VOID() != null ? null : ctx.returnType().getText(), extractDocumentation(ctx));
    }

    @Override
    public void exitInterfaceMethod(LuminaryParser.InterfaceMethodContext ctx) {
        interfaceNode.getMethods().add(interfaceMethodNode);
        interfaceMethodNode = null;
    }

    private void tryConsumeLiteral(RuleContext parent) {
        if (lists.size() > 0) {
            lists.peek().getValue().add(literal);
            literal = null;
        } else {
            if (parent instanceof LuminaryParser.DecoratorPropertyContext) {
                decoratorPropertyNode.setDefault(literal);
                literal = null;
            } else if (parent instanceof LuminaryParser.ClassPropertyContext) {
                classPropertyNode.setDefault(literal);
                literal = null;
            }
        }
    }

    @Override
    public void exitInteger(LuminaryParser.IntegerContext ctx) {
        literal = extractIntegralLiteral(ctx.getText());
        tryConsumeLiteral(ctx.parent);
    }

    @Override
    public void exitFloat(LuminaryParser.FloatContext ctx) {
        literal = extractBinaryFloatingPointLiteral(ctx.getText());
        tryConsumeLiteral(ctx.parent);
    }

    @Override
    public void exitDecimal(LuminaryParser.DecimalContext ctx) {
        literal = extractDecimalFloatingPointLiteral(ctx.getText());
        tryConsumeLiteral(ctx.parent);
    }

    @Override
    public void exitString(LuminaryParser.StringContext ctx) {
        final String text = ctx.STRING_LITERAL().getText();
        literal = new LiteralText(text.substring(1, text.length() - 1));
        tryConsumeLiteral(ctx.parent);
    }

    @Override
    public void exitTrue(LuminaryParser.TrueContext ctx) {
        literal = LiteralBoolean.TRUE;
        tryConsumeLiteral(ctx.parent);
    }

    @Override
    public void exitFalse(LuminaryParser.FalseContext ctx) {
        literal = LiteralBoolean.FALSE;
        tryConsumeLiteral(ctx.parent);
    }

    @Override
    public void exitNull(LuminaryParser.NullContext ctx) {
        literal = LiteralNull.INSTANCE;
        tryConsumeLiteral(ctx.parent);
    }

    @Override
    public void exitMemberReference(LuminaryParser.MemberReferenceContext ctx) {
        literal = new LiteralUnresolved(LiteralKind.CONSTANT, ctx.getText());
        tryConsumeLiteral(ctx.parent);
    }

    @Override
    public void exitTypeReference(LuminaryParser.TypeReferenceContext ctx) {
        literal = new LiteralUnresolved(LiteralKind.TYPE, ctx.type().getText());
        tryConsumeLiteral(ctx.parent);
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        numberOfErrors += 1;
    }

    private BigInteger parse(String text) {
        if (text.contains("0x"))
            return new BigInteger(text.replace("0x", ""), 16);
        if (text.contains("0o"))
            return new BigInteger(text.replace("0o", ""), 8);
        if (text.contains("0b"))
            return new BigInteger(text.replace("0b", ""), 2);
        return new BigInteger(text, 10);
    }

    private long parseUInt64(String text) {
        final BigInteger value = parse(text);
        // TODO: Check bounds.
        return value.longValue();
    }

    private long parseSInt64(String text) {
        final BigInteger value = parse(text);
        // TODO: Check bounds.
        return value.longValue();
    }

    private int parseUInt32(String text) {
        final BigInteger value = parse(text);
        // TODO: Check bounds.
        return value.intValue();
    }

    private int parseSInt32(String text) {
        final BigInteger value = parse(text);
        // TODO: Check bounds.
        return value.intValue();
    }

    private short parseUInt16(String text) {
        final BigInteger value = parse(text);
        // TODO: Check bounds.
        return value.shortValue();
    }

    private short parseSInt16(String text) {
        final BigInteger value = parse(text);
        // TODO: Check bounds.
        return value.shortValue();
    }

    private byte parseUInt08(String text) {
        final BigInteger value = parse(text);
        // TODO: Check bounds.
        return value.byteValue();
    }

    private byte parseSInt08(String text) {
        final BigInteger value = parse(text);
        // TODO: Check bounds.
        return value.byteValue();
    }

    private LiteralInteger extractIntegralLiteral(String text) {
        text = text.replace("_", "");

        if (text.endsWith("u64"))
            return new LiteralUInt64(parseUInt64(text.substring(0, text.length() - 3)));
        if (text.endsWith("u32"))
            return new LiteralUInt32(parseUInt32(text.substring(0, text.length() - 3)));
        if (text.endsWith("u16"))
            return new LiteralUInt16(parseUInt16(text.substring(0, text.length() - 3)));
        if (text.endsWith("u8"))
            return new LiteralUInt8(parseUInt08(text.substring(0, text.length() - 2)));
        if (text.endsWith("i64"))
            return new LiteralInt64(parseSInt64(text.substring(0, text.length() - 3)));
        if (text.endsWith("i16"))
            return new LiteralInt16(parseSInt16(text.substring(0, text.length() - 3)));
        if (text.endsWith("i8"))
            return new LiteralInt8(parseSInt08(text.substring(0, text.length() - 2)));
        return new LiteralInt32(parseSInt32(text.endsWith("i32") ? text.substring(0, text.length() - 3) : text));
    }

    private Literal extractBinaryFloatingPointLiteral(String text) {
        text = text.replace("_", "");
        if (text.endsWith("f32")) {
            text = text.substring(0, text.length() - 3);
            return new LiteralFloat32(Float.parseFloat(text));
        }

        text = text.endsWith("f64") ? text.substring(0, text.length() - 3) : text;
        return new LiteralFloat64(Double.parseDouble(text));
    }

    private Literal extractDecimalFloatingPointLiteral(String text) {
        text = text.replace("_", "");
        text = text.endsWith("d64") ? text.substring(0, text.length() - 3) : text;
        return new LiteralDecimal(Decimal64Utils.parse(text));
    }

    private List<String> extractSupertypes(LuminaryParser.SupertypeListContext ctx) {
        if (ctx == null)
            return null;

        List<String> supertypes = new ArrayList<>();
        while (ctx != null) {
            supertypes.add(ctx.type().getText());
            ctx = ctx.supertypeList();
        }

        return supertypes;
    }

    private List<String> extractDocumentation(ParserRuleContext ctx) {
        List<Token> hiddenTokens = tokens.getHiddenTokensToLeft(ctx.start.getTokenIndex());
        if (hiddenTokens == null)
            return null;

        List<String> comments = null;
        for (Token token : hiddenTokens)
            if (token.getText().startsWith("///")) {
                if (comments == null)
                    comments = new ArrayList<>();
                comments.add(token.getText().substring(3).trim());
            }
        return comments;
    }
}
