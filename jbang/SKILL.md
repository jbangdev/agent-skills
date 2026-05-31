---
name: jbang
description: JBang as a universal Java launcher and tool manager — run JARs, Maven artifacts, and remote scripts without writing any code. Use this skill when the user wants to run a JAR or WAR file, execute a Maven artifact by GAV coordinates, install a Java tool as a system command, manage JDK versions, browse the JBang AppStore, set up catalogs to publish or distribute Java apps, run remote scripts from URLs or GitHub, or use JBang in CI/CD pipelines. Also triggers for "jbang app install", "jbang jdk", running anything by Maven coordinates, or publishing apps via catalogs. This skill is about USING and DISTRIBUTING Java tools — for WRITING JBang scripts, see the jbang-scripting skill instead.
---

# JBang — Universal Java Launcher & Tool Manager

JBang isn't just for scripting. It's a universal launcher for anything Java — run JARs, Maven artifacts, WARs, and remote code from URLs, all without project setup. Manage JDK installations. Install Java tools as system commands. Publish your apps so users can run them with a single short command.

**No code writing required for any of this.**

## Install JBang

```bash
curl -Ls https://sh.jbang.dev | bash -s - app setup   # Linux/macOS/Windows (bash)
sdk install jbang                                       # SDKMAN
brew install jbangdev/tap/jbang                         # Homebrew
choco install jbang                                     # Chocolatey
```

## Run Anything Java

### By Maven Coordinates (GAV)

Run any artifact from Maven Central — no download, no install, no code:

```bash
jbang com.h2database:h2:2.2.224                          # H2 database console
jbang info.picocli:picocli-codegen:4.6.3                 # Picocli code generator
jbang org.apache.camel:camel-jbang:4.4.0                 # Apache Camel CLI

# With a specific main class
jbang --main com.example.Tool com.example:mylib:1.0.0

# Add extra dependencies at runtime
jbang --deps org.postgresql:postgresql:42.6.0 com.example:dbtools:1.0
```

### JARs and WARs

```bash
jbang myapp.jar                                          # Local JAR
jbang myapp.jar arg1 arg2                                # With arguments
jbang https://example.com/releases/tool-1.0.jar          # Remote JAR
jbang https://download.example.com/app.war               # WAR files too

# Add dependencies to existing JARs
jbang --deps com.google.code.gson:gson:2.11.0 myapp.jar

# Specify main class
jbang --main com.example.Main myapp.jar
```

### Remote Scripts and URLs

```bash
jbang https://github.com/user/repo/blob/main/tool.java
jbang https://gist.github.com/user/abc123
jbang https://example.com/scripts/utility.java

# JBang auto-converts GitHub/GitLab/Bitbucket page URLs to raw content
```

### Inline Code

```bash
jbang -c 'System.out.println("Hello from JBang!")'
```

### Interactive REPL

```bash
jbang --interactive                                      # Plain JShell
jbang --interactive myapp.java                           # JShell with your code + deps loaded
```

## Aliases & Catalogs

Catalogs are the way to **publish and distribute** Java tools with short, memorable names. Users don't need to know about Maven coordinates, JAR locations, or flags — they just type a short command.

### Running from Catalogs

```bash
# Format: jbang <alias>@<org-or-domain>
jbang hello@jbangdev                                     # Hello world from jbangdev catalog
jbang gavsearch@jbangdev hibernate                       # Search Maven Central
jbang httpd@jbangdev -d ./public                         # Serve a directory
jbang quarkus@quarkusio                                  # Quarkus CLI
jbang camel@redhat-camel                                 # Camel CLI

# How resolution works:
#   hello@jbangdev  →  github.com/jbangdev/jbang-catalog/jbang-catalog.json
#   hello@acme.com  →  https://acme.com/jbang-catalog.json
```

### Creating Local Aliases

```bash
jbang alias add --name mytool myapp.java
jbang alias add --name h2 com.h2database:h2:2.2.224
jbang alias add --description "Run our deploy tool" --name deploy deploy.jar
jbang alias list
jbang alias list --show-origin                           # See where each alias lives
```

### Publishing a Catalog for Your Org/Project

This is how real projects like Quarkus, Camel, and Microsoft publish their tools. Users run `jbang <cmd>@<your-org>` and it just works.

**Step 1:** Create a `jbang-catalog` repo (or add `jbang-catalog.json` to any repo).

**Step 2:** Define aliases — they can point to Maven artifacts, JARs, or scripts:

```json
{
  "aliases": {
    "mytool": {
      "script-ref": "com.yourorg:mytool-cli:1.3.0",
      "description": "Run MyTool CLI"
    },
    "mytool-lts": {
      "script-ref": "https://releases.yourorg.com/mytool-1.2.5.jar",
      "description": "MyTool LTS release"
    },
    "devutil": {
      "script-ref": "devutil.java",
      "description": "Internal development utility"
    }
  }
}
```

**Step 3:** Users run it:

```bash
jbang mytool@your-org
jbang app install mytool@your-org   # Or install as system command
mytool --help
```

For detailed catalog publishing, read `references/catalogs-and-publishing.md`.

### Catalog Placement Options

