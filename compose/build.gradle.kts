plugins {
    id("com.avast.gradle.docker-compose") version "0.17.12"
}

dockerCompose {
    useComposeFiles.add("postgres.yml")
    dockerExecutable.set(project.property("org.example.executable.docker") as String)
    executable.set(project.property("org.example.executable.docker-compose") as String)
}