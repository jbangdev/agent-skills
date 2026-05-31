# JBang Catalogs & Publishing — Full Guide

How to publish Java tools so users can run them with `jbang <cmd>@<your-org>`.

## Why Publish a Catalog?

A catalog gives you:
- **Short command names** — `jbang mytool@yourorg` instead of `jbang com.yourorg:mytool-cli:1.3.0:cli@fatjar`
- **Predefined arguments** — bundle default flags, JVM options, Java version requirements
- **Discoverability** — `jbang alias list yourorg` shows all available tools
- **Decoupled naming** — change the underlying artifact without changing the user-facing command

Real-world examples:
```bash
jbang quarkus@quarkusio           # Quarkus CLI
jbang camel@redhat-camel          # Apache Camel CLI
jbang minecraft-server@microsoft  # Minecraft server
jbang arthas@alibaba              # Alibaba Arthas diagnostics
```

## What Should Aliases Point To?

For app distribution, prefer this order:

1. **Maven artifact (GAV)** — best for versioned releases from Maven Central or private repos
2. **JAR/WAR URL** — great for GitHub releases, private distribution, or direct downloads
3. **Script** — fully supported, but secondary for app catalogs

JBang treats all three as runnable targets behind the same alias UX.

## Catalog Structure

### jbang-catalog.json Format

```json
{
  "aliases": {
    "mytool": {
      "script-ref": "com.yourorg:mytool-cli:1.3.0",
      "description": "Run MyTool CLI"
    },
    "mytool-lts": {
      "script-ref": "https://releases.yourorg.com/mytool-cli-1.2.5.jar",
      "description": "MyTool LTS release (stable)"
    },
    "devutil": {
      "script-ref": "devutil.java",
      "description": "Internal developer utility script"
    }
  },
  "catalogs": {},
  "templates": {}
}
```

### Alias Fields

| Field | Required | Description |
|-------|----------|-------------|
| `script-ref` | Yes | Maven GAV, JAR URL, script path, or another alias |
| `description` | Recommended | Shown in `jbang alias list` |
| `arguments` | Optional | Default arguments (array of strings) |
| `runtime-options` | Optional | JVM options (array of strings) |
| `java` | Optional | Java version requirement (e.g., `"17+"`) |
| `properties` | Optional | System properties (object of key-value pairs) |

### Full Alias Example

```json
{
  "aliases": {
    "myapp": {
      "script-ref": "com.example:myapp-cli:2.1.0",
      "description": "MyApp CLI with sensible defaults",
      "arguments": ["--format", "json", "--color"],
      "runtime-options": [
        "-Xmx512m",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED"
      ],
      "java": "17+",
      "properties": {
        "app.env": "production"
      }
    }
  }
}
```

Users run: `jbang myapp@yourorg` — they get all defaults automatically but can still pass additional arguments.

## Publishing Options

### Option A: Org-Level Catalog (Recommended)

1. Create `github.com/<your-org>/jbang-catalog`
2. Add `jbang-catalog.json` at root
3. Users run: `jbang <cmd>@<your-org>`

```bash
# Quick scaffold with CI pre-configured
jbang init -t jbang-catalog jbang-catalog
```

### Option B: Repo-Level Catalog

1. Add `jbang-catalog.json` to any existing repo
2. Users run: `jbang <cmd>@<your-org>/<repo>`

Good when commands are tightly coupled to one project.

### Option C: Website Catalog

1. Host `jbang-catalog.json` at `https://yourdomain.com/jbang-catalog.json`
2. Users run: `jbang <cmd>@yourdomain.com`

Can also use a subpath: `jbang <cmd>@yourdomain.com/path/to/jbang-catalog.json`

## Implicit Catalog Resolution

JBang automatically resolves `@` references:

| Pattern | Resolution |
|---------|-----------|
| `cmd@org` | `github.com/org/jbang-catalog/jbang-catalog.json` (then GitLab, BitBucket) |
| `cmd@org/repo` | `github.com/org/repo/jbang-catalog.json` |
| `cmd@org/repo/branch` | `github.com/org/repo/jbang-catalog.json` on that branch |
| `cmd@org~subdir` | `github.com/org/jbang-catalog/subdir/jbang-catalog.json` |
| `cmd@domain.com` | `https://domain.com/jbang-catalog.json` |
| `cmd@domain.com/path` | `https://domain.com/path/jbang-catalog.json` |

## Managing Aliases via CLI

Instead of editing JSON by hand:

```bash
# Add alias pointing to Maven artifact
jbang alias add -f jbang-catalog.json --name mytool \
  --description "Run MyTool CLI" \
  com.yourorg:mytool-cli:1.3.0

# Add alias pointing to JAR
jbang alias add -f jbang-catalog.json --name mytool-lts \
  --description "MyTool LTS" \
  https://releases.yourorg.com/mytool-1.2.5.jar

# Add alias pointing to script
jbang alias add -f jbang-catalog.json --name devutil \
  --description "Dev utility" \
  devutil.java

# Remove alias
jbang alias remove -f jbang-catalog.json mytool

# List aliases
jbang alias list -f jbang-catalog.json
```

## Local Catalog Resolution Order

JBang searches for `jbang-catalog.json` in this order:

1. `./jbang-catalog.json`
2. `./.jbang/jbang-catalog.json`
3. `../jbang-catalog.json` (and up to root)
4. `$HOME/.jbang/jbang-catalog.json`

This means project-level catalogs override global ones.

## Including Other Catalogs

Catalogs can reference other catalogs:

```json
{
  "catalogs": {
    "tools": {
      "catalog-ref": "https://github.com/myteam/shared-tools/blob/main/jbang-catalog.json",
      "description": "Shared team tools"
    }
  },
  "aliases": {
    "myapp": { "script-ref": "myapp.java" }
  }
}
```

Users can then run `jbang sometool@tools` or `jbang catalog add --name tools myteam`.

## Templates in Catalogs

Catalogs can also publish templates for `jbang init`:

```json
{
  "templates": {
    "microservice": {
      "file-refs": {
        "{basename}.java": "templates/microservice.java.qute"
      },
      "description": "Microservice starter template"
    }
  }
}
```

Users create from template: `jbang init -t microservice@yourorg myservice.java`

Templates support Qute syntax and `-Dkey=value` properties.

## Publishing via JitPack

Publish JBang scripts as Maven artifacts:

```yaml
# jitpack.yml
before_install:
  - curl -Ls https://sh.jbang.dev | bash -s - app setup
install:
  - ~/.jbang/bin/jbang export mavenrepo --force -O target \
      -Dgroup=$GROUP -Dartifact=$ARTIFACT -Dversion=$VERSION myapp.java
  - mkdir -p ~/.m2/repository
  - cp -rv target/* ~/.m2/repository/
```

Or bootstrap with: `jbang init -t jitpack@jbangdev`

## Keeping Dependencies Updated

Use Renovate to auto-update `//DEPS` versions in scripts:

```bash
jbang init -t renovate@jbangdev .github/renovate.json
```

Install the [Renovate GitHub App](https://github.com/apps/renovate) — it creates PRs for dependency updates automatically.

## Team Setup Script

```bash
#!/bin/bash
# team-setup.sh — onboard new developers
jbang app setup
jbang catalog add --name team https://github.com/myteam/jbang-tools
jbang app install deploy@team
jbang app install monitor@team
jbang app install dbmigrate@team
echo "Team tools installed! Run 'deploy --help' to get started."
```

## The JBang AppStore

Browse community-published scripts and tools at [jbang.dev/appstore](https://jbang.dev/appstore).