| Option | Location | User runs | Best for |
|--------|----------|-----------|----------|
| **Org-level** (recommended) | `github.com/<org>/jbang-catalog` repo | `jbang cmd@org` | Multiple tools, stable namespace |
| **Repo-level** | `jbang-catalog.json` in any repo | `jbang cmd@org/repo` | Single product |
| **Website** | `https://domain.com/jbang-catalog.json` | `jbang cmd@domain.com` | Custom domains |

### Alias Options in Catalog JSON

Aliases can bundle default arguments, JVM options, and Java version requirements:

```json
{
  "aliases": {
    "myapp": {
      "script-ref": "com.example:myapp:2.0.0",
      "description": "My application with predefined settings",
      "arguments": ["--verbose", "--port", "8080"],
      "runtime-options": ["--add-opens", "java.base/java.lang=ALL-UNNAMED"],
      "java": "17+"
    }
  }
}
```

### Adding Catalogs

```bash
jbang catalog add --name team https://github.com/myteam/jbang-tools
jbang alias list team                                    # Browse catalog
jbang deploy@team                                        # Run from catalog
```

### Quick Catalog Bootstrap

```bash
jbang init -t jbang-catalog jbang-catalog                # Creates repo scaffold with CI
```

This generates a catalog repo with Renovate and GitHub Actions pre-configured.

## Install Java Tools as System Commands

```bash
jbang app setup                                          # One-time: add ~/.jbang/bin to PATH

# Install from catalog alias
jbang app install gavsearch@jbangdev
gavsearch hibernate                                      # Now a regular command!

# Install with custom name
jbang app install --name search gavsearch@jbangdev
search hibernate

# Install from Maven coordinates
jbang app install info.picocli:picocli-codegen:4.6.3

# Install from URL
jbang app install https://github.com/user/repo/blob/main/tool.java

# Install as native image (requires GraalVM)
jbang app install --native mytool.java

# Manage installed apps
jbang app list
jbang app uninstall search
jbang app install --force gavsearch@jbangdev              # Update/reinstall
```

## JDK Management

JBang downloads and manages JDKs automatically — no manual installation needed:

```bash
jbang jdk list                                           # Show installed JDKs
jbang jdk install 21                                     # Install specific version
jbang jdk install 17 --vendor temurin                     # Specific vendor
jbang jdk default 21                                     # Set default JDK
jbang jdk home 17                                        # Print JDK path
jbang jdk java-env 21                                    # Print env vars for shell
jbang jdk uninstall 11                                   # Remove a JDK

# Use in shell scripts:
eval $(jbang jdk java-env 21)                            # Switch JDK in current shell
```

### JDK Configuration

```bash
export JBANG_DEFAULT_JAVA_VERSION=17                     # Default version when none specified
export JBANG_JDK_VENDOR=temurin                          # Vendor: temurin, azul, oracle, microsoft, etc.
```

JBang auto-downloads the right JDK when a script requires a specific version (`//JAVA 21+`).

## Trust and Security

```bash
jbang trust add https://github.com/myorg/                # Trust a GitHub org
jbang trust add https://internal.example.com/            # Trust internal sources
jbang trust list                                         # Show trusted sources
jbang trust remove https://github.com/myorg/

# Run with certificate bypass (use cautiously)
jbang --insecure https://self-signed.example.com/app.java
```

## CI/CD Integration

### GitHub Actions

```yaml
- uses: jbangdev/setup-jbang@main
- run: jbang mytool@myorg --input data.csv
```

### Docker

```bash
docker run -v $(pwd):/ws --workdir=/ws jbangdev/jbang-action mytool.java
docker run -v $(pwd):/ws --workdir=/ws jbangdev/jbang-action com.example:tool:1.0
```

### Build Tool Plugins

```bash
# Maven plugin — run JBang scripts during builds
# Gradle plugin — same for Gradle builds
```

See `references/ci-and-integration.md` for full details.

## Configuration

```bash
jbang config set edit.open code                          # Default editor
jbang config set run.debug 4004                          # Default debug port
jbang config list                                        # Show all config
jbang config list --show-available                       # Show all possible keys
jbang config list --show-origin                          # Show config file locations
```

Any CLI option can become a default: `<command>.<option>` (e.g., `app.list.format`).

## Cache Management

```bash
jbang cache clear                                        # Clear all caches
jbang cache list                                         # Show cache contents
jbang --fresh mytool@myorg                               # Force re-download
jbang --offline mytool@myorg                              # Fail if not cached
```

## Common Real-World Examples

```bash
# Run H2 database console
jbang com.h2database:h2:2.2.224

# Search Maven Central from CLI
jbang gavsearch@jbangdev hibernate

# Serve current directory over HTTP
jbang httpd@jbangdev -d .

# Run any GitHub-hosted Java file
jbang https://github.com/jbangdev/jbang-examples/blob/main/examples/helloworld.java

# Quick team tool setup
jbang app setup
jbang catalog add --name team https://github.com/myteam/tools
jbang app install deploy@team
jbang app install monitor@team
deploy --env staging
```

## Further Reading

- **`references/catalogs-and-publishing.md`** — Full guide to creating, structuring, and publishing catalogs for your org or project
- **`references/ci-and-integration.md`** — Docker, GitHub Actions, Maven/Gradle plugins, npm/pip integration, wrappers
