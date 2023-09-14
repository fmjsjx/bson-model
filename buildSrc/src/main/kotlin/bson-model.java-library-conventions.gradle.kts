plugins {
    `java-library`
}

repositories {
    maven {
        url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    }
    mavenCentral()
}

dependencies {
    // libcommon-bom
    api(platform("com.github.fmjsjx:libcommon-bom:3.6.0"))
    // junit-bom
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    // jackson2-bom
    api(platform("com.fasterxml.jackson:jackson-bom:2.15.2"))

    constraints {
        implementation("org.slf4j:slf4j-api:2.0.7")
        implementation("ch.qos.logback:logback-classic:1.4.11")
        api("com.jsoniter:jsoniter:0.9.23")
        val mongodbVersion = "4.10.2"
        api("org.mongodb:bson:$mongodbVersion")
        api("org.mongodb:mongodb-driver-core:$mongodbVersion")
        api("org.mongodb:mongodb-driver-sync:$mongodbVersion")
        api("org.mongodb:mongodb-driver-reactivestreams:$mongodbVersion")
        api("org.mongodb:mongodb-driver-legacy:$mongodbVersion")
        val jrubyVersion = "9.4.3.0"
        implementation("org.jruby:jruby-complete:$jrubyVersion")
        implementation("org.jruby:jruby:$jrubyVersion")
        implementation("org.jruby:jruby-core:$jrubyVersion")
        implementation("org.jruby:jruby-stdlib:$jrubyVersion")
        implementation("org.yaml:snakeyaml:2.0")
    }
    // log4j2
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.20.0"))

}

val javaVersion = 17

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
    options.memberLevel = JavadocMemberLevel.PUBLIC
}
