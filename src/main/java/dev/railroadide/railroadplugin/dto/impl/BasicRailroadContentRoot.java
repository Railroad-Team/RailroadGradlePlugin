package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadContentRoot;
import dev.railroadide.railroadplugin.dto.RailroadSourceDirectory;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BasicRailroadContentRoot implements RailroadContentRoot {
    private final Project project;
    private final SourceSet sourceSet;

    public BasicRailroadContentRoot(Project project, SourceSet sourceSet) {
        this.project = project;
        this.sourceSet = sourceSet;
    }

    public static DomainObjectSet<BasicRailroadContentRoot> empty() {
        return ImmutableDomainObjectSet.of(Collections.emptyList());
    }

    @Override
    public File getRootDirectory() {
        return project.getProjectDir();
    }

    @Override
    public DomainObjectSet<? extends RailroadSourceDirectory> getSourceDirectories() {
        return toSourceDirectories(
                sourceSet.getAllSource().getSrcDirs(),
                this::isGenerated,
                "source"
        );
    }

    @Override
    public DomainObjectSet<? extends RailroadSourceDirectory> getTestSourceDirectories() {
        return toSourceDirectories(
                sourceSet.getAllJava().getSrcDirs(),
                this::isGenerated,
                "testSource"
        );
    }

    @Override
    public DomainObjectSet<? extends RailroadSourceDirectory> getResourceDirectories() {
        return toSourceDirectories(
                sourceSet.getResources().getSrcDirs(),
                this::isGenerated,
                "resource"
        );
    }

    @Override
    public DomainObjectSet<? extends RailroadSourceDirectory> getTestResourceDirectories() {
        return toSourceDirectories(
                sourceSet.getResources().getSrcDirs(),
                this::isGenerated,
                "testResource"
        );
    }

    @Override
    public Set<File> getExcludedDirectories() {
        Set<File> excluded = new LinkedHashSet<>();

        File buildDir = project.getLayout().getBuildDirectory().getAsFile().getOrNull();
        if (buildDir != null) {
            excluded.add(buildDir);
        }

        File gradleDir = new File(project.getProjectDir(), ".gradle");
        if (gradleDir.exists()) {
            excluded.add(gradleDir);
        }

        excluded.addAll(sourceSet.getOutput().getClassesDirs().getFiles());
        File resourcesOutput = sourceSet.getOutput().getResourcesDir();
        if (resourcesOutput != null) {
            excluded.add(resourcesOutput);
        }

        return Collections.unmodifiableSet(excluded);
    }

    private static DomainObjectSet<BasicRailroadSourceDirectory> toSourceDirectories(Set<File> dirs, java.util.function.Predicate<File> generated, String type) {
        List<BasicRailroadSourceDirectory> sources = dirs.stream()
                .map(dir -> new BasicRailroadSourceDirectory(generated.test(dir), dir, type))
                .collect(Collectors.toList());
        return ImmutableDomainObjectSet.of(sources);
    }

    private boolean isGenerated(File dir) {
        File buildDir = project.getLayout().getBuildDirectory().getAsFile().getOrNull();
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
}
