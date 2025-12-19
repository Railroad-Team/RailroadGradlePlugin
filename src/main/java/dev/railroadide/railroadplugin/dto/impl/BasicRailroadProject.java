package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadJavaLanguageSettings;
import dev.railroadide.railroadplugin.dto.RailroadModule;
import dev.railroadide.railroadplugin.dto.RailroadProject;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record BasicRailroadProject(String name, @Nullable String description, @Nullable RailroadProject parent,
                                   List<RailroadModule> modules,
                                   RailroadJavaLanguageSettings javaLanguageSettings) implements RailroadProject, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public BasicRailroadProject(String name,
                                @Nullable String description,
                                @Nullable RailroadProject parent,
                                List<RailroadModule> modules,
                                RailroadJavaLanguageSettings javaLanguageSettings) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.modules = modules;
        this.javaLanguageSettings = javaLanguageSettings;
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
    public @Nullable HierarchicalElement getParent() {
        return parent;
    }

    @Override
    public DomainObjectSet<? extends RailroadModule> getModules() {
        return ImmutableDomainObjectSet.of(modules);
    }

    @Override
    public DomainObjectSet<? extends RailroadModule> getChildren() {
        return getModules();
    }
}
