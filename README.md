# bson-model
An ORM like object model framework for BSON/MongoDB.

## 添加依赖
每个release版本都将发布至[Maven中央仓库](https://repo1.maven.org/maven2/)
### 添加Maven依赖
`pom.xml`
```xml
...
  <dependencyManagement>
    <dependencies>
      ...
      <!-- 版本控制 -->
      <dependency>
        <groupId>com.github.fmjsjx</groupId>
        <artifactId>bson-model-bom</artifactId>
        <version>1.4.4</version>
      </dependency>
      ...
    </dependencies>
  </dependencyManagement>
...
  <dependencies>
    ...
    <!-- 核心库 -->
    <dependency>
      <groupId>com.github.fmjsjx</groupId>
      <artifactId>bson-model-core</artifactId>
    </dependency>
    <!-- 代码生成器 -->
    <dependency>
      <groupId>com.github.fmjsjx</groupId>
      <artifactId>bson-model-generator</artifactId>
      <scope>provided</scope>
      <optional>true</optional>
    </dependency>
    ...
  </dependencies>
...
```

### 添加Gradle依赖

#### Groovy DSL
```groovy
...
repositories {
    mavenCentral
}

dependencies {
    // 版本控制
    implementation platform('com.github.fmjsjx:bson-model-bom:1.4.4')
    // 核心库
    implementation 'com.github.fmjsjx:bson-model-core'
    // 代码生成器
    compileOnly 'com.github.fmjsjx:bson-model-generator'
    ...
}
...
```
#### Kotlin DSL
```kotlin
...
repositories {
    mavenCentral()
}

dependencies {
    // 版本控制
    implementation(platform("com.github.fmjsjx:bson-model-bom:1.4.4"))
    // 核心库
    implementation("com.github.fmjsjx:bson-model-core")
    // 代码生成器
    compileOnly("com.github.fmjsjx:bson-model-generator")
    ...
}
...
```

