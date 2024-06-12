plugins {
    `kotlin-dsl`
    application
}

group = "com.singlestore"
version = properties["version"] as String


repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("info.picocli:picocli:4.7.5")
    implementation("com.singlestore:singlestore-jdbc-client:1.2.3")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    kotlinOptions.javaParameters = true
}

java {
    withSourcesJar()
}

application {
    mainClass.set("com.singlestore.hasura.cli.CLI")
}
