plugins {
    id("bson-model.java-library-conventions")
    id("bson-model.publish-conventions")
}

java {
    registerFeature("generatorSupport") {
        usingSourceSet(sourceSets["main"])
    }
}

dependencies {
    
    api(project(":bson-model-core"))
    "generatorSupportImplementation"("org.jruby:jruby")

    testImplementation("org.mongodb:mongodb-driver-core")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl")

}

description = "bson-model/Generator"

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
                name.set("bson-model/Generator")
                description.set("A code generator for `bson-model`.")
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
