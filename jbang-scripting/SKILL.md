---
name: jbang-scripting
description: How to WRITE Java scripts with JBang — single-file programs with inline dependencies, no project setup. Use this skill when the user wants to write a Java script, create a single-file Java program, add dependencies via //DEPS without Maven/Gradle, create a CLI tool in Java, use JBang directives (//DEPS, //JAVA, //SOURCES, etc.), generate scripts from templates, or needs to know about JBang's script syntax and features. This skill is about WRITING code — for running JARs, installing tools, managing JDKs, or publishing apps via catalogs without writing code, see the jbang skill instead.
---

# JBang Scripting — Write and Run Java Scripts Instantly

JBang lets you run `.java` files directly — no `pom.xml`, no `build.gradle`, no project structure. Just a single file with inline dependency declarations.

## Quick Reference

### Install JBang

```bash
curl -Ls https://sh.jbang.dev | bash -s - app setup   # Linux/macOS
sdk install jbang                                        # SDKMAN
brew install jbangdev/tap/jbang                          # Homebrew
```

### Core Workflow

```bash
jbang init hello.java              # Create a script from template
jbang hello.java                   # Compile + run in one step
jbang edit hello.java              # Open in IDE with full IntelliSense
jbang app install hello.java       # Install as a system command
```

### Minimal Script

```java
///usr/bin/env jbang "$0" "$@" ; exit $?

class hello {
    public static void main(String[] args) {
        System.out.println("Hello " + (args.length > 0 ? args[0] : "World"));
    }
}
```

The first line (`///usr/bin/env jbang ...`) is the shebang — it lets you run the file directly on Unix (`chmod +x hello.java && ./hello.java`) while remaining valid Java.

### Adding Dependencies

Use `//DEPS` to declare Maven dependencies inline:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.6.3

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "greet", mixinStandardHelpOptions = true)
class greet implements Runnable {
    @Parameters(description = "Who to greet") String name = "World";

    public static void main(String[] args) {
        new CommandLine(new greet()).execute(args);
    }

    public void run() { System.out.println("Hello " + name + "!"); }
}
```

### Essential Directives

All directives go at the top of the file as `//` comments (no space between `//` and directive name):

| Directive | Purpose | Example |
|-----------|---------|---------|
| `//DEPS` | Maven dependencies | `//DEPS com.google.code.gson:gson:2.11.0` |
| `//REPOS` | Custom Maven repos | `//REPOS central,jitpack` |
| `//JAVA` | Java version | `//JAVA 17+` |
| `//PREVIEW` | Enable preview features | `//PREVIEW` |
| `//SOURCES` | Additional source files | `//SOURCES util/Helper.java` |
| `//FILES` | Bundle resource files | `//FILES config.properties` |
| `//COMPILE_OPTIONS` | Javac flags | `//COMPILE_OPTIONS -Xlint:all` |
| `//RUNTIME_OPTIONS` | JVM flags | `//RUNTIME_OPTIONS -Xmx2g` |
| `//NATIVE_OPTIONS` | GraalVM native-image flags | `//NATIVE_OPTIONS --no-fallback` |
| `//DESCRIPTION` | Script description | `//DESCRIPTION My cool tool` |
| `//MAIN` | Override main class | `//MAIN com.example.App` |
| `//GAV` | Maven coordinates for export | `//GAV com.example:my-tool:1.0` |
| `//MANIFEST` | JAR manifest entries | `//MANIFEST Built-By=Dev` |
| `//CDS` | Class Data Sharing (fast start) | `//CDS` |

### Templates

```bash
jbang init hello.java                  # Basic hello world
jbang init --template=cli myapp.java   # CLI with picocli
jbang init --template=qcli app.java    # Quarkus CLI
jbang template list                    # See all available templates
```

JBang also supports AI-powered code generation:

```bash
export OPENAI_API_KEY="your-key"
jbang init hello.java "create a REST client that fetches weather data"
```

### Running Scripts

```bash
jbang myapp.java                         # Run local file
jbang myapp.java arg1 arg2               # Pass arguments
jbang https://gist.github.com/.../x.java # Run from URL
jbang hello@jbangdev                     # Run from catalog alias
./myapp.java                             # Direct execution (Unix, after chmod +x)
jbang --native myapp.java                # Build + run native binary
jbang --jfr myapp.java                   # Run with Flight Recorder
jbang --interactive myapp.java           # REPL mode with jshell
```

### Aliases, Catalogs & App Install

For distributing scripts, installing them as system commands, and publishing catalogs, see the **jbang** skill — those features work the same whether you wrote a script or are running a JAR.

### IDE Support

```bash
jbang edit myapp.java                        # Open with IDE (auto-detects)
jbang edit --open=idea myapp.java            # Open with specific IDE
jbang edit --live myapp.java                 # Live-reload on changes
```

Supported IDEs: VS Code / VSCodium, IntelliJ IDEA, Eclipse, NetBeans, Neovim, Emacs.

### Exporting to Full Projects

```bash
jbang export maven myapp.java               # Export as Maven project
jbang export gradle myapp.java              # Export as Gradle project
jbang export portable myapp.java            # Portable JAR + lib/
jbang export mavenrepo myapp.java           # Publish to local Maven repo
```

### Multi-Language Support

JBang supports `.java`, `.jsh` (JShell), `.kt` (Kotlin), `.groovy`, and `.md` (Markdown with embedded code):

```bash
jbang init --template=hello.kt hello.kt     # Kotlin
jbang init --template=hello.groovy hi.groovy # Groovy
```

## Verification Script

To verify JBang is working, run the bundled verification script:

```bash
jbang scripts/verify_jbang.java
```

This checks installation, Java version, dependency resolution, and template system.

## Further Reading

For deeper topics, read the reference files in `references/`:

- **`references/directives-deep-dive.md`** — Complete directive reference with advanced patterns (BOM POMs, environment variable substitution, classifiers, multi-file organization)
- **`references/patterns-and-recipes.md`** — Common patterns: CLI tools, web servers, REST clients, file processing, database access, testing, and more
For aliases, catalogs, app installation, JDK management, and CI/CD integration, see the **jbang** skill.
