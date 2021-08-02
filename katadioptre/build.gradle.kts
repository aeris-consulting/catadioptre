plugins {
    java
    kotlin("jvm")
}

java {
    description = "Reflection utils to use your private and protected functions and properties in tests"
}

val junitVersion: String by project
val assertkVersion: String by project
val kotlinCoroutinesVersion: String by project

dependencies {
    compileOnly(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    testImplementation("com.willowtreeapps.assertk:assertk:$assertkVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertkVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${kotlinCoroutinesVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
