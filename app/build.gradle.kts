plugins {
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    application
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Lombok dependencies
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Spring Boot configuration processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // SpringDoc OpenAPI (Swagger для Spring Boot 3.x)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("chat.giga:spring-ai-starter-model-gigachat:1.1.1")

    // Spring Boot Starter JDBC
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // PostgreSQL драйвер
    runtimeOnly("org.postgresql:postgresql")
}

application {
    mainClass.set("org.example.Application")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}