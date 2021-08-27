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
}

java {
    description =
        "Catadioptre example in Java for Gradle with Kotlin DSL - Tests and demos the application of the annotations"
}

// Adds the generated sources to the test sources.
java.sourceSets["test"].java.srcDir(layout.buildDirectory.dir("generated/sources/annotationProcessor/java/catadioptre"))

val junitVersion: String by project

dependencies {
    compileOnly(project(":catadioptre-annotations"))

    annotationProcessor(project(":catadioptre-annotations"))

    testImplementation(project(":catadioptre-java"))
    testImplementation("org.assertj:assertj-core:3.20.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<AbstractPublishToMaven> {
    enabled = false
}
