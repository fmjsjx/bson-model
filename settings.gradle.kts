pluginManagement {
    repositories {
        maven {
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "bson-model"
include(":bson-model-bom")
include(":bson-model-core")
include(":bson-model-generator")
