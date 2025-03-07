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
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT
import org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED

plugins {
    java
    kotlin("jvm") version "2.0.0" apply false
    kotlin("kapt") version "2.0.0" apply false
    `maven-publish`
    signing

    id("org.sonarqube") version "6.0.+"
}

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.BIN
}

val target = JavaVersion.VERSION_11

allprojects {
    group = "io.aeris-consulting"
    version = File(rootDir, "project.version").readText().trim()
    apply(plugin = "signing")
    apply(plugin = "maven-publish")

    val signingKeyId = "signing.keyId"
    if (System.getProperty(signingKeyId) != null || System.getenv(signingKeyId) != null) {
        signing {
            publishing.publications.forEach { sign(it) }
        }
    }
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenLocal()
        mavenCentral()
    }

    java {
        sourceCompatibility = target
        targetCompatibility = target
    }

    val project = this
    val ossrhUsername: String? by project
    val ossrhPassword: String? by project
    publishing {

        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                pom {
                    name.set(project.name)
                    description.set(project.description)

                    url.set("https://catadioptre.aeris-consulting.io/")
                    licenses {
                        license {
                            name.set("Apache License, Version 2.0 (Apache-2.0)")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    developers {
                        developer {
                            id.set("ericjesse")
                            name.set("Eric Jessé")
                        }
                    }
                    scm {
                        url.set("https://github.com/aeris-consulting/catadioptre.git/")
                    }
                }
            }
        }

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