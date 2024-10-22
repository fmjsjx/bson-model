# bson-model

A data object model framework based on `BSON/MongoDB`.

> Since version 2.x, all modules are compiled based on JDK-17.

## Features

- Provides mapping ways for multiple data types.
- Provides APIs for data conversions between object model and BSON document, and supports automatic generation of part update expressions.
- Provides JSON serialization/deserialization.
- Provides a `Java code generator` that can quickly generate data object model code through YAML configuration file.

## How to use

All releases will be released to the [Maven central repository](https://repo1.maven.org/maven2/).

### Using maven

`pom.xml`
```xml
<pom>
  <dependencyManagement>
    <dependencies>
      <!-- Choose Version -->
      <dependency>
        <groupId>com.github.fmjsjx</groupId>
        <artifactId>bson-model-bom</artifactId>
        <version>2.2.0</version>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <!-- Core Library -->
    <dependency>
      <groupId>com.github.fmjsjx</groupId>
      <artifactId>bson-model-core</artifactId>
    </dependency>
    <!-- Code Generator -->
    <dependency>
      <groupId>com.github.fmjsjx</groupId>
      <artifactId>bson-model-generator</artifactId>
      <scope>provided</scope>
      <optional>true</optional>
    </dependency>
  </dependencies>
</pom>
```

### Using gradle

#### Groovy DSL
```groovy
repositories {
    mavenCentral
}

dependencies {
    // Choose Version
    implementation platform('com.github.fmjsjx:bson-model-bom:2.2.0')
    // Core Library
    implementation 'com.github.fmjsjx:bson-model-core'
    // Code Generator
    compileOnly 'com.github.fmjsjx:bson-model-generator'
}
```
#### Kotlin DSL
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    // Choose Version
    implementation(platform("com.github.fmjsjx:bson-model-bom:2.2.0"))
    // Core Library
    implementation("com.github.fmjsjx:bson-model-core")
    // Code Generator
    compileOnly("com.github.fmjsjx:bson-model-generator")
}
```

