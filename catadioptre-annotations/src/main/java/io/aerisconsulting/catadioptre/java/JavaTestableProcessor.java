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

import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.Builder;
import io.aerisconsulting.catadioptre.ReflectionFieldUtils;
import io.aerisconsulting.catadioptre.ReflectionMethodUtils;
import io.aerisconsulting.catadioptre.Testable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Processor that generates source code to provide indirect access to private fields and methods.
 *
 * @author Eric Jessé
 */
@SupportedAnnotationTypes("io.aerisconsulting.catadioptre.Testable")
public class JavaTestableProcessor extends AbstractProcessor {

    private static final String INSTANCE_PARAM_TYPE = "INSTANCE";

    private Elements elementUtils;

    private File generatedDir;

    private JavaSpecificationUtils specificationUtils;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // The maximal supported is the 21. But when running with a lower JDK, the enum SourceVersion.RELEASE_21
        // does not exist. So a fallback is done onto the latest source version of the current JDK.
        final Optional<SourceVersion> expectedRelease = Arrays.stream(SourceVersion.values()).filter((version) -> "RELEASE_21".equals(version.name())).findFirst();
        return expectedRelease.orElse(SourceVersion.latest());
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        specificationUtils = new JavaSpecificationUtils();

        try {
            // Finds out the folder where generated sources are written.
            final JavaFileObject builderFile = processingEnv.getFiler().createSourceFile("CatadioptreLocationTest");
            generatedDir = new File(new File(builderFile.getName()).getParentFile().getParentFile(), "catadioptre");
            builderFile.openWriter().close();
            builderFile.delete();
            generatedDir.mkdirs();
        } catch (IOException e) {
            processingEnv.getMessager()
                    .printMessage(Kind.ERROR, "[Catadioptre] Could not detect the generation folder: " + e.getMessage());
        }
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(Testable.class);
        if (annotatedElements.isEmpty() || generatedDir == null) {
            return false;
        }

        // Groups the annotated elements by declaring class.
        final Map<TypeElement, Set<Element>> annotatedElementsByDeclaringType = new HashMap<>();
        annotatedElements.stream()
                .forEach(element -> annotatedElementsByDeclaringType
                        .computeIfAbsent((TypeElement) element.getEnclosingElement(), k -> new HashSet<>())
                        .add(element));

