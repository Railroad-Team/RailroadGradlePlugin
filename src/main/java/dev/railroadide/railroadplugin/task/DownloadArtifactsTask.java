package dev.railroadide.railroadplugin.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.ArtifactResolutionResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.artifacts.result.UnresolvedArtifactResult;
import org.gradle.api.component.Artifact;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.JvmLibrary;
import org.gradle.language.base.artifact.SourcesArtifact;
import org.gradle.language.java.artifact.JavadocArtifact;

import javax.inject.Inject;
import java.util.*;

public abstract class DownloadArtifactsTask extends DefaultTask {
    private static final Logger LOGGER = Logging.getLogger(DownloadArtifactsTask.class);

    @Inject
    public DownloadArtifactsTask() {
    }

    @Input
    public abstract Property<Boolean> getDownloadSources();

    @Input
    public abstract Property<Boolean> getDownloadJavadoc();

    @Input
    public abstract SetProperty<String> getConfigurations();

    @Input
    public abstract Property<Boolean> getLogResolvedFiles();

    @TaskAction
    public void download() {
        List<Class<? extends Artifact>> artifactTypes = new ArrayList<>();
        if (getDownloadSources().get()) {
            artifactTypes.add(SourcesArtifact.class);
        }

        if (getDownloadJavadoc().get()) {
            artifactTypes.add(JavadocArtifact.class);
        }

        if (artifactTypes.isEmpty()) {
            LOGGER.lifecycle("No artifacts requested (sources/javadoc disabled); skipping.");
            setDidWork(false);
            return;
        }

        Set<ModuleComponentIdentifier> componentIds = collectResolvableComponentIds(
                getConfigurations().getOrElse(Collections.emptySet()));
        if (componentIds.isEmpty()) {
            LOGGER.lifecycle("No external modules found in resolvable configurations.");
            setDidWork(false);
            return;
        }

        @SuppressWarnings({"UnstableApiUsage", "unchecked"})
        ArtifactResolutionResult result = getProject().getDependencies()
                .createArtifactResolutionQuery()
                .forComponents(componentIds)
                .withArtifacts(JvmLibrary.class, artifactTypes.toArray(new Class[0]))
                .execute();

        report(result);
    }

    private Set<ModuleComponentIdentifier> collectResolvableComponentIds(Set<String> configurationNames) {
        Set<ModuleComponentIdentifier> moduleIds = new LinkedHashSet<>();
        getProject().getConfigurations()
                .stream()
                .filter(Configuration::isCanBeResolved)
                .filter(configuration -> configurationNames.isEmpty() || configurationNames.contains(configuration.getName()))
                .filter(configuration -> !configuration.isCanBeConsumed())
                .forEach(configuration -> configuration.getIncoming().getResolutionResult().getAllComponents().forEach(component -> {
                    if (component.getId() instanceof ModuleComponentIdentifier id) {
                        moduleIds.add(id);
                    }
                }));
        return moduleIds;
    }

    private void report(ArtifactResolutionResult result) {
        int sourcesResolved = 0;
        int javadocResolved = 0;
        int unresolved = 0;

        for (ComponentArtifactsResult component : result.getResolvedComponents()) {
            if (getDownloadSources().get()) {
                Count resolved = reportArtifacts(component, SourcesArtifact.class);
                sourcesResolved += resolved.resolved();
                unresolved += resolved.unresolved();
            }
            if (getDownloadJavadoc().get()) {
                Count resolved = reportArtifacts(component, JavadocArtifact.class);
                javadocResolved += resolved.resolved();
                unresolved += resolved.unresolved();
            }
        }

        LOGGER.lifecycle(
                "Sources downloaded: {}, Javadoc downloaded: {}, unresolved: {}",
                sourcesResolved,
                javadocResolved,
                unresolved
        );
    }

    private Count reportArtifacts(ComponentArtifactsResult component, Class<? extends Artifact> type) {
        int resolvedCount = 0;
        int unresolvedCount = 0;
        for (var artifact : component.getArtifacts(type)) {
            if (artifact instanceof ResolvedArtifactResult resolved) {
                resolvedCount++;
                if (getLogResolvedFiles().get()) {
                    LOGGER.info("Resolved {} for {} -> {}", type.getSimpleName(), component.getId(), resolved.getFile());
                } else {
                    LOGGER.debug("Resolved {} for {} -> {}", type.getSimpleName(), component.getId(), resolved.getFile());
                }
            } else if (artifact instanceof UnresolvedArtifactResult unresolved) {
                unresolvedCount++;
                LOGGER.warn("Failed to resolve {} for {}: {}", type.getSimpleName(), component.getId(), unresolved.getFailure().getMessage());
                LOGGER.debug("Resolution failure for {}: {}", component.getId(), unresolved.getFailure().toString(), unresolved.getFailure());
            } else {
                LOGGER.warn("Unexpected artifact result {} for {}", artifact.getClass().getSimpleName(), component.getId());
            }
        }

        return new Count(resolvedCount, unresolvedCount);
    }

    private record Count(int resolved, int unresolved) {
    }
}
