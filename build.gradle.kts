import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    `java-library`
    checkstyle
    alias(libs.plugins.vanniktech.maven.publish)
}

group = "io.taskmigo"
version = "0.1.1"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
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

mavenPublishing {
    publishToMavenCentral()
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }

    coordinates(group.toString(), "checkstyle", version.toString())

    pom {
        name.set("Taskmigo Checkstyle")
        description.set("Taskmigo-specific Checkstyle extensions")
        inceptionYear.set("2026")
        url.set("https://github.com/taskmigo/checkstyle")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("taskmigo")
                name.set("Taskmigo")
                url.set("https://github.com/taskmigo")
            }
        }

        scm {
            url.set("https://github.com/taskmigo/checkstyle")
            connection.set("scm:git:git://github.com/taskmigo/checkstyle.git")
            developerConnection.set("scm:git:ssh://git@github.com/taskmigo/checkstyle.git")
        }
    }
}
