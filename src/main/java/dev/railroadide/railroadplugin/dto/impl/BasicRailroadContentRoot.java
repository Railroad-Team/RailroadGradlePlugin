package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadContentRoot;
import dev.railroadide.railroadplugin.dto.RailroadSourceDirectory;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public record BasicRailroadContentRoot(File rootDirectory,
                                       List<RailroadSourceDirectory> sourceDirectories,
                                       List<RailroadSourceDirectory> testSourceDirectories,
                                       List<RailroadSourceDirectory> resourceDirectories,
                                       List<RailroadSourceDirectory> testResourceDirectories,
                                       Set<File> excludedDirectories) implements RailroadContentRoot, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public File getRootDirectory() {
        return rootDirectory;
    }

    @Override
    public DomainObjectSet<? extends RailroadSourceDirectory> getSourceDirectories() {
        return ImmutableDomainObjectSet.of(sourceDirectories);
    }

    @Override
    public DomainObjectSet<? extends RailroadSourceDirectory> getTestSourceDirectories() {
        return ImmutableDomainObjectSet.of(testSourceDirectories);
    }

    @Override
    public DomainObjectSet<? extends RailroadSourceDirectory> getResourceDirectories() {
        return ImmutableDomainObjectSet.of(resourceDirectories);
    }

    @Override
    public DomainObjectSet<? extends RailroadSourceDirectory> getTestResourceDirectories() {
        return ImmutableDomainObjectSet.of(testResourceDirectories);
    }

    @Override
    public Set<File> getExcludedDirectories() {
        return Collections.unmodifiableSet(excludedDirectories);
    }
}
