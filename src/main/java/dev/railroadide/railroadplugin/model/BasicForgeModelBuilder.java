package dev.railroadide.railroadplugin.model;

import dev.railroadide.railroadplugin.dto.ForgeDataModel;
import dev.railroadide.railroadplugin.dto.impl.BasicForgeDataModel;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BasicForgeModelBuilder implements ToolingModelBuilder {
    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals("dev.railroadide.railroadplugin.model.BasicForgeModelBuilder");
    }

    @Override
    public @NotNull Object buildAll(String modelName, @NotNull Project project) {
        if (modelName.equals(ForgeDataModel.class.getName()))
            return buildForgeModel(project);

        throw new IllegalArgumentException("Unsupported model: " + modelName);
    }

    private static ForgeDataModel buildForgeModel(Project project) {
        List<ExternalModuleDependency> allDependencies = project.getConfigurations()
                .stream()
                .flatMap(config -> config.getAllDependencies().stream())
                .filter(ExternalModuleDependency.class::isInstance)
                .map(ExternalModuleDependency.class::cast)
                .distinct()
                .toList();

        String forgeVersionRaw = findDependencyVersion(allDependencies, "net.minecraftforge", "forge", false);
        String minecraftVersion = null;
        String forgeVersion;
        if (forgeVersionRaw != null && forgeVersionRaw.contains("-")) {
            String[] parts = forgeVersionRaw.split("-", 2);
            minecraftVersion = parts[0];
            forgeVersion = parts.length > 1 ? parts[1] : null;
        } else {
            forgeVersion = forgeVersionRaw;
        }

        String forgeGradleVersion = findBuildscriptDependencyVersion(project, "net.minecraftforge.gradle", "ForgeGradle");

        return new BasicForgeDataModel(
                minecraftVersion,
                forgeVersion,
                forgeGradleVersion
        );
    }

    private static String findBuildscriptDependencyVersion(Project project, String group, String name) {
        Dependency match = project.getBuildscript()
                .getConfigurations()
                .getByName("classpath")
                .getAllDependencies()
                .stream()
                .filter(dep -> Objects.equals(dep.getGroup(), group) && name.equals(dep.getName()))
                .findFirst()
                .orElse(null);
        return match != null ? match.getVersion() : null;
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
