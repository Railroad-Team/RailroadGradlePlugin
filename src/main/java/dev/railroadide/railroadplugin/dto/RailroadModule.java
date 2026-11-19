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

    /**
     * Problems hit while resolving this module's configurations and classpaths, one human-readable line each.
     * An empty list means resolution succeeded, so empty roots really mean "no dependencies".
     */
    List<String> getResolutionFailures();
    String getPath();
    Path getProjectDir();
}
