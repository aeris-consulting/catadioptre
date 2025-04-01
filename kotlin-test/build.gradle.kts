import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
plugins {
    java
    kotlin("jvm")
    kotlin("kapt")
}

description = "Module to test the annotation processing for Kotlin"

val junitVersion: String by project
val assertkVersion: String by project
val kotlinCoroutinesVersion: String by project

kapt.useBuildCache = false

kotlin {
    // Adds the generated sources to the test sources.
    sourceSets["test"].kotlin.srcDir(layout.buildDirectory.dir("generated/source/kaptKotlin/catadioptre"))
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.add("-Xsuppress-version-warnings")
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    compileOnly(project(":catadioptre-annotations"))
    compileOnly("jakarta.transaction:jakarta.transaction-api:2.+")

    kapt(project(":catadioptre-annotations"))

    testImplementation(project(":catadioptre-kotlin"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${kotlinCoroutinesVersion}")
    testImplementation("com.willowtreeapps.assertk:assertk:$assertkVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertkVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("io.mockk:mockk:1.+")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<AbstractPublishToMaven> {
    enabled = false
}
