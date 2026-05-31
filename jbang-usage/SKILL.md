# JBang Usage Skill

JBang lets you create, edit, and run self-contained source-only Java programs.

## Getting Started

Install JBang:

```bash
# Linux/macOS
curl -Ls https://sh.jbang.dev | bash -s - app setup

# Windows (PowerShell)
iex "& { $(iwr -useb https://ps.jbang.dev) } app setup"

# Homebrew
brew install jbangdev/tap/jbang

# SDKMAN
sdk install jbang
```

## Running a Script

```bash
jbang hello.java
```

## Inline Dependencies

Add dependencies directly in your Java source:

```java
//DEPS com.google.code.gson:gson:2.10.1
```

## Using a Specific Java Version

```java
//JAVA 21+
```

## Creating a Script from Template

```bash
jbang init --template=cli hello.java
```

## JBang Catalog

Browse and run scripts from the JBang AppStore:

```bash
jbang catalog list
jbang appstore@jbangdev
```

## Documentation

- User Guide: https://www.jbang.dev/documentation/guide/latest/index.html
- Download: https://www.jbang.dev/download
- AppStore: https://www.jbang.dev/appstore
- GitHub: https://github.com/jbangdev/jbang
