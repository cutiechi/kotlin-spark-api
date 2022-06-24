import java.net.URI

plugins {
    scala
    kotlin
    dokka
//    license
//    `maven-publish`
//    signing
}

group = Versions.groupID
version = Versions.project

repositories {
    mavenCentral()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    with(Dependencies) {
        implementation(
            kotlinStdLib,
            scalaLibrary,
        )
        testImplementation(
            kotest,
            atrium,
            kotlinTest,
        )
    }
}

//val javadocJar by tasks.registering(Jar::class) {
//    dependsOn("dokkaJavadoc")
//    archiveClassifier.set("javadoc")
//    from("build/dokka/javadoc")
//}
//
//val sourcesJar by tasks.registering(Jar::class) {
//    dependsOn(tasks.classes)
//    archiveClassifier.set("sources")
//    from(sourceSets.main.get().allSource)
//}
//
//artifacts {
//    archives(tasks.jar)
////    archives(javadocJar)
//    archives(sourcesJar)
//}
//
//publishing {
//    publications {
//        register<MavenPublication>("gpr") {
//            from(components["java"])
//        }
//        create<MavenPublication>("mavenJava") {
//            artifact(sourcesJar) {
//                classifier = "sources"
//            }
//
////            artifact(javadocJar) {
////                classifier = "javadoc"
////            }
//
//            pom.withXml {
//                val pomNode = asNode()
//
//                val dependencyNodes: groovy.util.NodeList = pomNode.get("dependencies") as groovy.util.NodeList
//                dependencyNodes.forEach {
//                    (it as groovy.util.Node).parent().remove(it)
//                }
//
//                pom {
//                    groupId = Versions.groupID
////                    artifactId = "kotlin-spark-api_${Versions.scalaCompat}_${Versions.spark}"
//                    artifactId = "scala-tuples-in-kotlin"
//                    version = Versions.project
//
//                    from(components["kotlin"])
//
//                    name.set("Kotlin Spark API")
//                    description.set("Kotlin for Apache Spark")
//                    packaging = "pom"
//
//                    url.set("https://maven.apache.org")
//                    inceptionYear.set("2019")
//
//                    organization {
//                        name.set("JetBrains")
//                        url.set("https://www.jetbrains.com/")
//                    }
//
//                    licenses {
//                        license {
//                            name.set("Apache License, Version 2.0")
//                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
//                        }
//                    }
//
//                    developers {
//                        developer {
//                            id.set("asm0dey")
//                            name.set("Pasha Finkelshteyn")
//                            email.set("asm0dey@jetbrains.com")
//                            timezone.set("GMT+3")
//                        }
//                        developer {
//                            id.set("vitaly.khudobakhshov")
//                            name.set("Vitaly Khudobakhshov")
//                            email.set("vitaly.khudobakhshov@jetbrains.com")
//                            timezone.set("GMT+3")
//                        }
//                        developer {
//                            id.set("Jolanrensen")
//                            name.set("Jolan Rensen")
//                            email.set("jolan.rensen@jetbrains.com")
//                            timezone.set("GMT+1")
//                        }
//                    }
//
//                    scm {
//                        connection.set("scm:git:https://github.com/JetBrains/kotlin-spark-api.git")
//                        url.set("https://github.com/JetBrains/kotlin-spark-api")
//                        tag.set("HEAD")
//                    }
//                }
//            }
//        }
//        repositories {
//            maven {
//                name = "GitHubPackages"
//                url = uri("https://github.com/Kotlin/kotlin-spark-api")
//                credentials {
//                    username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
//                    password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
//                }
//            }
//
//            maven {
//                name = "MavenCentral"
//                val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
//                val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
//                url = URI(if (Versions.project.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
//                credentials {
//                    val mavenCentralUsername: String by project
//                    val mavenCentralPassword: String by project
//                    username = mavenCentralUsername
//                    password = mavenCentralPassword
//                }
//            }
//        }
//    }
//}
//
//val isReleaseVersion = !Versions.project.endsWith("SNAPSHOT")
//
//tasks.withType<Sign> {
//    onlyIf {
//        isReleaseVersion && gradle.taskGraph.hasTask("publish")
//    }
//}
//
//signing {
//    setRequired { isReleaseVersion && gradle.taskGraph.hasTask("publish") }
//    useGpgCmd()
//    sign(publishing.publications["mavenJava"])
//}