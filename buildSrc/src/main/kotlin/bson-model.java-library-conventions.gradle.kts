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
    api(platform("io.netty:netty-bom:4.1.70.Final"))
    // libcommon-bom
    api(platform("com.github.fmjsjx:libcommon-bom:2.5.3"))
    // junit-bom
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    // jackson2-bom
    api(platform("com.fasterxml.jackson:jackson-bom:2.13.0"))

    constraints {
        implementation("org.slf4j:slf4j-api:1.7.32")
        implementation("ch.qos.logback:logback-classic:1.2.6")
        api("com.dslplatform:dsl-json-java8:1.9.9")
        api("com.jsoniter:jsoniter:0.9.23")
        api("org.mongodb:bson:4.4.0")
        api("org.mongodb:mongodb-driver-core:4.4.0")
        api("org.mongodb:mongodb-driver-sync:4.4.0")
        api("org.mongodb:mongodb-driver-reactivestreams:4.4.0")
        api("org.mongodb:mongodb-driver-legacy:4.4.0")
        implementation("org.jruby:jruby-complete:9.3.1.0")
        implementation("org.jruby:jruby:9.3.1.0")
        implementation("org.jruby:jruby-core:9.3.1.0")
        implementation("org.jruby:jruby-stdlib:9.3.1.0")
        implementation("org.yaml:snakeyaml:1.29")
    }
    // log4j2
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.15.0"))

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
