package dev.railroadide.railroadplugin.dto.impl;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.tooling.internal.gradle.DefaultProjectIdentifier;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.ProjectIdentifier;
import org.jetbrains.annotations.Nullable;

/**
 * Minimal adapter to present a Gradle task via the tooling API model.
 */
public class BasicGradleTaskAdapter implements GradleTask {
    private final Project project;
    private final Task delegate;
    private final GradleProject gradleProject;

    public BasicGradleTaskAdapter(Project project, Task delegate, GradleProject gradleProject) {
        this.project = project;
        this.delegate = delegate;
        this.gradleProject = gradleProject;
    }

    @Override
    public ProjectIdentifier getProjectIdentifier() {
        return new DefaultProjectIdentifier(project.getRootDir(), project.getPath());
    }

    @Override
    public String getDisplayName() {
        return delegate.getName();
    }

    @Override
    public @Nullable String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public boolean isPublic() {
        return delegate.getGroup() != null;
    }

    @Override
    public String getPath() {
        return delegate.getPath();
    }

    @Override
    public String getBuildTreePath() {
        return delegate.getPath();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public @Nullable String getGroup() {
        return delegate.getGroup();
    }

    @Override
    public GradleProject getProject() {
        return gradleProject;
    }
}
