plugins {
    id("com.avast.gradle.docker-compose") version "0.17.12"
}

dockerCompose {
    useComposeFiles.add("postgres.yml")
}