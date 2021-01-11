import com.jfrog.bintray.gradle.BintrayExtension.*
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*
import org.gradle.api.artifacts.Dependency as GradleDependency

plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("kapt") version "1.4.21"
    `java-library`
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.5"
}

group = "pl.rzrz"
version = "1.0.8"
description = "Autogenerated test factories for Kotlin"

java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    jcenter()
}

val annotations: SourceSet by sourceSets.creating {
    java.srcDir(file("src/annotations/kotlin"))
}

val coreDependencies: Configuration by configurations.creating
val core: SourceSet by sourceSets.creating {
    java.srcDir(file("src/core/kotlin"))
    compileClasspath += coreDependencies
}

val generatorDependencies: Configuration by configurations.creating
val generator: SourceSet by sourceSets.creating {
    java.srcDir(file("src/generator/kotlin"))
    resources.srcDir(file("src/generator/resources"))
    compileClasspath += core.compileClasspath + core.output + generatorDependencies
    runtimeClasspath += compileClasspath
}

dependencies {
    coreDependencies("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    coreDependencies("org.jetbrains.kotlin:kotlin-reflect")

    generatorDependencies("com.squareup:kotlinpoet:1.7.2")
    generatorDependencies(annotations.output)
    generatorDependencies(core.output)

    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.18.1")
    testImplementation(core.compileClasspath + core.output)
    testImplementation(generator.compileClasspath + generator.output)

    kaptTest(generator.compileClasspath + generator.output)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging.events(STARTED, FAILED, PASSED, SKIPPED, STANDARD_OUT)
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        jvmTarget = "1.8"
    }
}

val annotationsJar: Task by tasks.creating(Jar::class) {
    archiveFileName.set("kotlin-test-factories-annotations.jar")
    from(annotations.output)
}

val annotationsSourcesJar: Task by tasks.creating(Jar::class) {
    archiveFileName.set("kotlin-test-factories-annotations-sources.jar")
    archiveClassifier.set("sources")
    from(annotations.allSource)
}

val coreJar: Task by tasks.creating(Jar::class) {
    archiveFileName.set("kotlin-test-factories-core.jar")
    from(core.output)
}

val coreSourcesJar: Task by tasks.creating(Jar::class) {
    archiveFileName.set("kotlin-test-factories-core-sources.jar")
    archiveClassifier.set("sources")
    from(core.allSource)
}

val generatorJar: Task by tasks.creating(Jar::class) {
    archiveFileName.set("kotlin-test-factories-generator.jar")
    from(generator.output)
}

val generatorSourcesJar: Task by tasks.creating(Jar::class) {
    archiveFileName.set("kotlin-test-factories-generator-sources.jar")
    archiveClassifier.set("sources")
    from(generator.allSource)
}

tasks.jar.configure {
    enabled = false
}

tasks.build.configure {
    finalizedBy(annotationsJar, annotationsSourcesJar, coreJar, coreSourcesJar, generatorJar, generatorSourcesJar)
}

val githubPath = "jacek-rzrz/kotlin-test-factories"
val githubUrl = "https://github.com/$githubPath"

val publicationName: String by project

fun selectJar() = when (publicationName) {
    "annotations" -> annotationsJar
    "core" -> coreJar
    "generator" -> generatorJar
    else -> null
}

fun selectSourcesJar() = when (publicationName) {
    "annotations" -> annotationsSourcesJar
    "core" -> coreSourcesJar
    "generator" -> generatorSourcesJar
    else -> null
}

fun Configuration.externalDependencies(): Collection<Dependency> {
    return allDependencies.mapNotNull { Dependency.from(it) }
}

fun selectDependencies() = when(publicationName) {
    "core" -> coreDependencies.externalDependencies()
    "generator" -> generatorDependencies.externalDependencies() + listOf(
            Dependency(group.toString(), "kotlin-test-factories-annotations", version.toString()),
            Dependency(group.toString(), "kotlin-test-factories-core", version.toString())
    )
    else -> emptyList()
}

data class Dependency(val group: String, val artifactId: String, val version: String) {
    companion object {
        fun from(dependency: GradleDependency): Dependency? {
            val group = dependency.group ?: return null
            val version = dependency.version ?: return null
            return Dependency(
                    group,
                    dependency.name,
                    version
            )
        }

    }
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>(publicationName) {
            groupId = project.group.toString()
            artifactId = "kotlin-test-factories-$publicationName"
            version = project.version.toString()

            pom {
                name.set(artifactId)
                description.set(project.description)
                url.set(githubUrl)

                withXml {
                    asNode().appendNode("dependencies").apply {
                        selectDependencies().forEach {
                            appendNode("dependency").apply {
                                appendNode("groupId", it.group)
                                appendNode("artifactId", it.artifactId)
                                appendNode("version", it.version)
                            }
                        }
                    }
                }

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        name.set("Jacek Rzrz")
                        email.set("jacek@rzrz.pl")
                    }
                }

                scm {
                    url.set("$githubUrl/tree/master")
                    connection.set("scm:git:git://github.com/$githubPath.git")
                    developerConnection.set("scm:git:ssh://github.com:$githubPath.git")
                }
            }

            selectJar()?.let { artifact(it) }
            selectSourcesJar()?.let {
                artifact(mapOf(
                        "source" to it,
                        "classifier" to "sources",
                        "extension" to "jar"
                ))
            }
        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    setPublications(publicationName)
    dryRun = false
    publish = true

    pkg(closureOf<PackageConfig> {
        repo = "maven-public"
        name = "kotlin-test-factories-$publicationName"
        setLicenses("Apache-2.0")
        vcsUrl = githubUrl
        issueTrackerUrl = "$githubUrl/issues"
        publicDownloadNumbers = true
        githubRepo = githubPath
        version(closureOf<VersionConfig> {
            name = project.version.toString()
            desc = project.description
            released = Date().toString()
            gpg(closureOf<GpgConfig> {
                sign = true
            })
            mavenCentralSync(closureOf<MavenCentralSyncConfig> {
                sync = true
                user = System.getenv("OSS_SONATYPE_USER")
                password = System.getenv("OSS_SONATYPE_PASSWORD")
                close = "1"
            })
        })
    })
}

tasks.create("printVersion") {
    doLast {
        println(project.version)
    }
}