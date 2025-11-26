package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.*;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.tooling.internal.gradle.DefaultProjectIdentifier;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.ProjectIdentifier;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BasicRailroadModule implements RailroadModule {
    private final Project project;
    private final BasicRailroadProject parent;

    public BasicRailroadModule(Project project, BasicRailroadProject parent) {
        this.project = project;
        this.parent = parent;
    }

    @Override
    public @Nullable RailroadJavaLanguageSettings getJavaLanguageSettings() {
        return new BasicRailroadJavaLanguageSettings(project);
    }

    @Override
    public DomainObjectSet<? extends RailroadContentRoot> getContentRoots() {
        if (!project.getPlugins().hasPlugin(JavaPlugin.class))
            return BasicRailroadContentRoot.empty();

        SourceSetContainer sourceSets = project.getExtensions().findByType(SourceSetContainer.class);
        if (sourceSets == null)
            return BasicRailroadContentRoot.empty();

        return ImmutableDomainObjectSet.of(
                sourceSets.stream()
                        .map(sourceSet -> new BasicRailroadContentRoot(project, sourceSet))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public ProjectIdentifier getProjectIdentifier() {
        return new DefaultProjectIdentifier(project.getRootDir(), project.getPath());
    }

    @Override
    public GradleProject getGradleProject() {
        return new BasicGradleProjectAdapter(project);
    }

    @Override
    public RailroadProject getParent() {
        return parent;
    }

    @Override
    public DomainObjectSet<? extends HierarchicalElement> getChildren() {
        return getConfigurations();
    }

    @Override
    public RailroadProject getProject() {
        return parent;
    }

    @Override
    public RailroadCompilerOutput getCompilerOutput() {
        return new BasicRailroadCompilerOutput(project);
    }

    @Override
    public DomainObjectSet<? extends RailroadConfiguration> getConfigurations() {
        return ImmutableDomainObjectSet.of(collectConfigurations(this, project));
    }

    public static List<RailroadConfiguration> collectConfigurations(RailroadModule module, Project project) {
        List<RailroadConfiguration> configurations = new ArrayList<>();

        for (Configuration configuration : project.getConfigurations()) {
            if (!configuration.isCanBeResolved())
                continue;

            try {
                configurations.add(new BasicRailroadConfiguration(module, configuration));
            } catch (Exception exception) {
                // TODO: LOGGER.warn("Failed to build dependency tree for configuration: {}", configuration.getName(), exception);
            }
        }

        return Collections.unmodifiableList(configurations);
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
