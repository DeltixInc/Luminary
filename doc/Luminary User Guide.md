# Rationale

While developing various **QuantOffice**/**QuantServer** as well as other projects like **MaxiMarkets TradingServer**
and related products one often has to deal with essentially the same data structures in different programming languages
(e.g. C# and Java). Besides, there are various representations of these data structures -- there is a need to store them
in SQL databases, time-series databases like **TimeBase**, transfer data between machines/processes/threads using some
kind of serialization format, print them in text format for the purposes of logging etc. Most of these data structures
and the operations on them can be effectively generated thus reducing a probability of human error which are rather
frequent especially when dealing with cumbersome writing of similar code over and over again. Thus, having some kind of
intermediate language capable of describing such data structures as well as a framework that allows to automatically
generate routine code is greatly beneficial.

# Use Cases

At the moment there are several use cases when such language and code generation framework can be used:

  * generation of TimeBase schema;
  * generation of Java TimeBase messages;
  * generation of C# TimeBase messages;
  * generation of Java TimeBase codecs;
  * generation of C# TimeBase codecs;
  * generation of Java business objects for **QuantServer**;
  * generation of C# business objects for **QuantOffice**;
  * generation of SQL database schemas;
  * generation of Java protocol messages for **TradingServer**;
  * generation of C# protocol messages for **TradingServer**;
  * generation of Java business objects for **TradingServer**;
  * generation of C# business objects for **TradingServer**;
  * generation of codecs for our FIX engine;
  * generation of RPC for various products;
  * generation of public API with consistent documentation across various languages;
  * generation of ProtocolBuffers definitions;
  * generation of codecs that transform business objects into ProtocolBuffers messages and vice versa.

# Language Overview

## Constant Values

Constant values are used as arguments to the decorators, default values for properties of decorators and classes.
Following categories of constant values are recognized:

  * `null` denotes the absence of any value.
  * Boolean literals are used to denote values of type `Boolean` and consist of keywords `true` and `false`.
  * Integer literals are used to denote integral values and have several forms:

    - decimal literals such as `1`, `1_000`, `1234`, `-1_234_567`,
    - hexadecimal literals such as `0x0`, `-0x1234A`, `0xCA_FE_BA_BE`,
    - octal literals such as `0o0`, `-0o123`, `0o123_456`,
    - binary literals such as `0b0`, `-0b0101`, `0b010_001_000`.

  By default all integral literals have type `Int32`, but the correct type can be specified using suffixes `i8`, `i16`,
  `i32`, `i64`, `u8`, `u16`, `u32` and `u64`.
  * Floating-point literals such as `1.0E-12`, `1_000_000.123_456E+001`. Default type of such literals is `Float64` but
  it can be explicitly specified using suffixes `f64` and `f32`.
  * String literals represented by sequence of characters enclosed with two double-quote characters:
  `"An example of string."`.
  * Enumeration members, e.g. `SomeEnumeration.VALUE1`.
  * Types specified using the `typeof` operator: `typeof(Int32)`, `typeof(List<SomeClass?>)`.

There are also several built-in constants for primitive types:

  * `MIN_VALUE` and `MAX_VALUE` for `Int8`, `Int16`, `Int32`, `Int64`, `UInt8`, `UInt16`, `UInt32`, `UInt64`,
  `Timestamp`, `Duration`, `Time`, `Date`, `UUID`
  * `MIN_VALUE`, `MAX_VALUE`, `NaN`, `NEGATIVE_INFINITY` and `POSITIVE_INFINITY` for `Float32` and `Float64`.

These constants can referenced by joining the name of the type and the name of the constant via a period:
`Int64.MAX_VALUE`.

## Type System

**Luminary**'s type system is made of built-in types as well as several syntax constructions to produce custom types
such as enumerations, interfaces, classes and decorators.

### Builtin Types

At the core of the **Luminary** there is a number of built-in types that can be used to define composite types (such as
    decorators, interfaces and classes). Builtin types include:

  * integral types: `Int8`, `Int16`, `Int32`, `Int64`, `UInt8`, `UInt16`, `UInt32` and `UInt64`;
  * boolean type: `Boolean`;
  * floating-point types: `Float32` and `Float64`;
  * textual type: `Text`;
  * raw binary type: `Data`;
  * time-related types: `Timestamp`, `Duration`, `Date`, `Time` (time of day);
  * decimal type: `Decimal`;
  * special type used to represent other type declarations: `Type`;
  * `UUID`.

In addition one can use `T?` represent nullable counterpart of type `T` and `List<T>` to represent array of elements of
type `T` thus creating complex types such as: `List<List<Int32?>>`.

There are also some number of predefined generic types besides the `List<T>`: `Function` and `Action`. Both of these
generic types have variable number of generic arguments. `Function` defines a type of methods that return values,
`Action` - type of methods that do not return anything. This types are very similar to C#'s `Func` and `Action`.

### Enumerations

An enumeration is a type that supplies alternate names for the values of an underlying primitive type. An enumeration
type has a name, an underlying type that must be one of the built-in signed or unsigned integer types (such as `Int8`,
    `Int32`, `UInt64` etc.), and a set of members separated by semicolon. Member definition consists of member name
    followed by equals sign and the value (of the underlying type):

```
enum SimpleEnum : Int64 {
    A = 1;
    B = 2;
}
```

If no underlying type is specified, the enumeration will have the underlying type of `Int32`.

### Interfaces

Interfaces can contain properties with specification of whether they are gettable and/or settable. Also, interface can
have any number of super-interfaces.

```
interface SimpleInterface : SuperInterface1, SuperInterface2 {
    Text Name get set;
    List<Int64> Values get;
}
```

Also, interfaces can contain declarations of methods. Interface methods are declared the same way as in languages C# and Java:

```
interface SimpleAPI {
    void LogIn(Text username, Text password);
    Int64 GetId();
    Text Format(Text format, Int64 ... args);
}
```

At the moment Luminary does not support documentation for method parameters as in C# and Java, but the same result
can be achieved:

```
interface Test {
    /// Do some useful stuff.
    Int64 DoUsefullStuff(
        /// Name of the person.
        Text name,
        /// ID of the request.
        Int64 requestId);
}
```

### Classes

Classes composed of typed properties. Each property of the class can have a default value. Interface can have any
number of super-interfaces and no more than one superclass.

```
class SimpleClass : SuperClass, SuperInterface1, SuperInterface2 {
    // Here we specify the default value for the property.
    Text Name = "John';
    Int64 Value;
}
```

Class can be marked as `final` prohibiting from inheriting it in the future.

```
/// This class cannot be inherited from.
final class A {
}
```

For the purpose of changing decorators (which may affect generated code) and comments class properties can be overriden by adding `override` specifier.

```
class A {
    Int32 Value;
}

class B : A {
    /// Alternative documentation comment.
    [SomeNewDecorator]
    override Int32 Value;
}
```

To prevent class property being overriden in such matter, keyword `final` can also be used on class properties:

```
class A {
    /// This property cannot be overriden.
    final Int32 Value;
    /// But this - can be.
    Text Name;
}
```

Note, in cases you want to specify both `final` and `override` keywords on class property the order of these keywords matter: `final` must precede the `override`.

### Decorator Types

Decorator types are similar to the classes with the difference that the types of decorator properties are restricted to
ones that can have constant values: `Boolean`, integers, floating-point types, strings, types, and single-dimension
lists of the types mentioned above.

```
decorator SimpleDecorator {
    // Here we specify the default value for the property.
    Text Name = "Jane";
    Text Value;
}
```

## Decorators

**Luminary**'s decorators are similar to C#'s attributes and Java's annotations. There are three kind of decorators.
The first kind is the most general, while the other kinds are merely shorthands (or syntactic sugar) for the first kind.
 When working with **Luminary** API all these types of decorators are indistinguishable.

Decorators precede the item they are targeted to and must be enclosed in square brackets. More than one decorator can
be specified within the square brackets using comma as a separator.

### Normal Decorators

A normal decorator specifies the name of the decorator type and optionally a list of comma-separated name-value pairs.
Each pair defines a value that is associated with a property of the decorator type. A normal decorator must contain an
name-value pair for every property of the corresponding decorator type, except for those properties with default values.
For example, given the decorator type

```
decorator SimpleDecorator {
    Text Name = "Jane";
    Int32 Value;
}
```

here are some examples of the decorators of that type:

```
[SimpleDecorator(Name = "John", Value = 1)]
class SimpleClass {
    [SimpleDecorator(Value = 2, Name = "Test")]
    Data Buffer;

    [SimpleDecorator(Value = 3)]
    Timestamp Time;
}
```

### Marker Decorator

A marker decorator is a shorthand designed for use in situations when the list of name-value pairs of the decorator is
empty. In such cases parentheses can be omitted:

```
decorator Test {
}

[Test]
enum SimpleEnumeration {
    A = 1;
    B = 2;
}
```

### Single-Element Decorators

A single-element decorator is a shorthand designed for use with single-element decorator types. With single-element
decorators the name of the property can be omitted and is presumed to have name `Value`. It is legal to use
single-element decorators for decorator types with multiple properties, so long as on element is named `Value` and all
other elements have default values.

```
decorator DecoratorOne {
    Text Value;
}

decorator DecoratorTwo {
    Int32 Value;
    Text Name = "Jane";
}

[DecoratorOne("Hello"), DecoratorTwo(42)]
class TestClass {
}
```

### Built-in Decorators

Besides the ability to define custom decorators **Luminary** defines several built-in decorators. Their definitions are
equivalent to these:

```
enum DecoratorTarget {
    ENUMERATION = 1;
    ENUMERATION_MEMBER = 2;
    INTERFACE = 3;
    INTERFACE_PROPERTY = 4;
    CLASS = 5;
    CLASS_PROPERTY = 6;
    DECORATOR = 7;
    DECORATOR_PROPERTY = 8;
}

decorator DecoratorUsage {
    List<DecoratorTarget> ValidOn = { DecoratorTarget.ENUMERATION,
                                      DecoratorTarget.ENUMERATION_MEMBER,
                                      DecoratorTarget.INTERFACE,
                                      DecoratorTarget.INTERFACE_PROPERTY,
                                      DecoratorTarget.CLASS,
                                      DecoratorTarget.CLASS_PROPERTY,
                                      DecoratorTarget.DECORATOR,
                                      DecoratorTarget.DECORATOR_PROPERTY }
    Boolean Repeatable = false;
}
```

Decorator `DecoratorUsage` can be applied only to decorator definitions and controls where the decorator being defined
can be used and how.

## Constants

Decorator and class definitions can contain definitions of constants. They are declared using keyword `const` followed
by the type of the constant, its name and value:

```
class TestClass {
    const Int32 ZERO = 0i32;
    const Text HELLO = "Hello";
    const Int32 NULL = ZERO;
}

class TestDecorator {
    const Int32 ZERO = TestClass.ZERO;
}
```

Constant can be used wherever constant values are expected: as arguments for the decorators or the default values of
properties.

## Comments

**Luminary** supports single-line and multiline comments familiar to anyone working with any C-style languages such as
C++, C#, Java etc.

```
// TODO: Come up with more descriptive name.
class SimpleClass {
    /*
    Int32 Id;
    Text Name;
    */
    Data Value;
}
```

Single-line comments starting with `///` denote documentation comments. Unlike regular comments they will be preserved
in the abstract syntax tree and will be made available via API.

