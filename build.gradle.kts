import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

val versionNumber = "0.2.1"
val isSnapshot = true
val kotlinVersion = "1.4.0-rc"

group = "dev.reactant"
version = "$versionNumber${if (isSnapshot) "-SNAPSHOT" else ""}"

plugins {
    java
    kotlin("jvm") version "1.4.0-rc"
    `maven-publish`
    signing
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("org.jetbrains.dokka") version "0.10.0"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    val compileKotlin: KotlinCompile by tasks
    compileKotlin.kotlinOptions {
        jvmTarget = "1.8"
        languageVersion = "1.4"
        freeCompilerArgs = listOf("-Xjvm-default=compatibility")
    }

    val compileTestKotlin: KotlinCompile by tasks
    compileTestKotlin.kotlinOptions {
        jvmTarget = "1.8"
    }

    repositories {
        jcenter()
        mavenCentral()
        maven { url = URI.create("https://hub.spigotmc.org/nexus/content/repositories/snapshots") }
        maven { url = URI.create("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = URI.create("https://oss.sonatype.org/content/repositories/releases/") }
        maven { url = URI.create("https://repo.codemc.org/repository/maven-public") }
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }

    dependencies {
        listOf(
                "stdlib-jdk8",
                "reflect"
//            "script-util",
//            "script-runtime",
//            "compiler-embeddable",
//            "scripting-compiler"
        ).forEach { api(kotlin(it, kotlinVersion)) }

        implementation("org.bstats:bstats-bukkit:1.7") {
            isTransitive = false
        }

        api("io.reactivex.rxjava3:rxjava:3.0.4")
        api("io.reactivex.rxjava3:rxkotlin:3.0.0")
        api("org.reflections:reflections:0.9.12")

        api("com.google.code.gson:gson:2.8.6")
        api("org.yaml:snakeyaml:1.26")
        api("com.moandjiezana.toml:toml4j:0.7.2")

        api("info.picocli:picocli:4.3.2")
        api("org.mariadb.jdbc:mariadb-java-client:2.5.1")

        api("org.apache.logging.log4j:log4j-core:2.12.1")

        api("com.squareup.retrofit2:retrofit:2.9.0")
        api("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
        api("com.squareup.retrofit2:converter-gson:2.9.0")

        api("net.sourceforge.cssparser:cssparser:0.9.27")

        api("javassist:javassist:3.12.1.GA")

        compileOnly("org.spigotmc:spigot-api:1.15.2-R0.1-SNAPSHOT")
        implementation(kotlin("stdlib-jdk8"))

    }
}

dependencies {
    api(project(":reactant-rui"))
}

val dokka = (tasks["dokka"] as DokkaTask).apply {
    outputFormat = "html"
}

val dokkaJavadoc by tasks.registering(DokkaTask::class) {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
}

gradle.taskGraph.whenReady {
    if (allTasks.any { it is Sign }) {
        allprojects {
            extra["signing.keyId"] = System.getenv("SIGNING_KEYID")
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaJavadoc)
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

val shadowJar = (tasks["shadowJar"] as ShadowJar).apply {
    relocate("org.bstats", "dev.reactant.reactant.core")
    relocate("okhttp3", "dev.reactant.reactant.okhttp3")
    relocate("okio", "dev.reactant.reactant.okio")
    relocate("cssparser", "dev.reactant.reactant.cssparser")
    relocate("javassist", "dev.reactant.reactant.javassist")
}

val dokkaJar by tasks.registering(Jar::class) {
    dependsOn(dokka)
    archiveClassifier.set("dokka")
    from(tasks.dokka)
}

val deployPlugin by tasks.registering(Copy::class) {
    dependsOn(shadowJar)
    System.getenv("PLUGIN_DEPLOY_PATH")?.let {
        from(shadowJar)
        into(it)
    }
}

val build = (tasks["build"] as Task).apply {
    arrayOf(
            sourcesJar,
            shadowJar,
            deployPlugin
    ).forEach { dependsOn(it) }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
            artifact(dokkaJar.get())
            artifact(shadowJar)

            groupId = group.toString()
            artifactId = project.name
            version = version

            pom {
                name.set("Reactant")
                description.set("An elegant plugin framework for spigot")
                url.set("https://reactant.dev")
                licenses {
                    license {
                        name.set("GPL-3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:git@gitlab.com:reactant/reactant.git")
                    url.set("https://gitlab.com/reactant/reactant/")
                }

                developers {
                    developer {
                        id.set("setako")
                        name.set("Setako")
                        organization.set("Reactant Dev Team")
                        organizationUrl.set("https://gitlab.com/reactant")
                    }
                }

            }
        }
    }

    repositories {
        maven {

            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }


        }
    }
}

signing {
    ext["signing.keyId"] = findProperty("signingKeyId") as String?
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey?.replace("\\n", "\n"), signingPassword)
    sign(publishing.publications["maven"])
}
