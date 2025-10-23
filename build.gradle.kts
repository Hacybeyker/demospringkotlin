plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "com.hacybeyker"
version = "0.0.1-SNAPSHOT"
description = "SpringKotlin"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}


repositories {
    mavenCentral()
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


kotlin {
    //jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

// GraalVM Native Image configuration
// Provides the `nativeCompile` and `nativeTest` tasks and configures resource autodetection
// Usage examples:
//  - ./gradlew nativeCompile
//  - ./gradlew bootBuildImage (builds a container) – uses BP_NATIVE_IMAGE=true

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            imageName.set("${project.name}")
            resources.autodetect()
        }
    }
}

// Configure bootBuildImage to build a native container image via Paketo buildpacks
// Run: ./gradlew bootBuildImage
// Resulting image name: projectName:version
// Requires Docker/Podman running

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
    imageName.set("${project.name}:${project.version}")
    builder.set("paketobuildpacks/builder-jammy-tiny")
    environment.set(mapOf("BP_NATIVE_IMAGE" to "true"))
}


tasks.withType<Test> {
    useJUnitPlatform()
}

/*
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "21"
    }
}*/