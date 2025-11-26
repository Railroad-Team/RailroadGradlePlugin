package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadCompilerOutput;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Optional;

public class BasicRailroadCompilerOutput implements RailroadCompilerOutput {
    private final Project project;

    public BasicRailroadCompilerOutput(Project project) {
        this.project = project;
    }

    @Override
    public boolean inheritsOutputDirectories() {
        return false;
    }

    @Override
    public @Nullable File getOutputDirectory() {
        return findClassesDir(SourceSet.MAIN_SOURCE_SET_NAME);
    }

    @Override
    public @Nullable File getTestOutputDirectory() {
        return findClassesDir(SourceSet.TEST_SOURCE_SET_NAME);
    }

    private @Nullable File findClassesDir(String sourceSetName) {
        if (!project.getPlugins().hasPlugin(JavaPlugin.class))
            return null;

        SourceSetContainer sourceSets = project.getExtensions().findByType(SourceSetContainer.class);
        if (sourceSets == null)
            return null;

        SourceSet sourceSet = sourceSets.findByName(sourceSetName);
        if (sourceSet == null)
            return null;

        return Optional.of(sourceSet.getOutput().getClassesDirs())
                .flatMap(fileCollection -> fileCollection.getFiles().stream().findFirst())
                .orElse(null);
    }
}
