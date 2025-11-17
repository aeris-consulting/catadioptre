# Catadioptre

## Easily work with private members when testing in Java and Kotlin

**Catadioptre** is a lightweight library to work with private and protected members in Java and Kotlin using reflection
and
generated "proxies" methods.

With **Catadioptre**, you can:

* read or write private and protected properties / fields,
* execute private and protected functions / methods,
* generate "proxy" methods at compilation-time to easily access to the private members in your tests.

**Catadioptre** supports variable, optional and named arguments in functions as well as `suspend` functions for Kotlin.

## Should I really test my private methods?

Yes and no. A private methods is always part of the implementation of a bigger unit. Best practices encourage to test
the feature, not the implementation. Therefore, conventions consist in testing the implementation of a private method
through the public ones.

But let's be honest. Often enough, this makes the testing of the feature more complex: you have to provide all the
inputs combinations
for all the use-cases to cover all the decision branches and use cases.

So, what are the options?

* Convert those private pieces into "features", by moving them to dedicated classes as public units.
* Verify that the convenient pieces of code are called in the execution workflow and that those pieces behave as
  expected for all the use-cases they support.

The first solution is a commonly used practice, but has a serious drawback: it exposes private pieces of code, (
potentially) strength the coupling and reduces the consistency of your code.
The second solution requires complex logic to access the private units. That's where **Catadioptre** helps you.

## Why using reflection and not byte-code manipulation?

To make private members accessible outside a class and generate "proxy" methods for it, we could have used code
generation and manipulation libraries like the excellent [ByteBuddy](https://bytebuddy.net/).

But this would have implied to:

* Make your tests more verbose, with additional statements comparable to the creation of mocks and stubs,
* Keep the annotations into the binary classes, making the usage of **Catadioptre** visible at runtime and shipped with
  your production code,
* Manipulate the byte code of your production classes, and potentially introducing differences between the "tested"
  and "actual" version of the classes.

Considering all those impacts, we preferred to apply the following strategy:

* Use reflection to access the private units: this is indeed a bit slower, but keeps the tested code untouched.
* Generate "proxy" methods at compile-time in an isolated source path: the annotations of the private units to test are
  not kept into the binary and the generated classes are not shipped with your production code.

## Why the name Catadioptre?

As describe just above, **Catadioptre** is using reflection to serve its purpose and in French, a "catadioptre" is a
reflector you generally use on bicycles or road security equipments.

## How to use Catadioptre?

In order to provide the best experience possible with your language of choice, we created two sets of utils, that
provide equivalent features but in an idiomatic way.

You can discover how to use Catadioptre **[for Java](./java.md)** or **[for Kotlin](./kotlin.md)**.

You will also find examples in the GitHub repository for both languages, using Gradle and Maven.

## Catadioptre, mocks, stubs and spies

The generated functions and methods are just "facilities" to access to the private and protected members of a class in
your tests.
Hence, they are not used by the main code and are not relevant to mock, stub or spy your instances.

We encourage you to evaluate the absolute necessity to stub or verify a protected or private method in your tests and
use
a mocking library that supports such feature, like [mockk for Kotlin](https://mockk.io/)

## Visibility of classes to generate code

The visibility of the generated code is constrained by the one of the class declaring the testable members.

Classes with a visibility less than `package-protected` are not supported.

**WARNING:** When generating the proxies for Kotlin with Maven, internal classes are not supported. There is no known
issue with Gradle.

## Changes

### Version 0.6.5

Kotlin proxy functions generation:

- Replaces the types recognized as error.NonExistentClass in Kotlin metadata by the types visible in the Java signature.
- Improves the support of kotlin.Comparator for which Kotlin metadata does not carry any type argument.

### Version 0.6.4

Kotlin proxy functions generation:

- Fixes the mapping from Kotlin to Java type when it is nullable.
- Disables the Kotlin annotation processor when no Kotlin environment is present.

### Version 0.6.3

Kotlin proxy functions generation:

- Fixes the infinite loop on types with themselves as type arguments.

### Version 0.6.2

Kotlin proxy functions generation:

- Removes the unexpected annotations from Kotlin proxy functions.

### Version 0.6.1

Kotlin proxy functions generation:

- Fixes the generation of proxy methods for Kotlin properties when the type is implied from assignment.

### Version 0.6.0

- Better support of the Kotlin functions, keeps the varargs arguments and the Kotlin types (collections, numbers,
strings) and adds support for suspend functions.

## Versions compatibilities

| Catadioptre | Java  | Kotlin      |
|-------------|-------|-------------|
| 0.6.+       | 11-21 | 1.8.+       |
| 0.5.+       | <= 11 | 1.8.+-1.9.+ |

## Further currently known limitations

We are still in the development phase and improving the library as fast as we can. Here are known limitations:

* Static code and Kotlin objects are not supported
* There are two different annotations to enable the proxy generation for Java and Kotlin, we plan to consolidate them in
  the future
* Integration with Gradle and Maven is not optimal, we will create plugins to make the work with Catadioptre more
  transparent
* Kotlin extended members are not supported
* Members in anonymous classes are not supported

We are heavily working to remove those limitations and improve your experience as much as possible.