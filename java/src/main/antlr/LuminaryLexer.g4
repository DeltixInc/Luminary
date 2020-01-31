lexer grammar LuminaryLexer;

// Special Symbols

ASTERISK:                               '*';
CLOSING_CURLY_BRACKET:                  '}';
CLOSING_PARENTHESIS:                    ')';
CLOSING_SQUARE_BRACKET:                 ']';
COLON:                                  ':';
COMMA:                                  ',';
EQUALS_SIGN:                            '=';
GREATER_THAN_SIGN:                      '>';
LESS_THAN_SIGN:                         '<';
OPENING_CURLY_BRACKET:                  '{';
OPENING_PARENTHESIS:                    '(';
OPENING_SQUARE_BRACKET:                 '[';
ELLIPSIS:                               '...';
PERIOD:                                 '.';
QUESTION_MARK:                          '?';
SEMICOLON:                              ';';

// Keywords

AS:                                     'as';
CLASS:                                  'class';
CONST:                                  'const';
DECORATOR:                              'decorator';
ENUM:                                   'enum';
FALSE:                                  'false';
GET:                                    'get';
IMPORT:                                 'import';
INTERFACE:                              'interface';
NAMESPACE:                              'namespace';
NULL:                                   'null';
OPTION:                                 'option';
OVERRIDE:                               'override';
SET:                                    'set';
TRUE:                                   'true';
TYPEOF:                                 'typeof';
VOID:                                   'void';
FINAL:                                  'final';

// Literals

STRING_LITERAL:                         '"' CHARACTERS? '"';

CHAR_LITERAL:                           '\'' CHARACTER '\'';



INTEGRAL_LITERAL:                       '-'? INTEGRAL_LITERAL_DIGITS ( 'i64' | 'i32'? | 'i16' | 'i8' ) |
                                        INTEGRAL_LITERAL_DIGITS ( 'u64' | 'u32' | 'u16' | 'u8' );

BINARY_FLOATING_POINT_LITERAL:          '-'? DEC_DIGITS PERIOD DEC_DIGITS? EXPONENT_PART? '-'* FLOATING_TYPE_SUFFIX? |
                                        '-'? PERIOD DEC_DIGITS EXPONENT_PART? '-'* FLOATING_TYPE_SUFFIX? |
                                        '-'? DEC_DIGITS EXPONENT_PART '-'* FLOATING_TYPE_SUFFIX? |
                                        '-'? DEC_DIGITS '-'* FLOATING_TYPE_SUFFIX;

DECIMAL_FLOATING_POINT_LITERAL:         '-'? DEC_DIGITS PERIOD DEC_DIGITS? EXPONENT_PART? '-'* DECIMAL_TYPE_SUFFIX |
                                        '-'? PERIOD DEC_DIGITS EXPONENT_PART? '-'* DECIMAL_TYPE_SUFFIX |
                                        '-'? DEC_DIGITS EXPONENT_PART '-'* DECIMAL_TYPE_SUFFIX |
                                        '-'? DEC_DIGITS '-'* DECIMAL_TYPE_SUFFIX;

// Identifier

IDENTIFIER:                             [a-zA-Z] [_a-zA-Z0-9]*;

// Whitespaces and Comments

WHITESPACE:                             [ \t]+ -> skip;
NEW_LINE:                               ('\r' '\n' | '\n') -> skip;
DOCUMENTATION_COMMENTS:                 '///' ~[\r\n]* -> channel(HIDDEN);
SINGLE_LINE_COMMENT:                    '//' ~[\r\n]* -> skip;
MULTI_LINE_COMMENT:                     '/*' .*? '*/' -> skip;

// Fragments

fragment EXPONENT_PART:                 ('e'|'E')? DEC_INTEGER;
fragment FLOATING_TYPE_SUFFIX:          ('f64' | 'f32');
fragment DECIMAL_TYPE_SUFFIX:           ('d64');

fragment INTEGRAL_LITERAL_DIGITS:       HEX_INTEGER | DEC_INTEGER | OCT_INTEGER | BIN_INTEGER;
fragment DEC_DIGITS:                    [0-9][0-9_]*;
fragment HEX_INTEGER:                   '0x' [0-9a-fA-F_]+;
fragment DEC_INTEGER:                   ('0' | [1-9][0-9_]*);
fragment OCT_INTEGER:                   '0o' [0-7_]+;
fragment BIN_INTEGER:                   '0b' [0-1_]+;
fragment CHARACTERS:                    (~[\\\r\n"] | '\\' ['"?abfnrtv\\])*;
fragment CHARACTER:                     (~[\\\r\n\'] | '\\' ['"?abfnrtv\\]);
