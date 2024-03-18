plugins {
    id("java")
}

group = "dev.oakleycord"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.21") {
        exclude("opus-java")
    }
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")

}

tasks.test {
    useJUnitPlatform()
}