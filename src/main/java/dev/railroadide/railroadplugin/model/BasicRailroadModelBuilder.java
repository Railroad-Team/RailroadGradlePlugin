package dev.railroadide.railroadplugin.model;

import dev.railroadide.railroadplugin.dto.RailroadProject;
import dev.railroadide.railroadplugin.dto.impl.BasicRailroadProject;
import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.jetbrains.annotations.NotNull;

public class BasicRailroadModelBuilder implements ToolingModelBuilder {
    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals("dev.railroadide.railroadplugin.model.BasicRailroadProject");
    }

    @Override
    public @NotNull Object buildAll(String modelName, @NotNull Project project) {
        if (modelName.equals(RailroadProject.class.getName()))
            return new BasicRailroadProject(project);

        throw new IllegalArgumentException("Unsupported model: " + modelName);
    }
}