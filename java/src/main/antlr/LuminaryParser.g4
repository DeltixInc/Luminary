parser grammar LuminaryParser;

options {
	tokenVocab=LuminaryLexer;
}

protocol
    :   namespace (importStatement | option)* typeDefinition* EOF
    ;

namespace
    :   NAMESPACE  qualifiedName SEMICOLON
    ;

importStatement
    :   IMPORT qualifiedName PERIOD IDENTIFIER SEMICOLON                # ImportType
    |   IMPORT qualifiedName PERIOD ASTERISK SEMICOLON                  # ImportEverything
    |   IMPORT qualifiedName PERIOD IDENTIFIER AS IDENTIFIER SEMICOLON  # ImportTypeWithAlias
    ;

option
    :   OPTION IDENTIFIER EQUALS_SIGN constantValue SEMICOLON
    ;

qualifiedName
    :   IDENTIFIER ( PERIOD IDENTIFIER )*
    ;

typeDefinition
    :   enumerationDefinition
    |   classDefinition
    |   interfaceDefinition
    |   decoratorDefinition
    ;

// Definition of Decorator

decoratorDefinition
    :   decorators? DECORATOR IDENTIFIER OPENING_CURLY_BRACKET decoratorEntry* CLOSING_CURLY_BRACKET
    ;

decoratorEntry
    :   decoratorProperty
    |   typeDefinition
    ;

decoratorProperty
    :   decorators? type IDENTIFIER ( EQUALS_SIGN constantValue )? SEMICOLON
    ;

// Definition of Interface

interfaceDefinition
    :   decorators? INTERFACE IDENTIFIER ( COLON supertypeList )? OPENING_CURLY_BRACKET interfaceEntry* CLOSING_CURLY_BRACKET
    ;

supertypeList
    :   type ( COMMA supertypeList )?
    ;

interfaceEntry
    :   interfaceProperty
    |   interfaceMethod
    |   typeDefinition
    ;

interfaceProperty
    :   decorators? OVERRIDE? type IDENTIFIER GET SET? SEMICOLON
    ;

interfaceMethod
    :   decorators? returnType IDENTIFIER OPENING_PARENTHESIS formalParameters? CLOSING_PARENTHESIS SEMICOLON
    ;

formalParameters
    :   formalFixedParameter ( COMMA formalFixedParameter)* ( COMMA formalArrayParameter )?
    |   formalArrayParameter
    ;

formalFixedParameter
    :   decorators? type IDENTIFIER
    ;

formalArrayParameter
    :   decorators? type ELLIPSIS IDENTIFIER
    ;

// Definition of Class

classDefinition
    :   decorators? FINAL? CLASS IDENTIFIER ( COLON supertypeList )? OPENING_CURLY_BRACKET classEntry* CLOSING_CURLY_BRACKET
    ;

classEntry
    :   classProperty
    |   typeDefinition
    |   constantDefinition
    ;

classProperty
    :   decorators? FINAL? OVERRIDE? type IDENTIFIER ( EQUALS_SIGN constantValue )? SEMICOLON
    ;

constantDefinition
    :   decorators? CONST type IDENTIFIER EQUALS_SIGN constantValue SEMICOLON
    ;

// Definition of Enumeration

enumerationDefinition
    :   decorators? ENUM IDENTIFIER ( COLON enumerationUnderlyingType )? OPENING_CURLY_BRACKET enumerationMember+ CLOSING_CURLY_BRACKET
    ;

enumerationUnderlyingType
    :   IDENTIFIER
    ;

enumerationMember
    :   decorators? IDENTIFIER EQUALS_SIGN constantValue SEMICOLON
    ;

// Type

returnType
    :   VOID
    |   type
    ;

type
    :   qualifiedName
    |   type QUESTION_MARK
    |   IDENTIFIER LESS_THAN_SIGN type ( COMMA type )* GREATER_THAN_SIGN
    ;

// Decorators

decorators
    :  ( OPENING_SQUARE_BRACKET decorator ( COMMA decorator )* CLOSING_SQUARE_BRACKET )+
    ;

decorator
    :   normalDecorator
    |   markerDecorator
    |   singleElementDecorator
    ;

normalDecorator
    :   qualifiedName OPENING_PARENTHESIS keyValuePair ( COMMA keyValuePair )* CLOSING_PARENTHESIS
    |   qualifiedName OPENING_PARENTHESIS CLOSING_PARENTHESIS
    ;

markerDecorator
    :   qualifiedName
    ;

singleElementDecorator
    :   qualifiedName OPENING_PARENTHESIS constantValue CLOSING_PARENTHESIS
    ;

keyValuePair
    :   IDENTIFIER EQUALS_SIGN constantValue
    ;

constantValue
    :   STRING_LITERAL                                                      # String
    |   CHAR_LITERAL                                                        # Char
    |   INTEGRAL_LITERAL                                                    # Integer
    |   BINARY_FLOATING_POINT_LITERAL                                       # Float
    |   DECIMAL_FLOATING_POINT_LITERAL                                      # Decimal
    |   qualifiedName                                                       # MemberReference
    |   TYPEOF OPENING_PARENTHESIS type CLOSING_PARENTHESIS                 # TypeReference
    |   TRUE                                                                # True
    |   FALSE                                                               # False
    |   NULL                                                                # Null
    |   OPENING_CURLY_BRACKET constantValueList? CLOSING_CURLY_BRACKET      # Array
    ;

constantValueList
    :   constantValue ( COMMA constantValue )*
    ;
