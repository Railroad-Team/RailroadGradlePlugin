package dev.railroadide.railroadplugin.model;

import dev.railroadide.railroadplugin.dto.FabricDataModel;
import dev.railroadide.railroadplugin.dto.impl.BasicFabricDataModel;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BasicFabricModelBuilder implements ToolingModelBuilder {
    private final String loomVersion;

    public BasicFabricModelBuilder(String loomVersion) {
        this.loomVersion = loomVersion;
    }

    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals("dev.railroadide.railroadplugin.model.BasicFabricModelBuilder");
    }

    @Override
    public @NotNull Object buildAll(String modelName, @NotNull Project project) {
        if (modelName.equals(FabricDataModel.class.getName()))
            return buildFabricModel(this.loomVersion, project);

        throw new IllegalArgumentException("Unsupported model: " + modelName);
    }

    private static FabricDataModel buildFabricModel(String pluginLoomVersion, Project project) {
        List<ExternalModuleDependency> allDependencies = project.getConfigurations()
                .stream()
                .flatMap(config -> config.getAllDependencies().stream())
                .filter(ExternalModuleDependency.class::isInstance)
                .map(ExternalModuleDependency.class::cast)
                .distinct()
                .toList();

        String minecraftVersion = findDependencyVersion(allDependencies, "com.mojang", "minecraft", false);
        String mappingsVersion = findDependencyVersion(allDependencies, "net.fabricmc", "yarn", true);
        String loaderVersion = findDependencyVersion(allDependencies, "net.fabricmc", "fabric-loader", false);
        String fabricApiVersion = findDependencyVersion(allDependencies, "net.fabricmc.fabric-api", "fabric-api", false);

        boolean hasLoomVersion = pluginLoomVersion != null && !pluginLoomVersion.isEmpty();
        String loomVersion = hasLoomVersion ?
                Objects.requireNonNull(pluginLoomVersion) :
                findDependencyVersion(allDependencies, "dev.architectury", "fabric-loom", false);
        boolean isArchitecturyLoom = loomVersion != null && loomVersion.contains("architectury");

        return new BasicFabricDataModel(
                minecraftVersion,
                mappingsVersion,
                loaderVersion,
                fabricApiVersion,
                new BasicFabricDataModel.BasicLoomVersion(loomVersion, isArchitecturyLoom)
        );
    }

    private static String findDependencyVersion(List<ExternalModuleDependency> dependencies, String group, String name, boolean includeClassifier) {
        Optional<ExternalModuleDependency> found = dependencies.stream()
                .filter(dep -> Objects.equals(dep.getGroup(), group) && dep.getName().equals(name))
                .findFirst();
        if (found.isEmpty())
            return null;

        ExternalModuleDependency dependency = found.get();
        String version = dependency.getVersion();
        if (includeClassifier && !dependency.getArtifacts().isEmpty()) {
            DependencyArtifact artifact = dependency.getArtifacts()
                    .stream()
                    .filter(art -> art.getClassifier() != null && !art.getClassifier().isEmpty())
                    .findFirst()
                    .orElse(null);
            if (artifact != null) {
                String classifier = artifact.getClassifier();
                version += "-" + classifier;
            }
        }

        return version;
    }
}