## Imports

Luminary supports flexible import system that takes best of the worlds of Java and C#. Essentially, there are three
types of import statements:

  * import of the given type: `import SomeNamespace.SomeInnerNamespace.SomeType;`
  * import of all types within the given namespace: `import SomeNamespace.SomeInnerNamespace.*;`
  * import of the given type with an alias: `import SomeNamespace.SomeInnerNamespace.SomeType as SomeOtherType;`. Here,
  type name `SomeOtherType` will be an alias to `SomeNamespace.SomeInnerNamespace.SomeType` while `SomeType` is
  considered unused and thus can taken by other import statement.

Also, similar to Java and C# one does not have to import types from the namespace we are currently in.

# Projects

In order to provide the ability to reuse and structure Luminary definitions the support of projects have been added.
Luminary project is a `*.json` file that have two top level properties: `Sources` and `References`.

`Sources` property is an array of objects each on which can itself have 3 properties:
  * `Directory` - a required root directory for the Luminary sources.
  * `Include` - an optional array of include patterns.
  * `Exclude` - an optional array of exclude patterns.

Luminary source code files are required to be placed accordingly to their namespace (similar to Java) but one can have
several (non necessarily nested) types defined within a one `*.lux` file. `Directory` defines the root directory for
Luminary sources while `Include` and `Exclude` defines a collections of rules to use when determining whether Luminary
file (having the `.lux` extension) should be considered part of the project or not. `Include` and `Exclude` are
optional, and their default values are `[ "**/*.lux" ]` and `[]` respectively.

