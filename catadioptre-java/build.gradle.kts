plugins {
    java
}

java {
    description = "Reflection utils to use your private and protected methods and fields in Java tests"
}

val junitVersion: String by project

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
