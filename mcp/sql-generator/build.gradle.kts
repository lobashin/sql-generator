plugins {
    application
    id("org.springframework.boot") version "3.4.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.guava)
    implementation(libs.spring.ai.gigachat)
    implementation(libs.spring.ai.vector.store)
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.5.0")
    // implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-M6")
    // implementation("org.springframework.ai:spring-ai-pgvector-store-spring-boot-starter:1.0.0-M6")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.1")
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
