package dev.railroadide.railroadplugin;

import dev.railroadide.railroadplugin.extension.RailroadDownloadSourcesExtension;
import dev.railroadide.railroadplugin.model.BasicFabricModelBuilder;
import dev.railroadide.railroadplugin.model.BasicForgeModelBuilder;
import dev.railroadide.railroadplugin.model.BasicRailroadModelBuilder;
import dev.railroadide.railroadplugin.task.DownloadArtifactsTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import javax.inject.Inject;

public class RailroadProjectPlugin implements Plugin<Project> {
    private static final String DOWNLOAD_SOURCES_TASK_GROUP = "Documentation";
    private final ToolingModelBuilderRegistry registry;

    @Inject
    public RailroadProjectPlugin(ToolingModelBuilderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void apply(Project project) {
        project.getLogger().lifecycle("Railroad plugin applied to project '{}'", project.getName());

        registry.register(new BasicRailroadModelBuilder());
        registry.register(new BasicFabricModelBuilder(RailroadSettingsPlugin.loomVersion));
        registry.register(new BasicForgeModelBuilder());

        registerTasks(project);
    }

    private void registerTasks(Project project) {
        RailroadDownloadSourcesExtension extension = project.getExtensions().create(
                RailroadDownloadSourcesExtension.EXTENSION_NAME,
                RailroadDownloadSourcesExtension.class,
                project.getObjects()
        );

        var downloadSources = project.getTasks().register(
                "downloadDependencySources",
                DownloadArtifactsTask.class,
                task -> {
                    task.setDescription("Downloads sources for all resolvable dependency configurations.");
                    task.setGroup(DOWNLOAD_SOURCES_TASK_GROUP);
                    task.getDownloadSources().set(extension.getDownloadSources());
                    task.getDownloadJavadoc().set(false);
                    task.getConfigurations().set(extension.getConfigurations());
                    task.getLogResolvedFiles().set(extension.getLogResolvedFiles());
                    task.onlyIf(t -> task.getDownloadSources().get());
                }
        );

        var downloadJavadoc = project.getTasks().register(
                "downloadDependencyJavadoc",
                DownloadArtifactsTask.class,
                task -> {
                    task.setDescription("Downloads Javadoc for all resolvable dependency configurations.");
                    task.setGroup(DOWNLOAD_SOURCES_TASK_GROUP);
                    task.getDownloadSources().set(false);
                    task.getDownloadJavadoc().set(extension.getDownloadJavadoc());
                    task.getConfigurations().set(extension.getConfigurations());
                    task.getLogResolvedFiles().set(extension.getLogResolvedFiles());
                    task.onlyIf(t -> task.getDownloadJavadoc().get());
                }
        );

        project.getTasks().register(
                "downloadDependencyDocumentation",
                DownloadArtifactsTask.class,
                task -> {
                    task.setDescription("Downloads sources and Javadoc for all resolvable dependency configurations.");
                    task.setGroup(DOWNLOAD_SOURCES_TASK_GROUP);
                    task.getDownloadSources().set(extension.getDownloadSources());
                    task.getDownloadJavadoc().set(extension.getDownloadJavadoc());
                    task.getConfigurations().set(extension.getConfigurations());
                    task.getLogResolvedFiles().set(extension.getLogResolvedFiles());
                    task.mustRunAfter(downloadSources, downloadJavadoc);
                    task.onlyIf(t -> task.getDownloadSources().get() || task.getDownloadJavadoc().get());
                }
        );
    }
}
