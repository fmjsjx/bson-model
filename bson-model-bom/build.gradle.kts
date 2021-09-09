plugins {
    `java-platform`
    id("bson-model.publish-conventions")
}

description = "bsom-model/BOM"

dependencies {
    constraints {
        api(project(":bson-model-core"))
        api(project(":bson-model-generator"))
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["javaPlatform"])
            pom {
                name.set("bson-model/BOM")
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
