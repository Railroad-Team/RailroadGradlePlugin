package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadDependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class BasicRailroadDependency implements RailroadDependency {
    private final HierarchicalElement parent;
    private final ComponentIdentifier identifier;
    private final ModuleVersionIdentifier moduleVersion;
    private final ResolvedArtifact artifact;
    private final List<RailroadDependency> children;

    public BasicRailroadDependency(HierarchicalElement parent, ComponentIdentifier identifier, ModuleVersionIdentifier moduleVersion, ResolvedArtifact artifact, List<RailroadDependency> children) {
        this.parent = parent;
        this.identifier = identifier;
        this.moduleVersion = moduleVersion;
        this.artifact = artifact;
        this.children = children;
    }

    @Override
    public String getGroup() {
        return moduleVersion.getGroup();
    }

    @Override
    public String getName() {
        return moduleVersion.getName();
    }

    @Override
    public @Nullable String getDescription() {
        return identifier.getDisplayName();
    }

    @Override
    public String getVersion() {
        return moduleVersion.getVersion();
    }

    @Override
    public File getFile() {
        return artifact.getFile();
    }

    @Override
    public @Nullable HierarchicalElement getParent() {
        return parent;
    }

    @Override
    public DomainObjectSet<? extends RailroadDependency> getChildren() {
        return ImmutableDomainObjectSet.of(children);
    }

    @Override
    public boolean isTransitive() {
        return !children.isEmpty();
    }
}
