package dev.railroadide.railroadplugin.dto;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.HasGradleProject;
import org.gradle.tooling.model.HierarchicalElement;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

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
    List<File> getDependencyRoots();
    List<File> getClasspathRoots();
    List<File> getModulePathRoots();
    String getPath();
    Path getProjectDir();
}
