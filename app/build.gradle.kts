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
    implementation(libs.spring.ai.gigachat)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("chat.giga:spring-ai-starter-model-gigachat:1.1.1")
    implementation("org.springframework.ai:spring-ai-starter-mcp-client:1.1.1")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.projectlombok:lombok")
    implementation("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")


}

application {
    mainClass.set("org.example.app.Application")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}