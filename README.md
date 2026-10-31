# JBang Agent Skills

AI agent skill definitions for [JBang](https://www.jbang.dev) — discoverable via the [Agent Skills Discovery](https://github.com/cloudflare/agent-skills-discovery-rfc) standard.

## Skills

| Skill | Description |
|-------|-------------|
| [jbang](jbang/SKILL.md) | Use JBang as a universal Java launcher and tool manager — run/debug JARs, Maven artifacts, and remote scripts, install tools, manage JDKs, and publish catalogs. No code writing required. |
| [jbang-scripting](jbang-scripting/SKILL.md) | Write single-file Java scripts with inline dependencies (`//DEPS`), templates, and directives — no project setup. |

## Discovery

These skills are indexed at [`https://www.jbang.dev/.well-known/agent-skills/index.json`](https://www.jbang.dev/.well-known/agent-skills/index.json).

## Contributing

PRs welcome! Each skill lives in its own directory with a `SKILL.md` file. After adding or updating a skill, update the digest in the [index on jbang.dev](https://github.com/jbangdev/jbang.dev/blob/main/public/.well-known/agent-skills/index.json).
