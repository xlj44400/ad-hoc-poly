import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.70"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.springframework.boot") version "2.3.0.M1"
}

val arrowSnapshotVersion = "0.10.5-SNAPSHOT"
val arrowVersion = "0.10.4"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.springframework.fu:spring-fu-kofu:0.3.0.M1")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.+")

    implementation("io.arrow-kt:arrow-core:$arrowSnapshotVersion")
    implementation("io.arrow-kt:arrow-fx:$arrowSnapshotVersion")

    implementation(project(":validation-templates"))

    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.spring.io/milestone")
    maven("https://repo.spring.io/snapshot")
    maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
    maven(url = "https://oss.jfrog.org/artifactory/oss-snapshot-local/")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=enable")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configurations.all {
    exclude(module = "jakarta.validation-api")
    exclude(module = "hibernate-validator")
}
