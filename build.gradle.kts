plugins {
    kotlin("jvm") version "1.4.20"
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.gradle.signing")
    apply(plugin = "org.gradle.java")

    group = "dev.mzarnowski.systems"

    repositories {
        mavenCentral()
    }

    sourceSets.create("jmh") {
        java.setSrcDirs(listOf("src/jmh/java"))
    }

    dependencies {
        testImplementation("org.assertj:assertj-core:3.12.2")

        testImplementation("org.mockito:mockito-core:3.5.13")
        testImplementation("org.mockito:mockito-junit-jupiter:3.5.13")

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")

        "jmhImplementation"(project)
        "jmhImplementation"("org.openjdk.jmh:jmh-core:1.21")
        "jmhAnnotationProcessor"("org.openjdk.jmh:jmh-generator-annprocess:1.21")
    }

    java {
        withSourcesJar()
        withJavadocJar()

        sourceCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                pom {
                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    scm {
                        url.set("https://github.com/mzarnowski/microlibs")
                    }

                    developers {
                        developer {
                            id.set("mzarnowski")
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                name = "Sonatype"

                val releaseRepo = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotRepo = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (isSnapshot()) snapshotRepo else releaseRepo

                credentials {
                    username = "" // FIXME don't commit credentials
                    password = "" // FIXME don't commit credentials
                }
            }
        }
    }

    signing {
        sign(publishing.publications["maven"])
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<Sign>().configureEach {
        onlyIf { !isSnapshot() }
    }

    tasks.register("jmh", type = JavaExec::class) {
        dependsOn("jmhClasses")
        main = "org.openjdk.jmh.Main"
        classpath = sourceSets["jmh"].runtimeClasspath
    }
}

fun isSnapshot() = version.toString().endsWith("SNAPSHOT")