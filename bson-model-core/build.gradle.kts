plugins {
    id("bson-model.java-library-conventions")
    id("bson-model.publish-conventions")
}

java {
    registerFeature("mongodbSupport") {
        usingSourceSet(sourceSets["main"])
    }
}

dependencies {

    implementation("org.slf4j:slf4j-api")

    api("com.github.fmjsjx:libcommon-util")
    api("com.github.fmjsjx:libcommon-json-jackson2")
    api("com.github.fmjsjx:libcommon-json-jsoniter")

    api("org.mongodb:bson")
    api("org.mongodb:mongodb-driver-core")
    "mongodbSupportApi"("org.mongodb:mongodb-driver-sync")
    "mongodbSupportApi"("org.mongodb:mongodb-driver-reactivestreams")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl")

}

description = "bson-model/Core"

tasks.test {
    // Use junit platform for unit tests.
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("bson-model/Core")
                description.set("An ORM like object model framework for BSON/MongoDB.")
                url.set("https://github.com/fmjsjx/bson-model")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("fmjsjx")
                        name.set("MJ Fang")
                        email.set("fmjsjx@163.com")
                        url.set("https://github.com/fmjsjx")
                        organization.set("fmjsjx")
                        organizationUrl.set("https://github.com/fmjsjx")
                    }
                }
                scm {
                    url.set("https://github.com/fmjsjx/bson-model")
                    connection.set("scm:git:https://github.com/fmjsjx/bson-model.git")
                    developerConnection.set("scm:git:https://github.com/fmjsjx/bson-model.git")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
