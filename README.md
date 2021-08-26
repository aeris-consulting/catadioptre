# Catadioptre

## Easily work with private members when testing in Java and Kotlin

[![Maven Central](https://img.shields.io/maven-central/v/io.aeris-consulting/catadioptre-annotations.svg?color=blue&label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.aeris-consulting%22%20AND%20a:%22catadioptre*%22)
[![Build](https://github.com/aeris-consulting/catadioptre/actions/workflows/gradle-master.yml/badge.svg)](https://github.com/aeris-consulting/catadioptre/actions/workflows/gradle-master.yml)
[![Scan with Detekt](https://github.com/aeris-consulting/catadioptre/actions/workflows/detekt-analysis.yml/badge.svg)](https://github.com/aeris-consulting/catadioptre/actions/workflows/detekt-analysis.yml)

**Catadioptre** is a lightweight library to work with private members in Kotlin and Java using reflection.

With **Catadioptre**, you can:

* read or write private and protected properties / fields,
* execute private and protected functions / methods,
* generate "proxy" methods at compilation-time to easily access to the private members in your tests.

**Catadioptre** supports variable arguments in Java and Kotlin as well as optional and named arguments as well as `suspend` functions in Kotlin.

## Why and how to use Catadioptre?

We encourage you to read the [official documentation](https://catadioptre.aeris-consulting.io/) to better understand the challenges that **Catadioptre** solves and how it helps you to do it.
