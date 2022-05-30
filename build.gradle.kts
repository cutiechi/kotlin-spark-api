import java.net.URI

plugins {
    kotlin
    license
    `maven-publish`
    signing
}

group = Versions.groupID
version = Versions.project

repositories {
    mavenCentral()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn("kotlin-spark-api:dokkaJavadoc")
    archiveClassifier.set("javadoc")
    from("kotlin-spark-api/build/dokka/javadoc")
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn("classes")
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

artifacts {
    archives(tasks["jar"])
    archives(javadocJar)
    archives(sourcesJar)
}



publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(sourcesJar) {
                classifier = "sources"
            }

            artifact(javadocJar) {
                classifier = "javadoc"
            }

            pom {
                groupId = Versions.groupID
                artifactId = "kotlin-spark-api-parent"
                version = Versions.project

                from(components["kotlin"])

                name.set("Kotlin Spark API: Parent")
                description.set("Parent project for Kotlin for Apache Spark")
                packaging = "pom"

                url.set("https://maven.apache.org")
                inceptionYear.set("2019")

                organization {
                    name.set("JetBrains")
                    url.set("https://www.jetbrains.com/")
                }

                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("asm0dey")
                        name.set("Pasha Finkelshteyn")
                        email.set("asm0dey@jetbrains.com")
                        timezone.set("GMT+3")
                    }
                    developer {
                        id.set("vitaly.khudobakhshov")
                        name.set("Vitaly Khudobakhshov")
                        email.set("vitaly.khudobakhshov@jetbrains.com")
                        timezone.set("GMT+3")
                    }
                    developer {
                        id.set("Jolanrensen")
                        name.set("Jolan Rensen")
                        email.set("jolan.rensen@jetbrains.com")
                        timezone.set("GMT+1")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/JetBrains/kotlin-spark-api.git")
                    url.set("https://github.com/JetBrains/kotlin-spark-api")
                    tag.set("HEAD")
                }
            }
        }
    }
    repositories {
        maven {
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
            url = URI(if (Versions.project.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                val mavenCentralUsername: String by project
                val mavenCentralPassword: String by project
                username = mavenCentralUsername
                password = mavenCentralPassword
            }
        }
    }
}

val isReleaseVersion = !Versions.project.endsWith("SNAPSHOT")

tasks.withType<Sign> {
    onlyIf {
        isReleaseVersion && gradle.taskGraph.hasTask("publish")
    }
}

signing {
    setRequired { isReleaseVersion && gradle.taskGraph.hasTask("publish") }
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}


