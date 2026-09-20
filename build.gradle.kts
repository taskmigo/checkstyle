import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.SourceSetContainer

plugins {
    `java-library`
    checkstyle
    `maven-publish`
}

group = "io.taskmigo"
version = providers.gradleProperty("version").orElse("0.1.0").get()

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    compileOnly(libs.checkstyle)
    compileOnly(libs.jspecify)

    testImplementation(libs.checkstyle)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Werror")
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).addBooleanOption("Werror", true)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

extensions.configure<CheckstyleExtension> {
    toolVersion = libs.versions.checkstyle.get()
    configFile = layout.projectDirectory.file("src/main/resources/checkstyle.xml").asFile
}

val sourceSets = extensions.getByType<SourceSetContainer>()

tasks.withType<Checkstyle>().configureEach {
    dependsOn(tasks.named("classes"))

    // Self-host the extension without adding this project as a dependency on itself.
    checkstyleClasspath = configurations.getByName("checkstyle") + sourceSets.named("main").get().output

    reports {
        xml.required.set(false)
        html.required.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "checkstyle"

            pom {
                name.set("Taskmigo Checkstyle")
                description.set("Taskmigo-specific Checkstyle extensions")
                url.set("https://github.com/taskmigo/checkstyle")
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/taskmigo/checkstyle")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR").orNull
                password = providers.environmentVariable("GITHUB_TOKEN").orNull
            }
        }
    }
}
