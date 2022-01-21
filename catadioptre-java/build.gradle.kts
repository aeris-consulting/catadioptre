plugins {
    java
}

java {
    description = "Reflection utils to use your private and protected methods and fields in Java tests"
}

val junitVersion: String by project

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.mockito:mockito-core:3.+")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
