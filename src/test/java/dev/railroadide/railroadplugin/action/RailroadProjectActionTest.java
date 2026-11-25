package dev.railroadide.railroadplugin.action;

import dev.railroadide.railroadplugin.dto.RailroadConfiguration;
import dev.railroadide.railroadplugin.dto.RailroadDependency;
import dev.railroadide.railroadplugin.dto.RailroadModule;
import dev.railroadide.railroadplugin.dto.RailroadProject;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Builds a multi-project build whose subproject depends on a jar from a local Maven repository, applies the plugin
 * the way Railroad does (through an init script) and checks the subproject's classpath through the tooling API.
 */
class RailroadProjectActionTest {
    private static final String GRADLE_VERSION = System.getProperty("railroad.test.gradleVersion", "9.7.0");
    private static final String LIBRARY_JAR = "greeting-1.0.jar";

    @TempDir
    static Path projectDir;
    private static Path initScript;

    @BeforeAll
    static void createBuild() throws IOException {
        Path repo = projectDir.resolve("repo/com/example/greeting/1.0");
        Files.createDirectories(repo);
        var manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        try (var jar = new JarOutputStream(Files.newOutputStream(repo.resolve(LIBRARY_JAR)), manifest)) {
            // An empty jar is enough: only its presence on the classpath matters.
        }
        write(repo.resolve("greeting-1.0.pom"), """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>greeting</artifactId>
                    <version>1.0</version>
                </project>
                """);

        write(projectDir.resolve("settings.gradle"), """
                rootProject.name = 'sample'
                include 'app'
                """);
        write(projectDir.resolve("build.gradle"), "");
        // Parallel mode gives every project its own lock (instead of one lock for the whole build), which is when
        // Gradle 9 rejects resolving another project's configurations. Industria-style builds enable it.
        write(projectDir.resolve("gradle.properties"), "org.gradle.parallel=true\n");
        write(projectDir.resolve("app/build.gradle"), """
                plugins { id 'java' }
                repositories { maven { url = uri('../repo') } }
                sourceSets { client { java.srcDirs = ['client-code']; compileClasspath += sourceSets.main.output } }
                dependencies { implementation 'com.example:greeting:1.0'; clientImplementation files('client-only.jar') }
                """);

        // Mirrors Railroad's init-gradle-plugin.gradle, with the plugin under test instead of a published version.
        String pluginClasspath = pluginUnderTestClasspath().stream()
                .map(file -> "'" + file.getAbsolutePath().replace('\\', '/') + "'")
                .collect(Collectors.joining(", "));
        initScript = projectDir.resolve("railroad-init.gradle");
        write(initScript, """
                initscript {
                    dependencies { classpath files(%s) }
                }
                beforeSettings { settings ->
                    settings.pluginManager.apply(dev.railroadide.railroadplugin.RailroadSettingsPlugin)
                }
                allprojects {
                    apply plugin: dev.railroadide.railroadplugin.RailroadProjectPlugin
                }
                """.formatted(pluginClasspath));
    }

    @Test
    void actionResolvesSubprojectClasspath() {
        RailroadProject project;
        try (ProjectConnection connection = connect()) {
            project = connection.action(new RailroadProjectAction())
                    .withArguments("--init-script", initScript.toString())
                    .run();
        }

        assertEquals("sample", project.getName());
        assertEquals(List.of(":", ":app"), project.getModules().stream().map(RailroadModule::getPath).sorted().toList());

        RailroadModule app = module(project, ":app");
        assertEquals(List.of(), app.getResolutionFailures());
        assertTrue(containsLibrary(app.getClasspathRoots()), "classpath roots: " + app.getClasspathRoots());
        assertTrue(containsLibrary(app.getDependencyRoots()), "dependency roots: " + app.getDependencyRoots());

        var sets = app.getContentRoots().stream().collect(Collectors.toMap(root -> root.getSourceSetName(), root -> root));
        var main = sets.get("main");
        var client = sets.get("client");
        assertTrue(main.getTestSourceDirectories().isEmpty());
        assertTrue(sets.get("test").getSourceDirectories().isEmpty());
        assertTrue(client.getSourceDirectories().stream().anyMatch(dir -> dir.getDirectory().getName().equals("client-code")));
        assertTrue(client.getCompileClasspath().stream().anyMatch(main.getClassOutputs()::contains));
        assertTrue(client.getCompileClasspath().stream().anyMatch(file -> file.getName().equals("client-only.jar")));
        assertFalse(main.getCompileClasspath().stream().anyMatch(file -> file.getName().equals("client-only.jar")));
        assertNotNull(main.getCompilerHome());

        RailroadConfiguration compileClasspath = app.getConfigurations().stream()
                .filter(configuration -> configuration.getName().equals("compileClasspath"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("compileClasspath missing from " + app.getConfigurations()));
        assertEquals(List.of(), compileClasspath.getResolutionFailures());
        RailroadDependency greeting = compileClasspath.getDependencies().stream()
                .filter(dependency -> dependency.getName().equals("greeting"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("greeting missing from " + compileClasspath.getDependencies()));
        assertEquals(LIBRARY_JAR, greeting.getFile().getName());
    }

    @Test
    void rootModelReportsSubprojectResolutionFailures() {
        assumeTrue(majorVersion(GRADLE_VERSION) >= 9,
                "Gradle " + GRADLE_VERSION + " still allows resolving other projects' configurations");

        RailroadProject project;
        try (ProjectConnection connection = connect()) {
            project = connection.model(RailroadProject.class)
                    .withArguments("--init-script", initScript.toString())
                    .get();
        }

        // The root-level model cannot resolve :app under Gradle 9, but it must say so instead of reporting no jars.
        RailroadModule app = module(project, ":app");
        assertFalse(containsLibrary(app.getClasspathRoots()), "classpath roots: " + app.getClasspathRoots());
        assertFalse(app.getResolutionFailures().isEmpty(), "expected resolution failures for :app");
        assertTrue(app.getResolutionFailures().stream().anyMatch(failure -> failure.contains(":app:compileClasspath")),
                "failures: " + app.getResolutionFailures());
        assertTrue(app.getConfigurations().stream().anyMatch(configuration ->
                        configuration.getName().equals("compileClasspath") && !configuration.getResolutionFailures().isEmpty()),
                "compileClasspath should still be listed, with its failure");
    }

    private static ProjectConnection connect() {
        return GradleConnector.newConnector()
                .forProjectDirectory(projectDir.toFile())
                .useGradleVersion(GRADLE_VERSION)
                .connect();
    }

    private static RailroadModule module(RailroadProject project, String path) {
        return project.getModules().stream()
                .filter(module -> module.getPath().equals(path))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no module " + path));
    }

    private static boolean containsLibrary(List<File> roots) {
        return roots.stream().anyMatch(file -> file.getName().equals(LIBRARY_JAR));
    }

    private static int majorVersion(String version) {
        return Integer.parseInt(version.split("\\.")[0]);
    }

    private static List<File> pluginUnderTestClasspath() {
        try (InputStream in = RailroadProjectActionTest.class.getClassLoader()
                .getResourceAsStream("plugin-under-test-metadata.properties")) {
            assertNotNull(in, "plugin-under-test-metadata.properties is missing from the test classpath");
            var properties = new Properties();
            properties.load(in);
            return Arrays.stream(properties.getProperty("implementation-classpath").split(File.pathSeparator))
                    .map(File::new)
                    .toList();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static void write(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }
}
