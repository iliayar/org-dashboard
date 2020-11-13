plugins {
    kotlin("multiplatform") version "1.4.0" apply false
}

allprojects {
    version = "0.1.1"

    repositories {
        mavenCentral()
        jcenter()
        maven("https://maven.pkg.github.com/iliayar/kotlin-org-mode") {
	    credentials {
		username = "iliayar"
		password = "e72d96114d0dd3c9d8843629a1ea1e54bdd0e9e2" // This is a READONLY token for packages, it's safe I hope
	    }
	}
	// maven("https://jitpack.io")
    }
}

tasks.register<Copy>("stage") {
    dependsOn("server:build")

    destinationDir = File("build/dist")

    from(tarTree("server/build/distributions/server-0.1.1.tar"))
}
