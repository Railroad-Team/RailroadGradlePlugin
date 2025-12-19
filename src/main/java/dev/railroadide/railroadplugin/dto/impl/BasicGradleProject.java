package dev.railroadide.railroadplugin.dto.impl;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.ProjectIdentifier;
import org.gradle.tooling.model.UnsupportedMethodException;
import org.gradle.tooling.model.gradle.GradleScript;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public record BasicGradleProject(String name,
                                 @Nullable String description,
                                 String path,
                                 File projectDirectory,
                                 @Nullable File buildDirectory,
                                 GradleScript buildScript,
                                 ProjectIdentifier projectIdentifier,
                                 @Nullable GradleProject parent,
                                 List<GradleProject> children,
                                 List<GradleTask> tasks) implements GradleProject, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public ProjectIdentifier getProjectIdentifier() {
        return projectIdentifier;
    }

    @Override
    public DomainObjectSet<? extends GradleTask> getTasks() {
        return ImmutableDomainObjectSet.of(tasks);
    }

    @Override
    public @Nullable GradleProject getParent() {
        return parent;
    }

    @Override
    public DomainObjectSet<? extends GradleProject> getChildren() {
        return ImmutableDomainObjectSet.of(children);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public GradleProject findByPath(String searchPath) {
        if (Objects.equals(getPath(), searchPath))
            return this;

        for (GradleProject child : children) {
            GradleProject match = child.findByPath(searchPath);
            if (match != null)
                return match;
        }

        return null;
    }

    @Override
    public GradleScript getBuildScript() throws UnsupportedMethodException {
        return buildScript;
    }

    @Override
    public File getBuildDirectory() {
        return buildDirectory;
    }

    @Override
    public File getProjectDirectory() {
        return projectDirectory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }
}
