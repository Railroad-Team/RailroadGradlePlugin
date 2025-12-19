package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.*;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.ProjectIdentifier;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record BasicRailroadModule(String name,
                                  @Nullable String description,
                                  String path,
                                  File projectDir,
                                  RailroadProject parent,
                                  RailroadJavaLanguageSettings javaLanguageSettings,
                                  RailroadCompilerOutput compilerOutput,
                                  List<RailroadContentRoot> contentRoots,
                                  List<RailroadConfiguration> configurations,
                                  List<RailroadGradleTask> tasks,
                                  GradleProject gradleProject,
                                  ProjectIdentifier projectIdentifier) implements RailroadModule, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public RailroadJavaLanguageSettings getJavaLanguageSettings() {
        return javaLanguageSettings;
    }

    @Override
    public DomainObjectSet<? extends RailroadContentRoot> getContentRoots() {
        return ImmutableDomainObjectSet.of(contentRoots);
    }

    @Override
    public ProjectIdentifier getProjectIdentifier() {
        return projectIdentifier;
    }

    @Override
    public GradleProject getGradleProject() {
        return gradleProject;
    }

    @Override
    public RailroadProject getParent() {
        return parent;
    }

    @Override
    public DomainObjectSet<? extends HierarchicalElement> getChildren() {
        List<HierarchicalElement> children = new ArrayList<>();
        children.addAll(configurations);
        children.addAll(tasks);
        return ImmutableDomainObjectSet.of(children);
    }

    @Override
    public DomainObjectSet<? extends RailroadGradleTask> getTasks() {
        return ImmutableDomainObjectSet.of(tasks);
    }

    @Override
    public RailroadProject getProject() {
        return parent;
    }

    @Override
    public RailroadCompilerOutput getCompilerOutput() {
        return compilerOutput;
    }

    @Override
    public DomainObjectSet<? extends RailroadConfiguration> getConfigurations() {
        return ImmutableDomainObjectSet.of(configurations);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Path getProjectDir() {
        return projectDir.toPath();
    }
}
