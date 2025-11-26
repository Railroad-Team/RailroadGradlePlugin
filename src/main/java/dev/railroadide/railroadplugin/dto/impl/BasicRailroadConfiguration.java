package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadConfiguration;
import dev.railroadide.railroadplugin.dto.RailroadDependency;
import dev.railroadide.railroadplugin.dto.RailroadModule;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BasicRailroadConfiguration implements RailroadConfiguration {
    private final RailroadModule parent;
    private final Configuration configuration;

    public BasicRailroadConfiguration(RailroadModule parent, Configuration configuration) {
        this.parent = parent;
        this.configuration = configuration;
    }

    @Override
    public String getName() {
        return configuration.getName();
    }

    @Override
    public @Nullable String getDescription() {
        return configuration.getDescription();
    }

    @Override
    public DomainObjectSet<? extends RailroadDependency> getDependencies() {
        return ImmutableDomainObjectSet.of(collectDependencies(this, configuration));
    }

    private static List<RailroadDependency> collectDependencies(HierarchicalElement parent, Configuration configuration) {
        Map<ModuleVersionIdentifier, ResolvedArtifact> artifactPaths = indexArtifacts(configuration);
        ResolutionResult resolutionResult = configuration.getIncoming().getResolutionResult();
        ResolvedComponentResult root = resolutionResult.getRoot();

        return root.getDependencies().stream()
                .filter(dependencyResult -> dependencyResult instanceof ResolvedDependencyResult)
                .map(ResolvedDependencyResult.class::cast)
                .map(result -> buildNode(parent, result.getSelected(), artifactPaths, new HashSet<>()))
                .toList();
    }

    private static RailroadDependency buildNode(HierarchicalElement parent,
                                                ResolvedComponentResult component,
                                                Map<ModuleVersionIdentifier, ResolvedArtifact> artifactPaths,
                                                Set<ComponentIdentifier> visiting) {
        ComponentIdentifier identifier = component.getId();
        ModuleVersionIdentifier moduleVersion = component.getModuleVersion();

        if (!visiting.add(identifier)) {
            // Cycle detected; return the node without expanding further.
            return new BasicRailroadDependency(
                    parent,
                    identifier,
                    moduleVersion,
                    artifactPaths.get(moduleVersion),
                    Collections.emptyList()
            );
        }

        List<RailroadDependency> children = new ArrayList<>();

        visiting.remove(identifier);

        var dependency = new BasicRailroadDependency(
                parent,
                identifier,
                moduleVersion,
                artifactPaths.get(moduleVersion),
                children
        );

        component.getDependencies().stream()
                .filter(dependencyResult -> dependencyResult instanceof ResolvedDependencyResult)
                .map(ResolvedDependencyResult.class::cast)
                .map(result -> buildNode(dependency, result.getSelected(), artifactPaths, visiting))
                .forEach(children::add);
        return dependency;
    }

    private static Map<ModuleVersionIdentifier, ResolvedArtifact> indexArtifacts(Configuration configuration) {
        Collection<ResolvedArtifact> artifacts = configuration.getResolvedConfiguration().getResolvedArtifacts();
        Map<ModuleVersionIdentifier, ResolvedArtifact> index = new HashMap<>(artifacts.size());

        for (ResolvedArtifact artifact : artifacts) {
            ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
            index.putIfAbsent(id, artifact);
        }

        return index;
    }

    @Override
    public @Nullable HierarchicalElement getParent() {
        return this.parent;
    }

    @Override
    public DomainObjectSet<? extends HierarchicalElement> getChildren() {
        return getDependencies();
    }
}
