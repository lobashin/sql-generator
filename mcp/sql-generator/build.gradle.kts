plugins {
    application
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.guava)
    implementation(libs.spring.ai.gigachat)
    implementation(libs.spring.ai.vector.store)
    
    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter")
    
    // Spring AI dependencies
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-M6")
    implementation("org.springframework.ai:spring-ai-pgvector-store-spring-boot-starter:1.0.0-M6")
    
    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.12.1")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.12.1")
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "org.example.mcp.generator.sql.RagMCPServer"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
