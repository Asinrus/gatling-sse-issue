plugins {
    id("java")
    id("io.freefair.lombok") version("8.10.2")
    id("org.springframework.boot") version ("3.3.4")
    id("com.avast.gradle.docker-compose") version ("0.17.11")
    id("com.google.cloud.tools.jib") version "3.4.4"
    id("io.gatling.gradle") version ("3.13.1")
}

group = "example.gatling.sse"
version = "0.0.1"

repositories {
    mavenCentral()
}
val springBootVersion = "3.3.4"
val springCloudVersion="2023.0.3"
val gatlingVersion="3.13.1"

dependencies {

    implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"))
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("io.netty:netty-resolver-dns-native-macos") { artifact { classifier = "osx-aarch_64" } }
    implementation("io.projectreactor.netty:reactor-netty-core")
    implementation("io.projectreactor.netty:reactor-netty-http")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

jib {
    from {
        image = "openjdk:21-slim-bullseye"
    }
    to { image = "local/issue-gatling-sse:${version}" }
    container {
        mainClass = "example.gatling.sse.Application"
        ports = listOf("8080")
        user = "1001:100"
    }
}

dockerCompose {
    createNested("test").apply {
        useComposeFiles = listOf("compose/docker-compose.yml")
        environment = mapOf("TAG" to version)
    }
}

tasks.named("testComposeBuild").configure { dependsOn("jibDockerBuild") }