package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadGradleTask;
import dev.railroadide.railroadplugin.dto.RailroadGradleTaskArgument;
import dev.railroadide.railroadplugin.dto.RailroadModule;
import org.gradle.tooling.model.*;
import org.jetbrains.annotations.Nullable;

public class BasicRailroadGradleTask implements RailroadGradleTask {
    private final RailroadModule module;
    private final GradleTask delegate;

    public BasicRailroadGradleTask(RailroadModule module, GradleTask delegate) {
        this.module = module;
        this.delegate = delegate;
    }

    @Override
    public RailroadModule module() {
        return module;
    }

    @Override
    public DomainObjectSet<? extends RailroadGradleTaskArgument> getArguments() {
        return null;
    }

    @Override
    public GradleProject getProject() {
        return delegate.getProject();
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
        return delegate.getPath();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public String getBuildTreePath() {
        return delegate.getBuildTreePath();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public ProjectIdentifier getProjectIdentifier() {
        return delegate.getProjectIdentifier();
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    @Override
    public @Nullable String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public boolean isPublic() {
        return delegate.isPublic();
    }

    @Override
    public @Nullable String getGroup() {
        return delegate.getGroup();
    }
}
