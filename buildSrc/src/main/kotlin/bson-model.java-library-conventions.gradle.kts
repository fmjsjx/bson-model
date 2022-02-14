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
    // netty-bom
    api(platform("io.netty:netty-bom:4.1.74.Final"))
    // libcommon-bom
    api(platform("com.github.fmjsjx:libcommon-bom:2.6.2"))
    // junit-bom
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    // jackson2-bom
    api(platform("com.fasterxml.jackson:jackson-bom:2.13.1"))

    constraints {
        implementation("org.slf4j:slf4j-api:1.7.36")
        implementation("ch.qos.logback:logback-classic:1.2.10")
        api("com.jsoniter:jsoniter:0.9.23")
        api("org.mongodb:bson:4.5.0")
        api("org.mongodb:mongodb-driver-core:4.5.0")
        api("org.mongodb:mongodb-driver-sync:4.5.0")
        api("org.mongodb:mongodb-driver-reactivestreams:4.5.0")
        api("org.mongodb:mongodb-driver-legacy:4.5.0")
        implementation("org.jruby:jruby-complete:9.3.3.0")
        implementation("org.jruby:jruby:9.3.3.0")
        implementation("org.jruby:jruby-core:9.3.3.0")
        implementation("org.jruby:jruby-stdlib:9.3.3.0")
        implementation("org.yaml:snakeyaml:1.30")
    }
    // log4j2
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.17.1"))

}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release.set(11)
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
