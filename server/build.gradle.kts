plugins {
    id("java")
    kotlin("jvm")
    id("application")
    id("distribution")
    kotlin("plugin.serialization") version "1.3.61"
}

val ktorVersion = project.property("ktor.version") as String
val logbackVersion = project.property("logback.version") as String

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    // implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.9.0")
    
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

tasks.withType<Copy>().named("processResources") {
    from(project(":client").tasks.named("browserDistribution"))
}
