package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadJavaLanguageSettings;
import dev.railroadide.railroadplugin.dto.RailroadModule;
import dev.railroadide.railroadplugin.dto.RailroadProject;
import org.gradle.api.Project;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class BasicRailroadProject implements RailroadProject {
    private final Project project;

    public BasicRailroadProject(Project project) {
        this.project = project;
    }

    @Override
    public RailroadJavaLanguageSettings getJavaLanguageSettings() {
        return new BasicRailroadJavaLanguageSettings(project);
    }

    @Override
    public @Nullable HierarchicalElement getParent() {
        return project.getParent() != null ? new BasicRailroadProject(project.getParent()) : null;
    }

    @Override
    public DomainObjectSet<? extends RailroadModule> getChildren() {
        return getModules();
    }

    @Override
    public DomainObjectSet<? extends RailroadModule> getModules() {
        return project.getSubprojects()
                .stream()
                .map(project -> new BasicRailroadModule(project, this))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        ImmutableDomainObjectSet::of
                ));
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
