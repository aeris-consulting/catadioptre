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
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    java
    kotlin("jvm") version "1.4.30"
    kotlin("kapt") version "1.4.30"
    id("net.ltgt.apt") version "0.21" apply false

    id("nebula.contacts") version "5.1.0"
    id("nebula.info") version "9.1.1"
    id("nebula.maven-publish") version "17.3.3"
    id("nebula.maven-scm") version "17.3.3"
    id("nebula.maven-manifest") version "17.3.3"
    id("nebula.maven-apache-license") version "17.3.3"
    signing

    id("org.sonarqube") version "3.3"
}

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.BIN
}

val target = JavaVersion.VERSION_1_8
val kotlinCompileTarget = "1.8"

allprojects {
    group = "io.aeris-consulting"
    version = File(rootDir, "project.version").readText().trim()

    apply(plugin = "java")
    apply(plugin = "nebula.contacts")
    apply(plugin = "nebula.info")
    apply(plugin = "nebula.maven-publish")
    apply(plugin = "nebula.maven-scm")
    apply(plugin = "nebula.maven-manifest")
    apply(plugin = "nebula.maven-developer")
    apply(plugin = "nebula.maven-apache-license")
    apply(plugin = "signing")
    apply(plugin = "nebula.javadoc-jar")
    apply(plugin = "nebula.source-jar")

    infoBroker {
        excludedManifestProperties = listOf(
            "Manifest-Version", "Module-Owner", "Module-Email", "Module-Source",
            "Built-OS", "Build-Host", "Build-Job", "Build-Host", "Build-Job", "Build-Number", "Build-Id", "Build-Url",
            "Built-Status"
        )
    }

    contacts {
        addPerson("catadioptre@aeris-consulting.com", delegateClosureOf<nebula.plugin.contacts.Contact> {
            moniker = "AERIS-Consulting e.U."
            github = "aeris-consulting"
            role("Owner")
        })
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    java {
        sourceCompatibility = target
        targetCompatibility = target
    }

    signing {
        publishing.publications.forEach { sign(it) }
    }
}

subprojects {
    val ossrhUsername: String? by project
    val ossrhPassword: String? by project
    publishing {
        repositories {
            maven {
                val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                name = "sonatype"
                url = uri(if (project.version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                credentials {
                    username = ossrhUsername
                    password = ossrhPassword
                }
            }
        }
    }

    tasks {
        withType<Jar> {
            archiveBaseName.set(project.name)
        }

        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                useIR = true
                jvmTarget = kotlinCompileTarget
                javaParameters = true
                freeCompilerArgs += listOf(
                    "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
                )
            }
        }

        val replacedPropertiesInResources = mapOf("project.version" to project.version)
        withType<ProcessResources> {
            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to replacedPropertiesInResources)
        }

        named<Test>("test") {
            ignoreFailures = System.getProperty("ignoreUnitTestFailures", "false").toBoolean()
            useJUnitPlatform()
        }

        withType<Test> {
            useJUnitPlatform()
            testLogging {
                events(FAILED, STANDARD_ERROR)
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

                debug {
                    events(STARTED, STANDARD_OUT, FAILED, SKIPPED, PASSED, STANDARD_ERROR)
                    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                }

                info {
                    events(FAILED, SKIPPED, PASSED, STANDARD_ERROR)
                    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                }
            }
        }
    }
}

val testTasks = subprojects.flatMap {
    val testTasks = mutableListOf<Test>()
    (it.tasks.findByName("test") as Test?)?.apply {
        testTasks.add(this)
    }
    testTasks
}

tasks.register("testReport", TestReport::class) {
    this.group = "verification"
    destinationDir = file("${buildDir}/reports/tests")
    reportOn(*(testTasks.toTypedArray()))
}

tasks {
    withType<AbstractPublishToMaven> {
        enabled = false
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "aeris-consulting_catadioptre")
        property("sonar.organization", "aeris-consulting")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}