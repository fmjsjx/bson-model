pluginManagement {
    repositories {
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
    }
}

rootProject.name = "bson-model"
include(":bson-model-bom")
include(":bson-model-core")
include(":bson-model-generator")
