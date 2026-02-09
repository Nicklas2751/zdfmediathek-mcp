plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.spring") version "2.3.0"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "7.2.2.6593"
    jacoco
}

group = "eu.wiegandt"
version = System.getenv("VERSION") ?: "0.0.1-SNAPSHOT"
description = "zdfmediathek-mcp"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["springAiVersion"] = "1.1.2"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webflux")
    implementation("org.springframework.boot:spring-boot-starter-graphql")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.ai:spring-ai-starter-mcp-client-webflux")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.wiremock.integrations:wiremock-spring-boot:4.1.0")
    testImplementation("io.github.nilwurtz:wiremock-graphql-extension:0.9.0") {
        exclude(group = "org.wiremock", module = "wiremock")
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

sonar {
    properties {
        property("sonar.projectKey", "Nicklas2751_zdfmediathek-mcp")
        property("sonar.organization", "nicklas2751-github")
    }
}


tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude("**/ZdfMediathekClient*", "**/ZdfMediathekClient$*")
            }
        })
    )
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
    imageName.set("ghcr.io/nicklas2751/zdfmediathek-mcp:${project.version}")

    environment.set(mapOf(
        "BP_JVM_VERSION" to "21",
        "BP_HEALTH_CHECKER_ENABLED" to "true"
    ))
}
