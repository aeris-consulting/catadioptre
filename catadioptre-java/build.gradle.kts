plugins {
    java
}

description = "Reflection utils to use your private and protected methods and fields in Java tests"

val junitVersion: String by project

dependencies {
    testImplementation("org.mockito:mockito-core:3.+")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
