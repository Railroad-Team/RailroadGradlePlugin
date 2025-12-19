package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadDependency;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record BasicRailroadDependency(@Nullable HierarchicalElement parent,
                                      String group,
                                      String name,
                                      String version,
                                      File file,
                                      @Nullable String description,
                                      List<RailroadDependency> children) implements RailroadDependency, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public File getFile() {
        return file;
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
