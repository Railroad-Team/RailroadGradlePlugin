# Railroad Gradle Plugin

Gradle plugin that exposes project metadata to the Railroad IDE via the Tooling API and optionally downloads dependency documentation (sources/Javadoc) for quicker IDE setup. It detects common Minecraft modding setups (Fabric/Forge) and reports versions to the IDE.

## Getting started

1. Add the Railroad Maven repository to your plugin management block so Gradle can resolve the plugin:
   ```groovy
   pluginManagement {
       repositories {
           gradlePluginPortal()
           maven { url "https://maven.railroadide.dev/releases" }
       }
   }
   ```
2. Apply the plugin to your build:
   ```groovy
   plugins {
       id "dev.railroadide.railroadgradleplugin" version "1.1.0"
   }
   ```

The plugin requires a Java 21 Gradle runtime (the plugin itself is built with Java 21).

## What the plugin provides

- **Railroad IDE models**: Registers custom tooling models that describe the Gradle project, Java settings, source roots, compiler output, tasks, and resolvable configurations. Fabric and Forge projects include detected versions (Minecraft, mappings, loader/Fabric API, Loom/ForgeGradle) so the IDE can configure the workspace automatically.
- **Documentation download tasks**: Tasks for resolving sources and/or Javadoc for all resolvable configurations to warm caches before opening the project in the IDE.

## Documentation download tasks

After applying the plugin, the following tasks are available:

- `downloadDependencySources` – downloads source jars for resolvable configurations.
- `downloadDependencyJavadoc` – downloads Javadoc jars.
- `downloadDependencyDocumentation` – downloads both; runs after the two tasks above.

Configuration is exposed via the `downloadSources` extension:

```groovy
downloadSources {
    downloadSources = true      // enable/disable source download (default: true)
    downloadJavadoc = true      // enable/disable Javadoc download (default: true)
    configurations = ["compileClasspath", "runtimeClasspath", "testCompileClasspath"]
    logResolvedFiles = true     // log resolved files at info level (default: true, otherwise debug)
}
```

Example usage:

```bash
./gradlew downloadDependencyDocumentation
```

Only resolvable, non-consumable configurations are processed; tasks are skipped if nothing is requested.

## Development

- Build the plugin locally: `./gradlew build`
- Publish to your local Maven repository for testing: `./gradlew publishToMavenLocal`

Licensed under the MIT license; see `LICENSE`.
