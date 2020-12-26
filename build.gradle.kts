import dev.clojurephant.plugin.clojure.ClojureBuild

plugins {
    id("fabric-loom") version "0.4-SNAPSHOT"
    id("dev.clojurephant.clojure") version "0.6.0-alpha.4"
}

repositories {
    mavenCentral()
    maven {
        name = "GFH"
        url = uri("https://raw.githubusercontent.com/Devan-Kerman/Devan-Repo/master/")
    }
    maven {
        name = "Clojars"
        url = uri("https://repo.clojars.org")
    }
}

sourceSets {
    main {
        clojure
    }
}

dependencies {
    implementation("org.clojure:clojure:1.10.1")
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modImplementation("net.devtech:grossfabrichacks:6.1")
}

clojure {
    builds {
        val main = maybeCreate("main") as ClojureBuild
        main.aotAll()
    }
}

group = "eutros"
version = "0.0.0"
