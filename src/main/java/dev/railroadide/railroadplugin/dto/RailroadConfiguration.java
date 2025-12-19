package dev.railroadide.railroadplugin.dto;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.HierarchicalElement;

import javax.annotation.Nullable;

public interface RailroadConfiguration extends HierarchicalElement {
    String getName();

    DomainObjectSet<? extends RailroadDependency> getDependencies();

    @Nullable
    RailroadModule getParent();
}
