package dev.railroadide.railroadplugin.model;

import dev.railroadide.railroadplugin.dto.DTOBuilder;
import dev.railroadide.railroadplugin.dto.RailroadModule;
import dev.railroadide.railroadplugin.dto.RailroadProject;
import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Builds {@link RailroadProject} (the whole build, seen from the target project) and {@link RailroadModule} (just the
 * target project). Only the latter can resolve dependencies on Gradle 9+, because Gradle only locks the target project.
 */
public class BasicRailroadModelBuilder implements ToolingModelBuilder {
    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(RailroadProject.class.getName()) || modelName.equals(RailroadModule.class.getName());
    }

    @Override
    public @NotNull Object buildAll(String modelName, @NotNull Project project) {
        if (modelName.equals(RailroadProject.class.getName()))
            return DTOBuilder.buildProject(project);

        if (modelName.equals(RailroadModule.class.getName()))
            return DTOBuilder.buildModule(project);

        throw new IllegalArgumentException("Unsupported model: " + modelName);
    }
}
