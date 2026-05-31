# JBang Directives — Deep Dive

This reference covers all JBang directives in detail, including advanced usage patterns.

## Directive Syntax Rules

- Must start at the beginning of a line with `//`
- Must appear in the first comment block (before any code)
- Case-sensitive, no space between `//` and the directive name
- Can appear multiple times where applicable

## Dependencies (`//DEPS`)

### Basic Format

```java
//DEPS groupId:artifactId:version
```

### Multiple Dependencies

```java
//DEPS com.fasterxml.jackson.core:jackson-core:2.15.2
//DEPS com.fasterxml.jackson.core:jackson-databind:2.15.2
```

Or on one line (space-separated):

```java
//DEPS com.google.code.gson:gson:2.11.0 info.picocli:picocli:4.6.3
```

### Classifiers

Append the classifier after the version with a colon:

```java
//DEPS io.netty:netty-transport-native-kqueue:4.1.107.Final:osx-aarch_64
```

### Fat JARs (Experimental)

Use `@fatjar` to skip transitive dependency resolution:

```java
//DEPS eu.maveniverse.maven.plugins:toolbox:0.1.9:cli@fatjar
```

### BOM POM for Managed Dependencies

The first `@pom` dependency defines version management — subsequent deps don't need explicit versions:

```java
//DEPS io.quarkus:quarkus-bom:3.2.0@pom
//DEPS io.quarkus:quarkus-resteasy-reactive
//DEPS io.quarkus:quarkus-smallrye-openapi
```

### Git Source Dependencies (JitPack)

Link directly to GitHub/GitLab/BitBucket projects — JBang converts them to JitPack artifacts:

```java
//DEPS https://github.com/jbangdev/jbang
//DEPS https://github.com/jbangdev/jbang/tree/v1.2.3
//DEPS https://github.com/jbangdev/jbang#mymodule
```

### Environment Variables in Dependencies

Use `${env.VAR}` or `${property}` with optional defaults:

```java
//DEPS org.postgresql:postgresql:${env.PG_VERSION:42.6.0}
//DEPS org.openjfx:javafx-graphics:17.0.2:${os.detected.jfxname}
```

Available OS properties (similar to `os-maven-plugin`):
- `${os.name}` — e.g., `Mac OS X`
- `${os.detected.jfxname}` — e.g., `mac`, `linux`, `win`

### Groovy-Style @Grab

Alternative syntax for those coming from Groovy:

```java
import groovy.lang.Grab;
import groovy.lang.Grapes;

@Grapes({
    @Grab(group="ch.qos.reload4j", module="reload4j", version="1.2.19")
})
```

### Checking for Updates

```bash
jbang deps@jbangdev myapp.java          # Check deps for newer versions
jbang deps@jbangdev com.google.code.gson:gson:2.10  # Check specific GAV
```

## Repositories (`//REPOS`)

```java
//REPOS central,jitpack
//REPOS central,myrepo=https://maven.example.com/releases
```

**Built-in shortcuts:** `central`, `google`, `jitpack`

**Important:** When any `//REPOS` is specified, Maven Central is NOT included automatically — add `central` explicitly.

Authentication uses `~/.m2/settings.xml` and `~/.m2/settings-security.xml`.

### Relative Repository Paths (Experimental)

```java
//REPOS local=./build/repo
```

### Transitive Repositories

JBang honors repositories from transitive dependencies by default. Disable with `--ignore-transitive-repositories` (`-itr`).

## Java Version (`//JAVA`)

```java
//JAVA 17       # Exact version required
//JAVA 11+      # Minimum version (downloads if needed)
//JAVA 21+      # JBang auto-downloads JDKs as needed
```

JBang downloads the required JDK automatically. Control vendor with:

```bash
export JBANG_JDK_VENDOR=temurin    # or azul, oracle, microsoft, etc.
export JBANG_DEFAULT_JAVA_VERSION=17
```

## Preview Features (`//PREVIEW`)

```java
//JAVA 21+
//PREVIEW
```

