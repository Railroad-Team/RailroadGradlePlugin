package dev.railroadide.railroadplugin.dto;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.HasGradleProject;
import org.gradle.tooling.model.HierarchicalElement;
import org.jetbrains.annotations.Nullable;

public interface RailroadModule extends HierarchicalElement, HasGradleProject {
    @Nullable
    RailroadJavaLanguageSettings getJavaLanguageSettings();
    DomainObjectSet<? extends RailroadContentRoot> getContentRoots();
    GradleProject getGradleProject();
    RailroadProject getParent();
    RailroadProject getProject();
    RailroadCompilerOutput getCompilerOutput();
    DomainObjectSet<? extends RailroadConfiguration> getConfigurations();
    DomainObjectSet<? extends RailroadGradleTask> getTasks();
}
