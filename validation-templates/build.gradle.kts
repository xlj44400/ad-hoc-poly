import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.70"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
}

val arrowSnapshotVersion = "0.10.5-SNAPSHOT"
val arrowVersion = "0.10.4"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.r2dbc:r2dbc-h2")
    implementation("org.springframework.data:spring-data-r2dbc:1.0.0.RC1")

    implementation("io.arrow-kt:arrow-core:$arrowSnapshotVersion")
    implementation("io.arrow-kt:arrow-fx:$arrowSnapshotVersion")
    implementation("io.arrow-kt:arrow-fx-reactor:$arrowSnapshotVersion")
}

dependencyManagement {
    imports {
        mavenBom("io.r2dbc:r2dbc-bom:Arabba-SR2")
    }
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
