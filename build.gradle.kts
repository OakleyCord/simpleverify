import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow").version("8.1.1")
}

group = "dev.oakleycord"
version = "1.0-SNAPSHOT"


tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "dev.oakleycord.simpleverify.Main"))
        }
    }
}
tasks {
    build {
        dependsOn(shadowJar)
    }
}


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
