package dev.railroadide.railroadplugin.dto;

import org.gradle.tooling.model.DomainObjectSet;

import java.io.File;
import java.util.Set;
import java.util.List;

public interface RailroadContentRoot {
    String getSourceSetName();
    List<File> getCompileClasspath();
    List<File> getClassOutputs();
    File getCompilerHome();
    File getRootDirectory();

    DomainObjectSet<? extends RailroadSourceDirectory> getSourceDirectories();

    DomainObjectSet<? extends RailroadSourceDirectory> getTestSourceDirectories();

    DomainObjectSet<? extends RailroadSourceDirectory> getResourceDirectories();

    DomainObjectSet<? extends RailroadSourceDirectory> getTestResourceDirectories();

    Set<File> getExcludedDirectories();
}
