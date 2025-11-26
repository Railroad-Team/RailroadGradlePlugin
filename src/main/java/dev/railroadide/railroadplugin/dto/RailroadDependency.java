package dev.railroadide.railroadplugin.dto;

import org.gradle.tooling.model.Dependency;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.HierarchicalElement;

import java.io.File;

public interface RailroadDependency extends HierarchicalElement, Dependency {
    String getGroup();

    String getName();

    String getVersion();

    File getFile();

    DomainObjectSet<? extends RailroadDependency> getChildren();

    boolean isTransitive();
}
