import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    id("com.gradleup.shadow") version "8.3.5" apply false
}

fun Project.gitValue(vararg args: String): String {
    return try {
        val process = ProcessBuilder(listOf("git", *args))
            .directory(rootDir)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        output.ifBlank { "unknown" }
    } catch (_: Exception) {
        "unknown"
    }
}

allprojects {
    group = providers.gradleProperty("group").get()
    version = providers.gradleProperty("version").get()

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            metadataSources {
                mavenPom()
                artifact()
            }
        }
        maven("https://libraries.minecraft.net/")
        maven("https://jitpack.io/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://maven.playpro.com/")
        maven("https://raw.githubusercontent.com/FabioZumbi12/RedProtect/mvn-repo/")
        maven("https://ci.ender.zone/plugin/repository/everything/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.panda-lang.org/releases")
        maven("https://www.iani.de/nexus/content/repositories/snapshots/")
        maven("https://repo.william278.net/snapshots/")
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "jacoco")

    extensions.configure(JavaPluginExtension::class.java) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        withSourcesJar()
        withJavadocJar()
    }

    configurations.matching { it.name.endsWith("CompileClasspath") }.configureEach {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21)
    }

    dependencies {
        add("compileOnly", "io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
        add("compileOnly", "com.google.code.findbugs:jsr305:3.0.2")
        add("testCompileOnly", "io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
        add("testCompileOnly", "com.google.code.findbugs:jsr305:3.0.2")
        add("testRuntimeOnly", "io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
        add("testRuntimeOnly", "com.google.code.findbugs:jsr305:3.0.2")
        add("testRuntimeOnly", "io.papermc:paperlib:1.0.7")
        add("testImplementation", platform("org.junit:junit-bom:5.10.2"))
        add("testImplementation", "org.junit.jupiter:junit-jupiter")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
        add("testImplementation", "org.mockito:mockito-core:4.11.0")
        add("testImplementation", "org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.100.0") {
            exclude(group = "org.jetbrains", module = "annotations")
        }
        add("implementation", "org.apache.commons:commons-lang3:3.20.0")
    }

    tasks.withType(JavaCompile::class.java).configureEach {
        options.release.set(21)
        options.isDeprecation = true
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
    }

    tasks.withType(Test::class.java).configureEach {
        useJUnitPlatform()
    }

    tasks.withType(JacocoReport::class.java).configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

project(":dough-common") {
    dependencies {
        add("api", "io.papermc:paperlib:1.0.7")
    }

    tasks.named("processResources", ProcessResources::class.java) {
        val values = mapOf(
            "project" to mapOf(
                "version" to project.version.toString()
            ),
            "git" to mapOf(
                "branch" to gitValue("rev-parse", "--abbrev-ref", "HEAD"),
                "commit" to mapOf(
                    "id" to mapOf(
                        "abbrev" to gitValue("rev-parse", "--short", "HEAD"),
                        "full" to gitValue("rev-parse", "HEAD")
                    )
                )
            )
        )

        filteringCharset = "UTF-8"
        filesMatching("dough.properties") {
            expand(values)
        }
    }
}

project(":dough-reflection") {
    dependencies {
        add("compileOnly", project(":dough-common"))
        add("testImplementation", project(":dough-common"))
    }
}
project(":dough-config") { dependencies { add("compileOnly", project(":dough-common")) } }
project(":dough-chat") { dependencies { add("compileOnly", project(":dough-common")) } }
project(":dough-data") { dependencies { add("compileOnly", project(":dough-common")) } }

project(":dough-skins") {
    dependencies {
        add("compileOnly", project(":dough-common"))
        add("compileOnly", project(":dough-reflection"))
        add("compileOnly", "com.mojang:authlib:6.0.52")
    }

    tasks.withType(Test::class.java).configureEach {
        failOnNoDiscoveredTests = false
    }
}

project(":dough-items") {
    dependencies {
        add("compileOnly", project(":dough-common"))
        add("compileOnly", project(":dough-reflection"))
    }
}

project(":dough-inventories") {
    dependencies {
        add("implementation", project(":dough-common"))
        add("implementation", project(":dough-reflection"))
        add("implementation", project(":dough-items"))
    }
}

project(":dough-recipes") { dependencies { add("compileOnly", project(":dough-common")) } }
project(":dough-updater") { dependencies { add("compileOnly", project(":dough-common")) } }
project(":dough-scheduling") { dependencies { add("compileOnly", project(":dough-common")) } }

project(":dough-protection") {
    val plotSquaredVersion = "7.5.4"

    dependencies {
        add("compileOnly", project(":dough-common"))

        add("compileOnly", "com.sk89q.worldedit:worldedit-core:7.2.17") { exclude(module = "bukkit") }
        add("compileOnly", "com.sk89q.worldedit:worldedit-bukkit:7.2.17") { exclude(module = "bukkit") }
        add("compileOnly", "com.sk89q.worldguard:worldguard-bukkit:7.0.9") { exclude(module = "bukkit") }
        add("compileOnly", "com.github.elBukkit:PreciousStones:1.17.2") { exclude(module = "bukkit") }
        add("compileOnly", "net.coreprotect:coreprotect:21.3") { exclude(module = "bukkit") }
        add("compileOnly", "de.diddiz:logblock:1.17.0.0-SNAPSHOT") { exclude(module = "bukkit") }
        add("compileOnly", "com.github.marcelo-mason:SimpleClans:7c3db52796") { exclude(module = "bukkit") }
        add("compileOnly", "com.github.GriefPrevention:GriefPrevention:16.18.2") { exclude(module = "bukkit") }
        add("compileOnly", "com.github.dmulloy2.LWC:lwc:master-SNAPSHOT") { exclude(module = "bukkit") }
        add("compileOnly", "me.lucko:helper:5.6.14") { exclude(module = "bukkit") }
        add("compileOnly", "com.massivecraft:Factions:1.6.9.5-4.1.4-STABLE") { exclude(module = "bukkit") }
        add("compileOnly", "com.github.LlmDl:Towny:1b86d017c5") { exclude(module = "bukkit") }
        add("compileOnly", "com.github.fubira:Lockette:9dac96e8f8") { exclude(module = "bukkit") }

        add("compileOnly", "com.intellectualsites.plotsquared:plotsquared-core:$plotSquaredVersion") {
            exclude(group = "org.projectlombok", module = "lombok")
            exclude(module = "bukkit")
        }
        add("compileOnly", "com.intellectualsites.plotsquared:plotsquared-bukkit:$plotSquaredVersion") {
            exclude(module = "plotsquared-core")
            exclude(module = "bukkit")
        }

        add("compileOnly", "br.net.fabiozumbi12.RedProtect:RedProtect-Core:7.7.3") {
            isTransitive = false
            exclude(module = "bukkit")
        }
        add("compileOnly", "br.net.fabiozumbi12.RedProtect:RedProtect-Spigot:7.7.3") {
            isTransitive = false
            exclude(module = "bukkit")
        }
        add("compileOnly", "world.bentobox:bentobox:1.20.1-SNAPSHOT") { exclude(module = "bukkit") }
        add("compileOnly", "nl.rutgerkok:blocklocker:1.10.4") { exclude(module = "bukkit") }
        add("compileOnly", "com.github.angeschossen:LandsAPI:6.29.12") { exclude(module = "bukkit") }
        add("compileOnly", "com.github.angeschossen:ChestProtectAPI:3.9.1") { exclude(module = "bukkit") }
        add("compileOnly", "com.github.Ez4p1xEL:NoBuildPlus:1.5.16") { exclude(module = "bukkit") }

        add("compileOnly", "net.dzikoysk.funnyguilds:plugin:4.12.0") {
            exclude(module = "bukkit")
            exclude(group = "com.github.PikaMug", module = "LocaleLib")
        }

        add("compileOnly", "net.william278.husktowns:husktowns-bukkit:3.0-988161b") { exclude(module = "bukkit") }
        add("compileOnly", "net.william278.huskclaims:huskclaims-bukkit:1.0.2-e60150d") { exclude(module = "bukkit") }
        add("compileOnly", "de.epiceric:ShopChest:1.13-SNAPSHOT") { exclude(module = "bukkit") }
        add("compileOnly", "org.popcraft:bolt-common:1.0.580")
        add("compileOnly", "org.popcraft:bolt-bukkit:1.0.580") { exclude(module = "bukkit") }
        add("compileOnly", "cn.lunadeer:DominionAPI:4.6.0") { exclude(module = "bukkit") }
    }
}

project(":dough-api") {
    apply(plugin = "com.gradleup.shadow")

    dependencies {
        add("implementation", project(":dough-common"))
        add("implementation", project(":dough-reflection"))
        add("implementation", project(":dough-config"))
        add("implementation", project(":dough-chat"))
        add("implementation", project(":dough-data"))
        add("implementation", project(":dough-skins"))
        add("implementation", project(":dough-items"))
        add("implementation", project(":dough-inventories"))
        add("implementation", project(":dough-protection"))
        add("implementation", project(":dough-recipes"))
        add("implementation", project(":dough-updater"))
        add("implementation", project(":dough-scheduling"))
    }

    tasks.named("shadowJar", ShadowJar::class.java) {
        // Publish the fat jar as the primary artifact name.
        archiveClassifier.set("")
        exclude("META-INF/*")
    }

    tasks.named("jar", Jar::class.java) {
        enabled = false
    }

    tasks.named("assemble") {
        dependsOn("shadowJar")
    }
}

