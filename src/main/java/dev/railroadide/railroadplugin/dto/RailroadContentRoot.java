package dev.railroadide.railroadplugin.dto;

import org.gradle.tooling.model.DomainObjectSet;

import java.io.File;
import java.util.Set;

public interface RailroadContentRoot {
    File getRootDirectory();

    DomainObjectSet<? extends RailroadSourceDirectory> getSourceDirectories();

    DomainObjectSet<? extends RailroadSourceDirectory> getTestSourceDirectories();

    DomainObjectSet<? extends RailroadSourceDirectory> getResourceDirectories();

    DomainObjectSet<? extends RailroadSourceDirectory> getTestResourceDirectories();

    Set<File> getExcludedDirectories();
}
