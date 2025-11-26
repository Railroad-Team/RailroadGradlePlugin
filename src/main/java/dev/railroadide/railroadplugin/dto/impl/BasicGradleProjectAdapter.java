package dev.railroadide.railroadplugin.dto.impl;

import org.gradle.api.Project;
import org.gradle.tooling.internal.gradle.DefaultProjectIdentifier;
import org.gradle.tooling.model.*;
import org.gradle.tooling.model.gradle.GradleScript;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Minimal adapter to expose a {@link GradleProject} view of a {@link Project}.
 */
public class BasicGradleProjectAdapter implements GradleProject {
    private final Project project;
    private final @Nullable BasicGradleProjectAdapter parent;

    public BasicGradleProjectAdapter(Project project) {
        this(project, null);
    }

    private BasicGradleProjectAdapter(Project project, @Nullable BasicGradleProjectAdapter parent) {
        this.project = project;
        this.parent = parent;
    }

    @Override
    public ProjectIdentifier getProjectIdentifier() {
        return new DefaultProjectIdentifier(project.getRootDir(), project.getPath());
    }

    @Override
    public DomainObjectSet<? extends GradleTask> getTasks() {
        return project.getTasks()
                .stream()
                .map(task -> new BasicGradleTaskAdapter(project, task, this))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        ImmutableDomainObjectSet::of
                ));
    }

    @Override
    public @Nullable GradleProject getParent() {
        return parent;
    }

    @Override
    public DomainObjectSet<? extends GradleProject> getChildren() {
        return project.getChildProjects()
                .values()
                .stream()
                .map(child -> new BasicGradleProjectAdapter(child, this))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        ImmutableDomainObjectSet::of
                ));
    }

    @Override
    public String getPath() {
        return project.getPath();
    }

    @Override
    public GradleProject findByPath(String path) {
        if (Objects.equals(getPath(), path))
            return this;

        for (GradleProject child : getChildren()) {
            GradleProject match = child.findByPath(path);
            if (match != null)
                return match;
        }

        return null;
    }

    @Override
    public GradleScript getBuildScript() throws UnsupportedMethodException {
        return new BasicGradleScriptAdapter(project.getBuildFile());
    }

    @Override
    public File getBuildDirectory() {
        return project.getLayout().getBuildDirectory().getAsFile().getOrNull();
    }

    @Override
    public File getProjectDirectory() {
        return project.getProjectDir();
    }

    @Override
    public String getName() {
        return project.getName();
    }

    @Override
    public @Nullable String getDescription() {
        return project.getDescription();
    }
}
