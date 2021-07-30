# Katadioptre

[![Continuous Integration](https://github.com/aeris-consulting/katadioptre/actions/workflows/gradle-master.yml/badge.svg)](https://github.com/aeris-consulting/katadioptre/actions/workflows/gradle-master.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.aeris-consulting/katadioptre?label=Maven%20central&style=plastic&versionPrefix=0.1.0)](https://img.shields.io/maven-central/v/io.aeris-consulting/katadioptre?label=Maven%20central&style=plastic&versionPrefix=0.1.0)


## Reflection utils for testing in Kotlin

**Katadioptre** is a lightweight library to manipulate the instances and objects in Kotlin.

Its main goal is to provide access to private members. With **Katadioptre**, you can:

* set or get private, protected properties,
* execute private and protected functions.

**Katadioptre** supports variable, optional and named arguments in functions as well as `suspend` functions.

## Why the name Katadioptre?

In French, a "catadioptre" is a reflector you generally have on bicycles or road security equipments.

_Kotlin + Catadioptre = Katadioptre_

## How to use Katadioptre

### Setting a private or protected property

Setting a property can be performed with the extension function `setProperty` on any instance or object.
The function takes the name of the property as first argument and the value to set as second.
```
 instance.setProperty("myProperty", 456)
    .setProperty("myOtherProperty", true)
```
Note that you can chain the calls for a fluent coding.

You can also use the `infix` function `withProperty`
```
 instance withProperty "myProperty" being 456
```

While you can use `setProperty` or `withProperty` to set a property to null, a more concise option is to use the function
`clearProperty´:
```
instance clearProperty "myProperty"
```

### Getting a private or protected property

To get the current value of a property, you can use the function `getProperty` providing the property name as first argument.
```
val value: Int = instance getProperty "myProperty"
```
The above example shows how to proceed with the `infix` approach, an alternative is the following:
```
val value = instance.getProperty<Int>("myProperty")
```

### Executing a private or protected function

#### Functions without parameter
The simplest way to execute a niladic function is to use `invokeNoArgs` providing the function name as argument:

```
val value: Int = instance invokeNoArgs "calculateRandomInteger"
```

The equivalent function to invoke a `suspend` function without parameter is `coInvokeNoArgs`.

```
val value: Int = instance coInvokeNoArgs "suspendedCalculateRandomInteger"
```

#### Functions with one or more parameters

The functions `invokeInvisible` (and respectively `coInvokeInvisible` for the `suspend` functions) executes
the function with the name passed as first argument, using the parameters provided in the same order.

The following example executes the function `divide` passing it `12.0` and `6.0` as arguments.
```
val result: Double = instance.invokeInvisible("divide", 12.0, 6.0)
```
The value of `result` is `2.0`.

Given the richness of the functions declarations in Kotlin - optional parameters, varargs, it is not trivial to resolve the real function to execute when several ones have the same name.

To help in this resolution, **Katadioptre** requires information about the types or names of the null, omitted optional or variable arguments.

We provide convenient arguments wrappers to achieve this in a concise way.

#### Passing a null argument

To simply pass a null value as an argument while providing the type of the argument, you can use the wrapper `nullOf`.

```
val value: Double? = instance.invokeInvisible("divideIfNotNull", nullOf<Double>(), named("divider", 6.0))
```

##### Naming an argument
Wrap the argument with the function `named`, giving first the name, then the value.
To execute the function
```
private fun divide(value: Double, divider: Double) : Double = value / divider
```
You can use:
```
val value: Double = instance.invokeInvisible("divide", named("value", 12.0), named("divider", 6.0))
```
When using named arguments, their order in the call no longer matters.

If the value is null, simply use `namedNull<Double>("value")`. The type of the argument is required to match the function in case of method overloading.

```
val value: Double? = instance.invokeInvisible("divideIfNotNull", namedNull<Double>("value"), named("divider", 6.0))
```

#### Passing a variable argument

To provide all the values of a variable argument, you have to use `vararg`:
```
val result: Double = instance.invokeInvisible("divideTheSum", 2.0, vararg(1.0, 3.0, 6.0))
```
This will execute the following function summing `1.0`, `3.0` and `6.0` (= `10.0`) and dividing the sum by `2.0`:
```
private fun divideSum(divider: Double = 1.0, vararg values: Double?): Double {
    return values.filterNotNull().sum() / divider
}
```

#### Omitting an optional argument to use the default value

When you want to execute a function that has a parameter with a default value you want to apply, use `omitted`

This function as a default divider set to `1.0`.
```
val result: Double = instance.invokeInvisible("divideTheSum", omitted<Double>(), vararg(1.0, 3.0, 6.0))
```
This will execute the following function summing `1.0`, `3.0` and `6.0` (= `10.0`) and dividing the sum by `1.0`, the default value of 
the parameter `divider`:
```
private fun divideSum(divider: Double = 1.0, vararg values: Double?): Double {
    return values.filterNotNull().sum() / divider
}
```

#### Combining wrappers

Last but not least, you can also combine the wrappers to create named variable arguments, or named omitted.
```
val result: Double = instance.invokeInvisible("divideTheSum", named("divider", omitted<Double>()), named("values", vararg(1.0, 3.0, 6.0)))
```

While this is in most cases unnecessary, this might help in resolving to the adequate function to execute when functions of a class are too similar.
