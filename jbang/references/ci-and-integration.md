# JBang CI/CD & Integration

Using JBang in pipelines, build tools, containers, and other languages.

## GitHub Actions

### Setup Action

```yaml
name: Build
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: jbangdev/setup-jbang@main
      - run: jbang mytool@myorg --input data.csv
```

### JBang Action (Docker-based)

```yaml
- uses: jbangdev/jbang-action@v0.109.0
  with:
    script: script.java
    args: "arg1 arg2"
```

### Run Scripts from Catalogs in CI

```yaml
- uses: jbangdev/setup-jbang@main
- run: |
    jbang app install deploy@myteam
    deploy --env staging
```

## Docker

### Official Images

```bash
# Run a script
docker run -v $(pwd):/ws --workdir=/ws jbangdev/jbang-action script.java

# Run a Maven artifact
docker run -v $(pwd):/ws --workdir=/ws jbangdev/jbang-action com.example:tool:1.0

# Quay.io alternative
docker run -v $(pwd):/ws --workdir=/ws quay.io/jbangdev/jbang-action script.java
```

**Note:** Remove `-ti` when using in CI (GitHub Actions, Jenkins, etc.).

### As a Base Image

```dockerfile
FROM jbangdev/jbang-action AS builder
COPY myapp.java /app/
RUN jbang export portable -O /app/dist /app/myapp.java

FROM eclipse-temurin:21-jre
COPY --from=builder /app/dist /app
CMD ["java", "-jar", "/app/myapp.jar"]
```

## Build Tool Plugins

### Maven Plugin

```xml
<plugin>
    <groupId>dev.jbang</groupId>
    <artifactId>jbang-maven-plugin</artifactId>
    <version>0.0.8</version>
    <executions>
        <execution>
            <id>run</id>
            <phase>process-resources</phase>
            <goals><goal>run</goal></goals>
            <configuration>
                <script>codegen.java</script>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Gradle Plugin

```groovy
plugins {
    id 'dev.jbang' version '0.2.0'
}
```

```bash
gradle jbang --jbang-script hello.jsh --jbang-args="Hello world"
```

## Programming Language Integrations

### JavaScript / Node.js

```bash
npx @jbangdev/jbang app setup
```

Or in `package.json`:
```json
{ "devDependencies": { "@jbangdev/jbang": "^0.1.4" } }
```

```javascript
const jbang = require('@jbangdev/jbang');
jbang.exec('properties@jbangdev');
```

### Python

```bash
pipx jbang app setup
uvx jbang app setup
```

```python
import jbang
jbang.exec('properties@jbangdev')
```

Works in Jupyter notebooks too.

## JBang Wrapper

Pin a specific JBang version for reproducible builds (like Maven/Gradle wrappers):

```bash
jbang wrapper install
```

Creates `jbang` and `jbang.cmd` wrapper scripts in your project. Commit them to source control — CI and team members use the pinned version automatically.

The `.jbang/` cache directory should be `.gitignore`d.

## Zero-Install Execution

Run JBang without installing it first:

```bash
# Linux/macOS — downloads JBang temporarily, runs your command
curl -Ls https://sh.jbang.dev | bash -s - properties@jbangdev

# Windows PowerShell
iex "& { $(iwr -useb https://ps.jbang.dev) } properties@jbangdev"
```

## Download Configuration

For CI environments with flaky networks:

```bash
export JBANG_DOWNLOAD_RETRY=10          # Retry count (default: 5)
export JBANG_DOWNLOAD_RETRY_DELAY=0     # 0 = progressive backoff (recommended)
export JBANG_DOWNLOAD_URL=https://internal-mirror.example.com/jbang/jbang.tar  # Corporate mirror
export JBANG_DOWNLOAD_VERSION=early-access   # Or a specific version like "0.120.0"
```

## Environment Variables Reference

| Variable | Default | Description |
|----------|---------|-------------|
| `JBANG_DIR` | `~/.jbang` | Root directory for config, apps, cache |
| `JBANG_CACHE_DIR` | `$JBANG_DIR/cache` | Cached JDKs, compiled scripts, downloads |
| `JBANG_DEFAULT_JAVA_VERSION` | `17` | Java version when no JDK is found |
| `JBANG_JDK_VENDOR` | `temurin` | JDK distribution (temurin, azul, oracle, etc.) |
| `JBANG_DOWNLOAD_RETRY` | `5` | Download retry attempts |
| `JBANG_DOWNLOAD_RETRY_DELAY` | `0` | Retry delay (0 = progressive backoff) |
| `JBANG_DOWNLOAD_URL` | GitHub | Override JBang download URL |
| `JBANG_DOWNLOAD_VERSION` | latest | Specific JBang version to download |
| `JBANG_JAVA_OPTIONS` | — | Extra JVM options for JBang itself |
| `JBANG_NO_VERSION_CHECK` | — | Disable auto-update check |
| `JBANG_REPO` | `~/.m2` | Local Maven repository path |

## Proxy Configuration

```bash
# For JBang's bash/shell scripts
export http_proxy=http://proxyhost:8888
export https_proxy=http://proxyhost:8888

# For Java itself
export JAVA_TOOL_OPTIONS="-Djava.net.useSystemProxies=true"
# Or explicitly:
export JAVA_TOOL_OPTIONS="-Dhttp.proxyHost=proxyhost -Dhttp.proxyPort=8888"
```

Maven dependency resolution also honors `~/.m2/settings.xml` proxy config.

## Offline Mode

```bash
jbang --offline myapp.java              # Fail if deps not cached
jbang -o com.example:tool:1.0           # Same for Maven artifacts
```

## Configuration System

```bash
jbang config set edit.open code         # Set default editor
jbang config set run.debug 4004         # Default debug port
jbang config list --show-available      # See all possible keys
jbang config list --show-origin         # See which file sets what

# Local config (project-level overrides)
jbang config set --file=. edit.open idea
```

Any CLI `--option` can become a config default via `<command>.<option>` naming.
