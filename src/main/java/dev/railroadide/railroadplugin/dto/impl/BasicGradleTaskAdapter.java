package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadGradleTask;
import dev.railroadide.railroadplugin.dto.RailroadGradleTaskArgument;
import dev.railroadide.railroadplugin.dto.RailroadModule;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.tasks.options.OptionDescriptor;
import org.gradle.api.internal.tasks.options.OptionReader;
import org.gradle.tooling.internal.gradle.DefaultProjectIdentifier;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.ProjectIdentifier;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Minimal adapter to present a Gradle task via the tooling API model.
 */
public record BasicGradleTaskAdapter(RailroadModule module, Project project, Task delegate,
                                     GradleProject gradleProject) implements RailroadGradleTask {
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

    @Override
    public DomainObjectSet<? extends RailroadGradleTaskArgument> getArguments() {
        var optionReader = new OptionReader();
        Map<String, OptionDescriptor> options = optionReader.getOptions(this.delegate);
        return options.values().stream()
                .map(option -> new BasicRailroadGradleTaskArgument(this, option))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        ImmutableDomainObjectSet::of
                ));
    }

    @Override
    public @Nullable HierarchicalElement getParent() {
        return module();
    }

    @Override
    public DomainObjectSet<? extends HierarchicalElement> getChildren() {
        return getArguments();
    }
}
