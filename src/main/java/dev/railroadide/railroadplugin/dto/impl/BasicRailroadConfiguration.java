package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadConfiguration;
import dev.railroadide.railroadplugin.dto.RailroadDependency;
import dev.railroadide.railroadplugin.dto.RailroadModule;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record BasicRailroadConfiguration(String name,
                                         @Nullable String description,
                                         boolean canBeResolved,
                                         boolean canBeConsumed,
                                         boolean visible,
                                         boolean transitive,
                                         List<String> extendsFrom,
                                         Map<String, String> attributes,
                                         RailroadModule parent,
                                         List<RailroadDependency> dependencies) implements RailroadConfiguration, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public boolean isCanBeResolved() {
        return canBeResolved;
    }

    @Override
    public boolean isCanBeConsumed() {
        return canBeConsumed;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public List<String> getExtendsFrom() {
        return List.copyOf(extendsFrom);
    }

    @Override
    public Map<String, String> getAttributes() {
        return Map.copyOf(attributes);
    }

    @Override
    public DomainObjectSet<? extends RailroadDependency> getDependencies() {
        return ImmutableDomainObjectSet.of(dependencies);
    }

    @Override
    public @Nullable RailroadModule getParent() {
        return parent;
    }

    @Override
    public DomainObjectSet<? extends HierarchicalElement> getChildren() {
        return getDependencies();
    }
}
