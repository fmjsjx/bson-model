# bson-model

一个基于`BSON/MongoDB`的数据对象模型框架，可快速接入MongoDB Java Driver。

> 从2.0开始，最低JDK版本提升为17。

## 特性

- 提供了多种数据类型的映射方式
- 提供了对象模型与BSON相互转换的API，并支持自动生成局部更新表达式
- 使用`Jackson2`依赖库实现对象模型与`JsonNode`相互转换
- 提供Java代码生成器，通过配置可快速生成数据对象模型代码

## 添加依赖

每个release版本都将发布至[Maven中央仓库](https://repo1.maven.org/maven2/)

### 添加Maven依赖

`pom.xml`
```xml
<pom>
  <dependencyManagement>
    <dependencies>
      <!-- 版本控制 -->
      <dependency>
        <groupId>com.github.fmjsjx</groupId>
        <artifactId>bson-model-bom</artifactId>
        <version>2.0.0-RC3</version>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
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
  </dependencies>
</pom>
```

### 添加Gradle依赖

#### Groovy DSL
```groovy
repositories {
    mavenCentral
}

dependencies {
    // 版本控制
    implementation platform('com.github.fmjsjx:bson-model-bom:2.0.0-RC3')
    // 核心库
    implementation 'com.github.fmjsjx:bson-model-core'
    // 代码生成器
    compileOnly 'com.github.fmjsjx:bson-model-generator'
}
```
#### Kotlin DSL
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    // 版本控制
    implementation(platform("com.github.fmjsjx:bson-model-bom:2.0.0-RC3"))
    // 核心库
    implementation("com.github.fmjsjx:bson-model-core")
    // 代码生成器
    compileOnly("com.github.fmjsjx:bson-model-generator")
}
```

