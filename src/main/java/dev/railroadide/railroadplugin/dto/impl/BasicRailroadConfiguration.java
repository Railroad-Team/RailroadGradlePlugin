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

public record BasicRailroadConfiguration(String name,
                                         @Nullable String description,
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
