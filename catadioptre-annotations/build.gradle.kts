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
}

java {
    description = "Generates code to use your private and protected members in tests, for Java and Kotlin"
}

val kotlinpoetVersion: String by project
val javapoetVersion: String by project

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(project(":catadioptre-kotlin"))
    implementation(project(":catadioptre-java"))
    implementation("com.squareup:kotlinpoet:$kotlinpoetVersion")
    implementation("com.squareup:kotlinpoet-classinspector-elements:$kotlinpoetVersion")
    implementation("com.squareup:kotlinpoet-metadata-specs:$kotlinpoetVersion")
    implementation("com.squareup:javapoet:$javapoetVersion")
}