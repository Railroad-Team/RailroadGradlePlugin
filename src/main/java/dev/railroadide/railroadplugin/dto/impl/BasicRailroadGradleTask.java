package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadGradleTask;
import dev.railroadide.railroadplugin.dto.RailroadGradleTaskArgument;
import dev.railroadide.railroadplugin.dto.RailroadModule;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.ProjectIdentifier;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record BasicRailroadGradleTask(RailroadModule module,
                                      String path,
                                      String buildTreePath,
                                      String name,
                                      ProjectIdentifier projectIdentifier,
                                      String displayName,
                                      @Nullable String description,
                                      boolean isPublic,
                                      @Nullable String group,
                                      GradleProject project,
                                      List<RailroadGradleTaskArgument> arguments) implements RailroadGradleTask, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public RailroadModule module() {
        return module;
    }

    @Override
    public DomainObjectSet<? extends RailroadGradleTaskArgument> getArguments() {
        return ImmutableDomainObjectSet.of(arguments);
    }

    @Override
    public GradleProject getProject() {
        return project;
    }

    @Override
    public @Nullable HierarchicalElement getParent() {
        return module();
    }

    @Override
    public DomainObjectSet<? extends HierarchicalElement> getChildren() {
        return getArguments();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getBuildTreePath() {
        return buildTreePath;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ProjectIdentifier getProjectIdentifier() {
        return projectIdentifier;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public @Nullable String getGroup() {
        return group;
    }
}