Equivalent to passing `--enable-preview` to both compiler and runtime.

## Compiler Options (`//COMPILE_OPTIONS`)

```java
//COMPILE_OPTIONS --enable-preview -source 17
//COMPILE_OPTIONS -Xlint:unchecked -Xlint:deprecation
```

## Runtime Options (`//RUNTIME_OPTIONS`)

```java
//RUNTIME_OPTIONS -Xmx2g -Xms512m
//RUNTIME_OPTIONS -XX:+UseG1GC
//RUNTIME_OPTIONS -Dfile.encoding=UTF-8
```

## Native Image Options (`//NATIVE_OPTIONS`)

For GraalVM `native-image` compilation:

```java
//NATIVE_OPTIONS -O2 --no-fallback
//NATIVE_OPTIONS -H:ReflectionConfigurationFiles=reflect.json
```

## Multi-File Organization

### Additional Sources (`//SOURCES`)

```java
//SOURCES util/Helper.java model/Person.java
```

Files are relative to the main script. Sources can recursively include more `//SOURCES`.

Without `//SOURCES`, JBang also supports auto-detection of files in the same directory and subdirectories, but explicit `//SOURCES` is more reliable for cross-package references.

### Resource Files (`//FILES`)

```java
//FILES config.properties                              # Same name
//FILES META-INF/resources/index.html=index.html       # Rename/relocate
```

## Application Metadata

### Description (`//DESCRIPTION`)

```java
//DESCRIPTION Database migration utility
//DESCRIPTION Supports PostgreSQL and MySQL
```

Multiple lines are concatenated. Used by `jbang alias list` and catalogs.

### GAV Coordinates (`//GAV`)

```java
//GAV com.example:my-script:1.0.0
```

Used by `jbang export maven` and `jbang export mavenrepo`.

### Manifest Entries (`//MANIFEST`)

```java
//MANIFEST Implementation-Version=1.0.0 Built-By=CI
//MANIFEST Sealed=true
```

### Main Class Override (`//MAIN`)

```java
//MAIN com.example.AlternativeMain
```

### Module Support (`//MODULE`) — Experimental

```java
//MODULE com.example.myapp
```

## Performance Features

### Class Data Sharing (`//CDS`) — Experimental

```java
//CDS
```

Requires Java 13+. Improves startup time for frequently-run scripts.

### Java Agent (`//JAVAAGENT`)

```java
//JAVAAGENT                                    # Mark script as agent
//JAVAAGENT io.opentelemetry.javaagent:opentelemetry-javaagent:1.20.0
//JAVAAGENT myagent.jar=option1,option2
```

## Multi-Language Directives

### Kotlin (`//KOTLIN`)

```kotlin
///usr/bin/env jbang "$0" "$@" ; exit $?
//KOTLIN 2.0.21
//DEPS org.jetbrains.kotlin:kotlin-stdlib:2.0.21

fun main(args: Array<String>) {
    println("Hello from Kotlin!")
}
```

### Groovy (`//GROOVY`)

```groovy
///usr/bin/env jbang "$0" "$@" ; exit $?
//GROOVY 3.0.19

println "Hello from Groovy!"
```

## Recommended Directive Order

For readability, order directives like this:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DESCRIPTION Your script description
//JAVA 17+
//PREVIEW
//MAIN com.example.Main
//GAV com.example:my-tool:1.0
//REPOS central,custom=https://...
//DEPS dependency1
//DEPS dependency2
//SOURCES additional.java
//FILES resources.properties
//MANIFEST Built-By=Dev
//COMPILE_OPTIONS -Xlint:all
//RUNTIME_OPTIONS -Xmx1g
//NATIVE_OPTIONS --no-fallback
//CDS
```

## Docs Directive (`//DOCS`)

Link documentation for discoverability:

```java
//DOCS https://myproject.org/docs/usage.html
//DOCS guide=./readme.md
```

Tags group links: `//DOCS guide=./readme.md` and `//DOCS guide=https://...` share the `guide` tag. View with `jbang info docs myapp.java`.
