package dev.railroadide.railroadplugin.dto;

import dev.railroadide.railroadplugin.dto.impl.*;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.internal.tasks.options.OptionDescriptor;
import org.gradle.api.internal.tasks.options.OptionReader;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.tooling.internal.gradle.DefaultProjectIdentifier;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.ProjectIdentifier;
import org.gradle.tooling.model.java.InstalledJdk;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class DTOBuilder {
    public static RailroadProject buildProject(Project project) {
        return buildProject(project, buildProjectReference(project.getParent()));
    }

    private static RailroadProject buildProject(Project project, @Nullable RailroadProject parentDto) {
        List<RailroadModule> modules = new ArrayList<>();
        RailroadJavaLanguageSettings javaLanguageSettings = buildJavaLanguageSettings(project);

        var dto = new BasicRailroadProject(
                project.getName(),
                project.getDescription(),
                parentDto,
                modules,
                javaLanguageSettings
        );

        for (Project sub : project.getAllprojects()) {
            modules.add(buildModule(sub, buildProjectReference(dto)));
        }

        return dto;
    }

    private static RailroadModule buildModule(Project project, RailroadProject parentProject) {
        List<RailroadConfiguration> configurations = new ArrayList<>();
        List<RailroadGradleTask> tasks = new ArrayList<>();
        List<RailroadContentRoot> contentRoots = buildContentRoots(project);
        RailroadJavaLanguageSettings javaLanguageSettings = buildJavaLanguageSettings(project);
        RailroadCompilerOutput compilerOutput = buildCompilerOutput(project);
        List<File> dependencyRoots = buildDependencyRoots(project);
        List<File> classpathRoots = buildClasspathRoots(project);
        List<File> modulePathRoots = buildModulePathRoots(classpathRoots);

        var projectIdentifier = new DefaultProjectIdentifier(project.getRootDir(), project.getPath());
        var gradleProjectRef = buildGradleProjectReference(project, projectIdentifier);
        List<GradleTask> gradleTasks = new ArrayList<>();
        var gradleProject = new BasicGradleProject(
                project.getName(),
                project.getDescription(),
                project.getPath(),
                project.getProjectDir(),
                project.getLayout().getBuildDirectory().getAsFile().getOrNull(),
                new BasicGradleScriptAdapter(project.getBuildFile()),
                projectIdentifier,
                null,
                Collections.emptyList(),
                gradleTasks
        );

        Supplier<RailroadModule> moduleRefSupplier = () -> buildModuleReference(
                project,
                parentProject,
                javaLanguageSettings,
                compilerOutput,
                contentRoots,
                gradleProjectRef,
                projectIdentifier
        );

        var module = new BasicRailroadModule(
                project.getName(),
                project.getDescription(),
                project.getPath(),
                project.getProjectDir(),
                parentProject,
                javaLanguageSettings,
                compilerOutput,
                contentRoots,
                dependencyRoots,
                classpathRoots,
                modulePathRoots,
                configurations,
                tasks,
                gradleProject,
                projectIdentifier
        );

        tasks.addAll(buildTasks(project, moduleRefSupplier, gradleProjectRef, gradleTasks));
        configurations.addAll(buildConfigurations(project, moduleRefSupplier));

        return module;
    }

    private static RailroadCompilerOutput buildCompilerOutput(Project project) {
        File outputDir = findClassesDir(project, SourceSet.MAIN_SOURCE_SET_NAME);
        File testOutputDir = findClassesDir(project, SourceSet.TEST_SOURCE_SET_NAME);
        return new BasicRailroadCompilerOutput(false, outputDir, testOutputDir);
    }

    private static List<File> buildDependencyRoots(Project project) {
        LinkedHashSet<File> roots = new LinkedHashSet<>();
        for (Configuration configuration : project.getConfigurations()) {
            if (!configuration.isCanBeResolved())
                continue;

            roots.addAll(resolveConfigurationFiles(configuration));
        }

        return List.copyOf(roots);
    }

    private static List<File> buildClasspathRoots(Project project) {
        LinkedHashSet<File> roots = new LinkedHashSet<>();
        SourceSetContainer sourceSets = project.getExtensions().findByType(SourceSetContainer.class);
        if (sourceSets != null) {
            for (SourceSet sourceSet : sourceSets) {
                try {
                    roots.addAll(sourceSet.getCompileClasspath().getFiles());
                } catch (Exception ignored) {
                    // Keep model building best-effort when an individual classpath cannot be resolved.
                }
            }
        }

        if (roots.isEmpty())
            roots.addAll(resolveMatchingConfigurationFiles(project, "compileclasspath"));

        return List.copyOf(roots);
    }

    private static List<File> buildModulePathRoots(List<File> classpathRoots) {
        return classpathRoots.stream()
                .filter(DTOBuilder::isModulePathCandidate)
                .toList();
    }

    private static File findClassesDir(Project project, String sourceSetName) {
        if (!project.getPlugins().hasPlugin(JavaPlugin.class))
            return null;

        SourceSetContainer sourceSets = project.getExtensions().findByType(SourceSetContainer.class);
        if (sourceSets == null)
            return null;

        SourceSet sourceSet = sourceSets.findByName(sourceSetName);
        if (sourceSet == null)
            return null;

        return Optional.of(sourceSet.getOutput().getClassesDirs())
                .flatMap(fileCollection -> fileCollection.getFiles().stream().findFirst())
                .orElse(null);
    }

    private static RailroadJavaLanguageSettings buildJavaLanguageSettings(Project project) {
        JavaPluginExtension javaExtension = project.getExtensions().findByType(JavaPluginExtension.class);
        JavaVersion sourceCompatibility = javaExtension != null ? javaExtension.getSourceCompatibility() : null;
        JavaVersion targetCompatibility = javaExtension != null ? javaExtension.getTargetCompatibility() : null;
        InstalledJdk jdk = project.getExtensions().findByType(InstalledJdk.class);
        InstalledJdk installedJdk = jdk == null ? null : new BasicInstalledJdk(jdk.getJavaVersion(), jdk.getJavaHome());
        return new BasicRailroadJavaLanguageSettings(sourceCompatibility, targetCompatibility, installedJdk);
    }

    private static List<RailroadContentRoot> buildContentRoots(Project project) {
        if (!project.getPlugins().hasPlugin(JavaPlugin.class))
            return Collections.emptyList();

        SourceSetContainer sourceSets = project.getExtensions().findByType(SourceSetContainer.class);
        if (sourceSets == null)
            return Collections.emptyList();

        File buildDir = project.getLayout().getBuildDirectory().getAsFile().getOrNull();
        List<RailroadContentRoot> roots = new ArrayList<>();

        for (SourceSet sourceSet : sourceSets) {
            List<RailroadSourceDirectory> sources = toSourceDirectories(sourceSet.getAllSource().getSrcDirs(), buildDir, "source");
            List<RailroadSourceDirectory> testSources = toSourceDirectories(sourceSet.getAllJava().getSrcDirs(), buildDir, "testSource");
            List<RailroadSourceDirectory> resources = toSourceDirectories(sourceSet.getResources().getSrcDirs(), buildDir, "resource");
            List<RailroadSourceDirectory> testResources = toSourceDirectories(sourceSet.getResources().getSrcDirs(), buildDir, "testResource");
            Set<File> excluded = collectExcludedDirectories(project, sourceSet);

            roots.add(new BasicRailroadContentRoot(
                    project.getProjectDir(),
                    sources,
                    testSources,
                    resources,
                    testResources,
                    excluded
            ));
        }

        return roots;
    }

    private static Set<File> collectExcludedDirectories(Project project, SourceSet sourceSet) {
        Set<File> excluded = new LinkedHashSet<>();

        File buildDir = project.getLayout().getBuildDirectory().getAsFile().getOrNull();
        if (buildDir != null) {
            excluded.add(buildDir);
        }

        File gradleDir = new File(project.getProjectDir(), ".gradle");
        if (gradleDir.exists()) {
            excluded.add(gradleDir);
        }

        SourceSetOutput output = sourceSet.getOutput();
        excluded.addAll(output.getClassesDirs().getFiles());
        File resourcesOutput = output.getResourcesDir();
        if (resourcesOutput != null) {
            excluded.add(resourcesOutput);
        }

        return Collections.unmodifiableSet(excluded);
    }

    private static List<RailroadSourceDirectory> toSourceDirectories(Set<File> dirs, @Nullable File buildDir, String type) {
        return dirs.stream()
                .map(dir -> new BasicRailroadSourceDirectory(isGenerated(buildDir, dir), dir, type))
                .map(RailroadSourceDirectory.class::cast)
                .toList();
    }

    private static boolean isGenerated(@Nullable File buildDir, File dir) {
        if (buildDir == null)
            return false;

        Path buildPath = buildDir.toPath().toAbsolutePath().normalize();
        Path dirPath = dir.toPath().toAbsolutePath().normalize();
        if (!dirPath.startsWith(buildPath))
            return false;

        Path relative = buildPath.relativize(dirPath);
        for (Path segment : relative) {
            if ("generated".equalsIgnoreCase(segment.toString()))
                return true;
        }

        return false;
    }

    private static List<File> resolveMatchingConfigurationFiles(Project project, String configurationNameFragment) {
        LinkedHashSet<File> files = new LinkedHashSet<>();
        String fragment = configurationNameFragment.toLowerCase(Locale.ROOT);
        for (Configuration configuration : project.getConfigurations()) {
            if (!configuration.isCanBeResolved())
                continue;

            if (!configuration.getName().toLowerCase(Locale.ROOT).contains(fragment))
                continue;

            files.addAll(resolveConfigurationFiles(configuration));
        }

        return List.copyOf(files);
    }

    private static List<File> resolveConfigurationFiles(Configuration configuration) {
        try {
            LinkedHashSet<File> files = new LinkedHashSet<>();
            for (ResolvedArtifact artifact : configuration.getResolvedConfiguration().getResolvedArtifacts()) {
                File file = artifact.getFile();
                if (file != null && file.exists())
                    files.add(file);
            }

            return List.copyOf(files);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private static List<RailroadConfiguration> buildConfigurations(Project project, Supplier<RailroadModule> moduleSupplier) {
        List<RailroadConfiguration> configurations = new ArrayList<>();

        for (Configuration configuration : project.getConfigurations()) {
            try {
                RailroadModule moduleRef = moduleSupplier.get();
                List<RailroadDependency> dependencies = Collections.emptyList();
                if (configuration.isCanBeResolved()) {
                    Supplier<HierarchicalElement> configurationRefSupplier = () -> buildConfigurationReference(
                            configuration,
                            moduleRef
                    );
                    dependencies = collectDependencies(configurationRefSupplier, configuration);
                }
                configurations.add(new BasicRailroadConfiguration(
                        configuration.getName(),
                        configuration.getDescription(),
                        configuration.isCanBeResolved(),
                        configuration.isCanBeConsumed(),
                        configuration.isVisible(),
                        configuration.isTransitive(),
                        configuration.getExtendsFrom().stream().map(Configuration::getName).sorted().toList(),
                        collectConfigurationAttributes(configuration.getAttributes()),
                        moduleRef,
                        dependencies
                ));
            } catch (Exception ignored) {
                // Swallow individual configuration failures to mirror previous behaviour.
            }
        }

        return configurations;
    }

    private static List<RailroadDependency> collectDependencies(Supplier<HierarchicalElement> parentSupplier,
                                                                Configuration configuration) {
        Map<ModuleVersionIdentifier, ResolvedArtifact> artifactPaths = indexArtifacts(configuration);
        ResolutionResult resolutionResult = configuration.getIncoming().getResolutionResult();
        ResolvedComponentResult root = resolutionResult.getRoot();

        return root.getDependencies().stream()
                .filter(dependencyResult -> dependencyResult instanceof ResolvedDependencyResult)
                .map(ResolvedDependencyResult.class::cast)
                .map(result -> buildNode(parentSupplier.get(), result.getSelected(), artifactPaths, new HashSet<>()))
                .toList();
    }

    private static RailroadDependency buildNode(HierarchicalElement parent,
                                                ResolvedComponentResult component,
                                                Map<ModuleVersionIdentifier, ResolvedArtifact> artifactPaths,
                                                Set<ComponentIdentifier> visiting) {
        ComponentIdentifier identifier = component.getId();
        ModuleVersionIdentifier moduleVersion = component.getModuleVersion();

        if (!visiting.add(identifier)) {
            return new BasicRailroadDependency(
                    parent,
                    moduleVersion.getGroup(),
                    moduleVersion.getName(),
                    moduleVersion.getVersion(),
                    Optional.ofNullable(artifactPaths.get(moduleVersion)).map(ResolvedArtifact::getFile).orElse(new File("")),
                    identifier.getDisplayName(),
                    Collections.emptyList()
            );
        }

        List<RailroadDependency> children = new ArrayList<>();

        var dependencyRef = buildDependencyReference(
                parent,
                moduleVersion,
                artifactPaths,
                identifier
        );

        component.getDependencies().stream()
                .filter(dependencyResult -> dependencyResult instanceof ResolvedDependencyResult)
                .map(ResolvedDependencyResult.class::cast)
                .map(result -> buildNode(dependencyRef,
                        result.getSelected(),
                        artifactPaths,
                        visiting))
                .forEach(children::add);

        var dependency = new BasicRailroadDependency(
                parent,
                moduleVersion.getGroup(),
                moduleVersion.getName(),
                moduleVersion.getVersion(),
                Optional.ofNullable(artifactPaths.get(moduleVersion)).map(ResolvedArtifact::getFile).orElse(new File("")),
                identifier.getDisplayName(),
                children
        );

        visiting.remove(identifier);
        return dependency;
    }

    private static Map<ModuleVersionIdentifier, ResolvedArtifact> indexArtifacts(Configuration configuration) {
        Collection<ResolvedArtifact> artifacts = configuration.getResolvedConfiguration().getResolvedArtifacts();
        Map<ModuleVersionIdentifier, ResolvedArtifact> index = new HashMap<>(artifacts.size());

        for (ResolvedArtifact artifact : artifacts) {
            ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
            index.putIfAbsent(id, artifact);
        }

        return index;
    }

    private static List<RailroadGradleTask> buildTasks(Project project,
                                                       Supplier<RailroadModule> moduleSupplier,
                                                       GradleProject gradleProject,
                                                       List<GradleTask> gradleTasks) {
        List<RailroadGradleTask> tasks = new ArrayList<>();

        for (Task task : project.getTasks()) {
            RailroadModule moduleRef = moduleSupplier.get();
            var taskRef = new BasicRailroadGradleTask(
                    moduleRef,
                    task.getPath(),
                    task.getPath(),
                    task.getName(),
                    new DefaultProjectIdentifier(project.getRootDir(), project.getPath()),
                    task.getName(),
                    task.getDescription(),
                    task.getGroup() != null,
                    task.getGroup(),
                    gradleProject,
                    Collections.emptyList()
            );
            List<RailroadGradleTaskArgument> arguments = new ArrayList<>();
            var taskDto = new BasicRailroadGradleTask(
                    moduleRef,
                    task.getPath(),
                    task.getPath(),
                    task.getName(),
                    new DefaultProjectIdentifier(project.getRootDir(), project.getPath()),
                    task.getName(),
                    task.getDescription(),
                    task.getGroup() != null,
                    task.getGroup(),
                    gradleProject,
                    arguments
            );
            arguments.addAll(buildTaskArguments(() -> buildTaskReference(task, moduleRef, gradleProject), task));
            tasks.add(taskDto);
            gradleTasks.add(taskRef);
        }

        return tasks;
    }

    private static List<RailroadGradleTaskArgument> buildTaskArguments(Supplier<RailroadGradleTask> taskSupplier,
                                                                       Task task) {
        var optionReader = new OptionReader();
        Map<String, OptionDescriptor> options = optionReader.getOptions(task);
        List<RailroadGradleTaskArgument> arguments = new ArrayList<>(options.size());

        for (OptionDescriptor option : options.values()) {
            RailroadGradleTask taskDto = taskSupplier.get();
            arguments.add(new BasicRailroadGradleTaskArgument(
                    taskDto,
                    option.getName(),
                    option.getName(),
                    toArgumentType(option.getArgumentType()),
                    option.getArgumentType() != null ? option.getArgumentType().getName() : null,
                    option.getDescription(),
                    option.getAvailableValues(),
                    option.isClashing()
            ));
        }

        return arguments;
    }

    private static RailroadGradleTaskArgument.GradleTaskArgumentType toArgumentType(Class<?> argType) {
        if (argType == null)
            return RailroadGradleTaskArgument.GradleTaskArgumentType.UNKNOWN;

        if (argType == Boolean.class || argType == boolean.class) {
            return RailroadGradleTaskArgument.GradleTaskArgumentType.BOOLEAN;
        } else if (argType.isEnum()) {
            return RailroadGradleTaskArgument.GradleTaskArgumentType.ENUM;
        } else if (argType.isPrimitive() || Number.class.isAssignableFrom(argType)) {
            return RailroadGradleTaskArgument.GradleTaskArgumentType.NUMBER;
        } else if (argType == String.class) {
            return RailroadGradleTaskArgument.GradleTaskArgumentType.STRING;
        } else if (argType == File.class || argType == Path.class) {
            return RailroadGradleTaskArgument.GradleTaskArgumentType.FILE;
        } else {
            return RailroadGradleTaskArgument.GradleTaskArgumentType.UNKNOWN;
        }
    }

    private static @Nullable RailroadProject buildProjectReference(@Nullable Project project) {
        if (project == null)
            return null;

        return new BasicRailroadProject(
                project.getName(),
                project.getDescription(),
                buildProjectReference(project.getParent()),
                Collections.emptyList(),
                buildJavaLanguageSettings(project)
        );
    }

    private static @Nullable RailroadProject buildProjectReference(@Nullable RailroadProject project) {
        if (project == null)
            return null;

        RailroadProject parent = null;
        HierarchicalElement parentElement = project.getParent();
        if (parentElement instanceof RailroadProject parentProject) {
            parent = buildProjectReference(parentProject);
        }

        return new BasicRailroadProject(
                project.getName(),
                project.getDescription(),
                parent,
                Collections.emptyList(),
                project.javaLanguageSettings()
        );
    }

    private static BasicGradleProject buildGradleProjectReference(Project project,
                                                                  ProjectIdentifier projectIdentifier) {
        return new BasicGradleProject(
                project.getName(),
                project.getDescription(),
                project.getPath(),
                project.getProjectDir(),
                project.getLayout().getBuildDirectory().getAsFile().getOrNull(),
                new BasicGradleScriptAdapter(project.getBuildFile()),
                projectIdentifier,
                null,
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    private static RailroadModule buildModuleReference(Project project,
                                                       RailroadProject parentProject,
                                                       RailroadJavaLanguageSettings javaLanguageSettings,
                                                       RailroadCompilerOutput compilerOutput,
                                                       List<RailroadContentRoot> contentRoots,
                                                       GradleProject gradleProjectRef,
                                                       ProjectIdentifier projectIdentifier) {
        return new BasicRailroadModule(
                project.getName(),
                project.getDescription(),
                project.getPath(),
                project.getProjectDir(),
                parentProject,
                javaLanguageSettings,
                compilerOutput,
                contentRoots,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                gradleProjectRef,
                projectIdentifier
        );
    }

    private static RailroadConfiguration buildConfigurationReference(Configuration configuration,
                                                                     RailroadModule module) {
        return new BasicRailroadConfiguration(
                configuration.getName(),
                configuration.getDescription(),
                configuration.isCanBeResolved(),
                configuration.isCanBeConsumed(),
                configuration.isVisible(),
                configuration.isTransitive(),
                configuration.getExtendsFrom().stream().map(Configuration::getName).sorted().toList(),
                collectConfigurationAttributes(configuration.getAttributes()),
                module,
                Collections.emptyList()
        );
    }

    private static Map<String, String> collectConfigurationAttributes(AttributeContainer attributes) {
        Map<String, String> values = new TreeMap<>();
        for (Attribute<?> attribute : attributes.keySet()) {
            Object value = attributes.getAttribute(attribute);
            values.put(attribute.getName(), value == null ? "" : String.valueOf(value));
        }

        return values;
    }

    private static boolean isModulePathCandidate(File file) {
        if (file == null || !file.exists())
            return false;

        if (file.isDirectory())
            return new File(file, "module-info.class").isFile();

        String lowerName = file.getName().toLowerCase(Locale.ROOT);
        if (!lowerName.endsWith(".jar"))
            return false;

        try (JarFile jarFile = new JarFile(file)) {
            if (jarFile.getEntry("module-info.class") != null)
                return true;

            Manifest manifest = jarFile.getManifest();
            if (manifest == null)
                return false;

            String automaticModuleName = manifest.getMainAttributes().getValue("Automatic-Module-Name");
            return automaticModuleName != null && !automaticModuleName.isBlank();
        } catch (IOException ignored) {
            return false;
        }
    }

    private static RailroadGradleTask buildTaskReference(Task task,
                                                         RailroadModule module,
                                                         GradleProject gradleProject) {
        return new BasicRailroadGradleTask(
                module,
                task.getPath(),
                task.getPath(),
                task.getName(),
                new DefaultProjectIdentifier(task.getProject().getRootDir(), task.getProject().getPath()),
                task.getName(),
                task.getDescription(),
                task.getGroup() != null,
                task.getGroup(),
                gradleProject,
                Collections.emptyList()
        );
    }

    private static RailroadDependency buildDependencyReference(HierarchicalElement parent,
                                                               ModuleVersionIdentifier moduleVersion,
                                                               Map<ModuleVersionIdentifier, ResolvedArtifact> artifactPaths,
                                                               ComponentIdentifier identifier) {
        return new BasicRailroadDependency(
                parent,
                moduleVersion.getGroup(),
                moduleVersion.getName(),
                moduleVersion.getVersion(),
                Optional.ofNullable(artifactPaths.get(moduleVersion)).map(ResolvedArtifact::getFile).orElse(new File("")),
                identifier.getDisplayName(),
                Collections.emptyList()
        );
    }
}
