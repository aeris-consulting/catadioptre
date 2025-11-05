/*
 * Copyright 2021 AERIS-Consulting e.U.
 *
 * AERIS-Consulting e.U. licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.aerisconsulting.catadioptre.java;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Comprehensive tests for JavaTestableProcessor covering all code generation scenarios.
 */
class JavaTestableProcessorTest {

    @Test
    void shouldGenerateProxyForPrivateField() {
        // given
        JavaFileObject source = JavaFileObjects.forSourceString("test.TestClass",
                "package test;\n" +
                        "import io.aerisconsulting.catadioptre.Testable;\n" +
                        "public class TestClass {\n" +
                        "    @Testable\n" +
                        "    private String privateField = \"value\";\n" +
                        "}"
        );

        // when
        Compilation compilation = javac()
                .withProcessors(new JavaTestableProcessor())
                .compile(source);

        // then
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test/TestableTestClass");
    }

    @Test
    void shouldGenerateGetterSetterClearerForField() {
        // given
        JavaFileObject source = JavaFileObjects.forSourceString("test.TestClass",
                "package test;\n" +
                        "import io.aerisconsulting.catadioptre.Testable;\n" +
                        "public class TestClass {\n" +
                        "    @Testable\n" +
                        "    private String field;\n" +
                        "}"
        );

        // when
        Compilation compilation = javac()
                .withProcessors(new JavaTestableProcessor())
                .compile(source);

        // then
        assertThat(compilation).succeeded();
        JavaFileObject generatedFile = compilation.generatedSourceFile("test/TestableTestClass").get();
        assertThat(compilation)
                .generatedSourceFile("test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("public static <INSTANCE extends TestClass> String field(INSTANCE instance)");
        assertThat(compilation)
                .generatedSourceFile("test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("public static <INSTANCE extends TestClass> INSTANCE field(INSTANCE instance, String value)");
        assertThat(compilation)
                .generatedSourceFile("test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("public static <INSTANCE extends TestClass> INSTANCE clearField(INSTANCE instance)");
    }

    @Test
    void shouldGenerateProxyForPrivateMethod() {
        // given
        JavaFileObject source = JavaFileObjects.forSourceString("test.TestClass",
                "package test;\n" +
                        "import io.aerisconsulting.catadioptre.Testable;\n" +
                        "public class TestClass {\n" +
                        "    @Testable\n" +
                        "    private String privateMethod() {\n" +
                        "        return \"result\";\n" +
                        "    }\n" +
                        "}"
        );

        // when
        Compilation compilation = javac()
                .withProcessors(new JavaTestableProcessor())
                .compile(source);

        // then
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("public static <INSTANCE extends TestClass> String privateMethod(INSTANCE instance)");
        assertThat(compilation)
                .generatedSourceFile("test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("ReflectionMethodUtils.executeInvisible(instance, \"privateMethod\")");
    }

    @Test
    void shouldGenerateProxyForMethodWithParameters() {
        // given
        JavaFileObject source = JavaFileObjects.forSourceString("test.TestClass",
                "package test;\n" +
                        "import io.aerisconsulting.catadioptre.Testable;\n" +
                        "public class TestClass {\n" +
                        "    @Testable\n" +
                        "    private int add(int a, int b) {\n" +
                        "        return a + b;\n" +
                        "    }\n" +
                        "}"
        );

        // when
        Compilation compilation = javac()
                .withProcessors(new JavaTestableProcessor())
                .compile(source);

        // then
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("public static <INSTANCE extends TestClass> int add(INSTANCE instance, int a, int b)");
        assertThat(compilation)
                .generatedSourceFile("test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("ReflectionMethodUtils.executeInvisible(instance, \"add\", a,b)");
    }

    @Test
    void shouldGenerateProxyForVoidMethod() {
        // given
        JavaFileObject source = JavaFileObjects.forSourceString("test.TestClass",
                "package test;\n" +
                        "import io.aerisconsulting.catadioptre.Testable;\n" +
                        "public class TestClass {\n" +
                        "    @Testable\n" +
                        "    private void voidMethod() {\n" +
                        "    }\n" +
                        "}"
        );

        // when
        Compilation compilation = javac()
                .withProcessors(new JavaTestableProcessor())
                .compile(source);

        // then
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("public static <INSTANCE extends TestClass> void voidMethod(INSTANCE instance)");
        assertThat(compilation)
                .generatedSourceFile("test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("ReflectionMethodUtils.executeInvisible(instance, \"voidMethod\")");
    }

    @Test
    void shouldRespectGetterSetterClearerAnnotationParameters() throws Exception {
        // given
        JavaFileObject source = JavaFileObjects.forSourceString("test.TestClass",
                "package test;\n" +
                        "import io.aerisconsulting.catadioptre.Testable;\n" +
                        "public class TestClass {\n" +
                        "    @Testable(getter = true, setter = false, clearer = false)\n" +
                        "    private String field;\n" +
                        "}"
        );

        // when
        Compilation compilation = javac()
                .withProcessors(new JavaTestableProcessor())
                .compile(source);

        // then
        assertThat(compilation).succeeded();
        String generatedCode = compilation
                .generatedSourceFile("test/TestableTestClass")
                .get()
                .getCharContent(true)
                .toString();

        // Should have getter
        assertThat(compilation)
                .generatedSourceFile("test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("public static <INSTANCE extends TestClass> String field(INSTANCE instance)");

        // Should not have setter or clearer (only one field method - the getter)
        int fieldMethodCount = generatedCode.split("\\bfield\\(").length - 1;
        org.junit.jupiter.api.Assertions.assertEquals(1, fieldMethodCount);
    }

    @Test
    void shouldGenerateProxyForGenericClass() {
        // given
        JavaFileObject source = JavaFileObjects.forSourceString("test.TestClass",
                "package test;\n" +
                        "import io.aerisconsulting.catadioptre.Testable;\n" +
                        "public class TestClass<T> {\n" +
                        "    @Testable\n" +
                        "    private T genericField;\n" +
                        "    @Testable\n" +
                        "    private T getGeneric() {\n" +
                        "        return genericField;\n" +
                        "    }\n" +
                        "}"
        );

        // when
        Compilation compilation = javac()
                .withProcessors(new JavaTestableProcessor())
                .compile(source);

        // then
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("<INSTANCE extends TestClass<T>, T>");
    }

    @Test
    void shouldGenerateProxyForMultipleAnnotatedMembers() throws Exception {
        // given
        JavaFileObject source = JavaFileObjects.forSourceString("test.TestClass",
                "package test;\n" +
                        "import io.aerisconsulting.catadioptre.Testable;\n" +
                        "public class TestClass {\n" +
                        "    @Testable\n" +
                        "    private String field1;\n" +
                        "    @Testable\n" +
                        "    private int field2;\n" +
                        "    @Testable\n" +
                        "    private void method1() {}\n" +
                        "    @Testable\n" +
                        "    private String method2(int arg) { return null; }\n" +
                        "}"
        );

        // when
        Compilation compilation = javac()
                .withProcessors(new JavaTestableProcessor())
                .compile(source);

        // then
        assertThat(compilation).succeeded();
        String generated = compilation
                .generatedSourceFile("test/TestableTestClass")
                .get()
                .getCharContent(true)
                .toString();

        // All members should have proxies
        org.junit.jupiter.api.Assertions.assertTrue(generated.contains("field1"));
        org.junit.jupiter.api.Assertions.assertTrue(generated.contains("field2"));
        org.junit.jupiter.api.Assertions.assertTrue(generated.contains("method1"));
        org.junit.jupiter.api.Assertions.assertTrue(generated.contains("method2"));
    }

    @Test
    void shouldNotGenerateFileWhenNoAnnotations() {
        // given
        JavaFileObject source = JavaFileObjects.forSourceString("test.TestClass",
                "package test;\n" +
                        "public class TestClass {\n" +
                        "    private String field;\n" +
                        "}"
        );

        // when
        Compilation compilation = javac()
                .withProcessors(new JavaTestableProcessor())
                .compile(source);

        // then
        assertThat(compilation).succeeded();
        org.junit.jupiter.api.Assertions.assertFalse(
                compilation.generatedSourceFile("test/TestableTestClass").isPresent()
        );
    }

    @Test
    void shouldGeneratePrivateConstructor() {
        // given
        JavaFileObject source = JavaFileObjects.forSourceString("test.TestClass",
                "package test;\n" +
                        "import io.aerisconsulting.catadioptre.Testable;\n" +
                        "public class TestClass {\n" +
                        "    @Testable\n" +
                        "    private String field;\n" +
                        "}"
        );

        // when
        Compilation compilation = javac()
                .withProcessors(new JavaTestableProcessor())
                .compile(source);

        // then
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("private TestableTestClass()");
    }

    @Test
    void shouldGenerateStaticMethods() throws Exception {
        // given
        JavaFileObject source = JavaFileObjects.forSourceString("test.TestClass",
                "package test;\n" +
                        "import io.aerisconsulting.catadioptre.Testable;\n" +
                        "public class TestClass {\n" +
                        "    @Testable\n" +
                        "    private String field;\n" +
                        "}"
        );

        // when
        Compilation compilation = javac()
                .withProcessors(new JavaTestableProcessor())
                .compile(source);

        // then
        assertThat(compilation).succeeded();
        String generated = compilation
                .generatedSourceFile("test/TestableTestClass")
                .get()
                .getCharContent(true)
                .toString();

        // All proxy methods should be static
        org.junit.jupiter.api.Assertions.assertTrue(generated.contains("public static"));
    }

    @Test
    void shouldHandleMethodsWithTypeParameters() {
        // given
        JavaFileObject source = JavaFileObjects.forSourceString("test.TestClass",
                "package test;\n" +
                        "import io.aerisconsulting.catadioptre.Testable;\n" +
                        "import java.util.List;\n" +
                        "public class TestClass {\n" +
                        "    @Testable\n" +
                        "    private <T> T identity(T value) {\n" +
                        "        return value;\n" +
                        "    }\n" +
                        "}"
        );

        // when
        Compilation compilation = javac()
                .withProcessors(new JavaTestableProcessor())
                .compile(source);

        // then
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("<INSTANCE extends TestClass, T>");
    }

    @Test
    void shouldGenerateCorrectPackageStructure() {
        // given
        JavaFileObject source = JavaFileObjects.forSourceString("com.example.test.TestClass",
                "package com.example.test;\n" +
                        "import io.aerisconsulting.catadioptre.Testable;\n" +
                        "public class TestClass {\n" +
                        "    @Testable\n" +
                        "    private String field;\n" +
                        "}"
        );

        // when
        Compilation compilation = javac()
                .withProcessors(new JavaTestableProcessor())
                .compile(source);

        // then
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("com/example/test/TestableTestClass");
        assertThat(compilation)
                .generatedSourceFile("com/example/test/TestableTestClass")
                .contentsAsUtf8String()
                .contains("package com.example.test;");
    }
}
