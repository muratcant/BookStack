plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    kotlin("plugin.jpa") version "2.2.21"
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "org.muratcant"
version = "0.0.1-SNAPSHOT"
description = "BookStack - Hybrid Bookstore & Reading Library"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val kotestVersion = "5.9.1"

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Database
    runtimeOnly("org.postgresql:postgresql")

    // OpenAPI / Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.0")

    // Test - Kotest
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")

    // Test - Spring Boot
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation("org.springframework.boot:spring-boot-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
    testImplementation("org.springframework:spring-test")

    // Test - MockK (Kotlin-native mocking)
    testImplementation("io.mockk:mockk:1.13.13")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

// ===========================================
// Test Configuration
// ===========================================

// Integration Test Source Set
sourceSets {
    create("integrationTest") {
        kotlin.srcDir("src/integrationTest/kotlin")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.testImplementation.get())
}
val integrationTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    // Integration Test - Spring Boot 4.0 modüler yapı
    "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-webmvc-test")
    "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito", module = "mockito-core")
    }
    "integrationTestImplementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
    "integrationTestImplementation"("org.jetbrains.kotlin:kotlin-test-junit5")
    "integrationTestRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

// Unit Tests (default test task) - No DB required
tasks.test {
    useJUnitPlatform()
    description = "Run unit tests (no database required)"
}

// Handle duplicate resources
tasks.named<ProcessResources>("processIntegrationTestResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

// Docker Compose Tasks
tasks.register<Exec>("dockerComposeUp") {
    description = "Start test database"
    commandLine("/usr/local/bin/docker", "compose", "-f", "docker-compose.test.yml", "up", "-d", "--wait")
}

tasks.register<Exec>("dockerComposeDown") {
    description = "Stop test database"
    commandLine("/usr/local/bin/docker", "compose", "-f", "docker-compose.test.yml", "down", "-v")
    isIgnoreExitValue = true
}

// Integration Tests
tasks.register<Test>("integrationTest") {
    useJUnitPlatform()
    description = "Run integration tests (requires database)"
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    dependsOn("dockerComposeUp")
    finalizedBy("dockerComposeDown")

    doFirst {
        // Wait for DB to be ready
        Thread.sleep(5000)
    }
}

// Check task runs both unit and integration tests
tasks.named("check") {
    dependsOn("integrationTest")
}

// ===========================================
// JaCoCo Configuration
// ===========================================

jacoco {
    toolVersion = "0.8.12"
}

// Enable JaCoCo for unit tests
tasks.test {
    finalizedBy("jacocoTestReport")
}

// Enable JaCoCo for integration tests
tasks.named<Test>("integrationTest") {
    finalizedBy("jacocoIntegrationTestReport")
}

// Unit test coverage report
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    
    classDirectories.setFrom(
        files(sourceSets["main"].output.classesDirs).asFileTree.matching {
            exclude(
                "**/BookStackApplication*",
                "**/shared/**",
                "**/config/**",
                "**/*Request*",
                "**/*Response*"
            )
        }
    )
}

// Integration test coverage report
tasks.register<JacocoReport>("jacocoIntegrationTestReport") {
    dependsOn("integrationTest")
    
    executionData(fileTree(layout.buildDirectory).include("jacoco/integrationTest.exec"))
    sourceSets(sourceSets["main"])
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/integrationTest/html"))
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/integrationTest/jacocoIntegrationTestReport.xml"))
    }
    
    classDirectories.setFrom(
        files(sourceSets["main"].output.classesDirs).asFileTree.matching {
            exclude(
                "**/BookStackApplication*",
                "**/shared/**",
                "**/config/**",
                "**/*Request*",
                "**/*Response*"
            )
        }
    )
}

// Combined coverage report (unit + integration)
tasks.register<JacocoReport>("jacocoFullReport") {
    description = "Generates combined coverage report for unit and integration tests"
    group = "verification"
    
    dependsOn(tasks.test, "integrationTest")
    
    executionData(fileTree(layout.buildDirectory).include("jacoco/*.exec"))
    sourceSets(sourceSets["main"])
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/full/html"))
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/full/jacocoFullReport.xml"))
    }
    
    classDirectories.setFrom(
        files(sourceSets["main"].output.classesDirs).asFileTree.matching {
            exclude(
                "**/BookStackApplication*",
                "**/shared/**",
                "**/config/**",
                "**/*Request*",
                "**/*Response*"
            )
        }
    )
}