`References` property defines an array of strings - relative or absolute paths to the other Luminary project files to
use while resolving import statements.

Here is the example of Luminary project:

````
{
    "Sources": [
        {
            "Directory" : "."
        }
    ],
    "References": [
        "ProtocolGenerators.json",
        "../Enums/Enums.json",
        "../Common/Common.json",
        "../External/External.json"
    ]
}
````

When referencing other Luminary projects it is often unknown the exact or even relative location of the said projects.
The exact path can be different depending on operating system, version of the package and other various reasons. Thus
in order to support such complex scenarios Luminary (its Java and C# implementation libraries to be exact) support
specification of **search path** via environment variable `LUMINARY_SEARCH_PATH` (or system property
`luminary.search.path` in Java). This environment variable (or Java's system property) specifies a list of candidate
directories that can contain other Luminary projects. These search directories are concatenated in a way similar to
`PATH` environment variable: via `;` on Windows or ':' on Linux.

This works like follows. Upon encountering a reference to another projects Luminary's project loader checks whether
the path is absolute or relative. If the path is absolute project loader attempts to load the project using the
specified path. Otherwise, first current working directory is considered and then all entries of `LUMINARY_SEARCH_PATH`
environment variable (or `luminary.search.path` Java's system property). The first entry that contains the specified
relative path is considered a match and the referenced project is attempted to load.

In Java packages are often distributed as ZIP archives (e.g. by Maven), thus Luminary's project loader understands
ZIP archives in search path and works with them as if they are simple directories.