        annotatedElementsByDeclaringType.forEach(this::generateProxyMethods);
        return true;
    }

    /**
     * Generates all the proxy methods for the annotated members of the class.
     *
     * @param declaringType the class declaring the members to proxy
     * @param elements      the annotated elements
     */
    private void generateProxyMethods(final TypeElement declaringType, final Set<Element> elements) {
        final String packageName = elementUtils.getPackageOf(declaringType).toString();
        final String testableClassName = "Testable" + declaringType.getSimpleName().toString();
        final Builder testableTypeSpec = TypeSpec.classBuilder(testableClassName);
        testableTypeSpec.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());

        final AtomicBoolean generateFile = new AtomicBoolean();
        elements.forEach(element -> {
            if (element instanceof ExecutableElement) {
                final ExecutableElement methodElement = (ExecutableElement) element;
                if (JavaVisibilityUtils.canBePublic(methodElement)) {
                    generateFile.set(true);
                    addTestableMethod(testableTypeSpec, declaringType, methodElement, Modifier.PUBLIC);
                } else {
                    final String methodSignature = declaringType.getQualifiedName() + "." + methodElement;
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.WARNING,
                            "[Catadioptre] Cannot generate the proxy method for the function " + methodSignature
                                    + ", one of the used type has a too low visibility"
                    );
                }
            } else if (element instanceof VariableElement) {
                final VariableElement variableElement = (VariableElement) element;
                if (JavaVisibilityUtils.canBePublic(variableElement)) {
                    generateFile.set(true);
                    addTestableField(testableTypeSpec, declaringType, variableElement, Modifier.PUBLIC);
                } else {
                    final String fieldSignature =
                            declaringType.getQualifiedName() + "." + variableElement.getSimpleName();
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.WARNING,
                            "[Catadioptre] Cannot generate the proxy method for the function " + fieldSignature
                                    + ", the type of the declaring class or the field has a too low visibility"
                    );
                }
            }
        });

        // Then writes the content of the generated class to the file.
        if (generateFile.get()) {
            try {
                final JavaFile testableClassFile = JavaFile.builder(packageName, testableTypeSpec.build()).build();
                testableClassFile.writeTo(generatedDir);
            } catch (IOException e) {
                processingEnv.getMessager()
                        .printMessage(Kind.ERROR,
                                "[Catadioptre] Could not generate the testable source for class " + packageName + "."
                                        + declaringType.getSimpleName().toString() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Builds all the proxy methods to access to a field.
     *
     * @param typeSpecBuilder the builder for the class declaring the proxy method
     * @param declaringType   the class declaring the annotated field
     * @param element         the field to access behind the proxy method
     * @param visibility      the visibility of the proxy method
     */
    private void addTestableField(final TypeSpec.Builder typeSpecBuilder, final TypeElement declaringType,
                                  final VariableElement element, final Modifier visibility) {
        final Testable annotation = element.getAnnotation(Testable.class);

        if (annotation.getter()) {
            buildGetterMethod(typeSpecBuilder, declaringType, element, visibility);
        }
        if (annotation.setter()) {
            buildSetterMethod(typeSpecBuilder, declaringType, element, visibility);
        }
        if (annotation.clearer()) {
            buildClearerMethod(typeSpecBuilder, declaringType, element, visibility);
        }
    }

    /**
     * Builds a proxy method to read the content of a field using reflection.
     *
     * @param typeSpecBuilder the builder for the class declaring the proxy method
     * @param declaringType   the class declaring the annotated field
     * @param element         the field to access behind the proxy method
     * @param visibility      the visibility of the proxy method
     */
    private void buildGetterMethod(final Builder typeSpecBuilder, final TypeElement declaringType,
                                   final VariableElement element, final Modifier visibility) {
        final MethodSpec.Builder methodBuilder = prepareProxyMethod(declaringType,
                MethodSpec.methodBuilder(element.getSimpleName().toString()), visibility, false)
                .addStatement("return $T.getField(instance, $S)", ClassName.get(ReflectionFieldUtils.class),
                        element.getSimpleName())
                .returns(TypeName.get(element.asType()));
        typeSpecBuilder.addMethod(methodBuilder.build());
    }

    /**
     * Builds a proxy method to write the content of a field using reflection.
     *
     * @param typeSpecBuilder the builder for the class declaring the proxy method
     * @param declaringType   the class declaring the annotated field
     * @param element         the field to access behind the proxy method
     * @param visibility      the visibility of the proxy method
     */
    private void buildSetterMethod(final Builder typeSpecBuilder, final TypeElement declaringType,
                                   final VariableElement element, final Modifier visibility) {
        final MethodSpec.Builder methodBuilder = prepareProxyMethod(declaringType,
                MethodSpec.methodBuilder(element.getSimpleName().toString()), visibility, true)
                .addParameter(TypeName.get(element.asType()), "value")
                .addStatement("$T.setField(instance, $S, value)", ClassName.get(ReflectionFieldUtils.class),
                        element.getSimpleName())
                .addStatement("return instance");
        typeSpecBuilder.addMethod(methodBuilder.build());
    }

    /**
     * Builds a proxy method to set the content of a field to null using reflection.
     *
     * @param typeSpecBuilder the builder for the class declaring the proxy method
     * @param declaringType   the class declaring the annotated field
     * @param element         the field to access behind the proxy method
     * @param visibility      the visibility of the proxy method
     */
    private void buildClearerMethod(final Builder typeSpecBuilder, final TypeElement declaringType,
                                    final VariableElement element, final Modifier visibility) {
        final String capitalizedName = capitalize(element.getSimpleName().toString());
        final MethodSpec.Builder methodBuilder = prepareProxyMethod(declaringType,
                MethodSpec.methodBuilder("clear" + capitalizedName), visibility, true)
                .addStatement("$T.clearField(instance, $S)", ClassName.get(ReflectionFieldUtils.class),
                        element.getSimpleName())
                .addStatement("return instance");
        typeSpecBuilder.addMethod(methodBuilder.build());
    }

    private String capitalize(final String value) {
        final char[] chars = value.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * Builds a proxy method to execute another method using reflection.
     *
     * @param typeSpecBuilder the builder for the class declaring the proxy method
     * @param declaringType   the class declaring the annotated method
     * @param element         the method to access behind the proxy method
     * @param visibility      the visibility of the proxy method
     */
    private void addTestableMethod(final TypeSpec.Builder typeSpecBuilder, final TypeElement declaringType,
                                   final ExecutableElement element, final Modifier visibility) {
        final MethodSpec.Builder methodBuilder = prepareProxyMethod(declaringType,
                MethodSpec.methodBuilder(element.getSimpleName().toString()), visibility, false)
                .returns(TypeName.get(element.getReturnType()));
        element.getTypeParameters().forEach(e -> methodBuilder.addTypeVariable(TypeVariableName.get(e)));
        element.getParameters().forEach(p -> methodBuilder.addParameter(
                ParameterSpec.builder(TypeName.get(p.asType()), p.getSimpleName().toString()).build()));
        String params = element.getParameters().stream().map(p -> p.getSimpleName().toString())
                .collect(Collectors.joining(","));
        if (!params.isEmpty()) {
            params = ", " + params;
        }
        final String returnStatement;
        if (element.getReturnType().getKind() == TypeKind.VOID) {
            returnStatement = "";
        } else {
            returnStatement = "return ";
        }
        methodBuilder.addStatement(returnStatement + "$T.executeInvisible(instance, $S" + params + ")",
                ClassName.get(ReflectionMethodUtils.class),
                element.getSimpleName().toString());
        typeSpecBuilder.addMethod(methodBuilder.build());
    }

    /**
     * Generally configures the proxy method.
     *
     * @param declaringType       the class declaring the field or method to proxy
     * @param methodBuilder       the builder for the proxy method to complete
     * @param visibility          the visibility of the proxy method
     * @param returnsInstanceType specifies if the returned type is the same as declaringType.
     */
    private MethodSpec.Builder prepareProxyMethod(final TypeElement declaringType,
                                                  final MethodSpec.Builder methodBuilder, final Modifier visibility, final boolean returnsInstanceType) {
        methodBuilder
                .addTypeVariable(
                        TypeVariableName.get(INSTANCE_PARAM_TYPE, specificationUtils.createTypeName(declaringType)))
                .addModifiers(Modifier.STATIC)
                .addParameter(TypeVariableName.get(INSTANCE_PARAM_TYPE), "instance");
        declaringType.getTypeParameters().forEach(e -> methodBuilder.addTypeVariable(TypeVariableName.get(e)));
        if (visibility != null) {
            methodBuilder.addModifiers(visibility);
        }
        if (returnsInstanceType) {
            methodBuilder.returns(TypeVariableName.get(INSTANCE_PARAM_TYPE));
        }
        return methodBuilder;
    }
}
