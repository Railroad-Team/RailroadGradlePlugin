package dev.railroadide.railroadplugin.dto;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.HierarchicalElement;

public interface RailroadProject extends HierarchicalElement {
    RailroadJavaLanguageSettings getJavaLanguageSettings();

    DomainObjectSet<? extends RailroadModule> getChildren();

    DomainObjectSet<? extends RailroadModule> getModules();

    boolean hasPlugin(String pluginId);
}
