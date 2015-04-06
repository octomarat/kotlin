# Annotation Arguments

Goals:
* Sort out problems of positional parameters and varargs in annotations
* \[TBD later] Better syntax for array arguments to annotations

Related issues:
* [KT-6652 Prohibit using java annotations with positional arguments](https://youtrack.jetbrains.com/issue/KT-6652)
* [KT-6220 Annotations: handling of "value" members](https://youtrack.jetbrains.com/issue/KT-6220)
* [KT-6641 Annotations with multiple array values](https://youtrack.jetbrains.com/issue/KT-6641)
* [KT-2576 Shortcut notation for annotations with single-value array elements](https://youtrack.jetbrains.com/issue/KT-2576)

## Problem Statement

In Java annotation elements (this is the term java uses for "fields"/"attributes"/"properties" of an annotation) are defined as methods in the corresponding `@interface`, so there is no ordering rule that we can use when loading a fictitious primary constructor for a Java annotation.

Example:

Let's say there's a Java annotation with two elements:

``` java
@interface Ann {
    int foo();
    String bar();
}
```

When we use it in Kotlin, we can use positional arguments:

``` kotlin
[Ann(10, "asd")]
class Baz
```

Now, it's both source- and binary- compatible to reorder methods in a Java interface:

``` java
@interface Ann {
    String bar();
    int foo();
}
```

But the code above will break.

Also, we now load all array arguments as varargs, which may break for the same reason.

## Loading Java Annotations

Fictitious constructors for Java annotations sould be built as follows:
* if there is an element named `value`, it is put first on the parameter list
* if all other elements have default values, and `value` has an array type, it is marked `vararg` and has the type of the elements of the array
* parameters corresponding to all elements but `value` can not be used positionally, only named arguments are allowed for them (this requires adding a platform-specific check to `frontend.java`)
* note that elements with default values should be transformed to parameters with default values

There should be some notes about case when `value` parameter is marked `vararg` and no arguments are passed.
Behavior will depend on presence of parameter's default value:
* if it has no default value, then empty array is emitted in bytecode
* if it has default value, then no value is emitted in bytecode, so default will be used

**NOTE**: Behavior of the same code can change after adding default value to parameter and recompiling kotlin
sources

## \[TBD later] Array Syntax Examples

**NOTE**: Scala still uses `Array(...)` in annotations, no matter how ugly it is

Option 1: Use `[]` for array literal

``` kotlin
@User(
  firstName = "John",
  names = ["Marie", "Spencer"],
  lastName = "Doe"
)
class JohnDoe

@Values([FOO, BAR]) // ugly, but it's the same in Java: @Ann({FOO, BAR})
class WithValues
```

Option 2: Use `@(...)`

``` kotlin
@User(
  firstName = "John",
  names = @("Marie", "Spencer"),
  lastName = "Doe"
)
class JohnDoe

@Values(@(FOO, BAR)) // looks bad
class WithValues
```