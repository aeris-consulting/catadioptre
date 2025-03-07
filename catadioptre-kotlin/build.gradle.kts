import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    kotlin("jvm")
}

description = "Reflection utils to use your private and protected functions and properties in Kotlin tests"

val junitVersion: String by project
val assertkVersion: String by project
val kotlinCoroutinesVersion: String by project

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        javaParameters = true
        freeCompilerArgs.add("-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    testImplementation("com.willowtreeapps.assertk:assertk:$assertkVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertkVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${kotlinCoroutinesVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("io.mockk:mockk:1.+")
}
