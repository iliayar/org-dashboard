plugins {
    kotlin("js")
    kotlin("plugin.serialization") version "1.4.10"
}

val ktorVersion = project.property("ktor.version") as String
val String.v: String get() = rootProject.extra["$this.version"] as String

dependencies {
    implementation(kotlin("stdlib-js"))

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")


    implementation("org.jetbrains.kotlinx:kotlinx-nodejs:0.0.7")

}

kotlin {
    js {
        browser {}
        binaries.executable()
    }
}
