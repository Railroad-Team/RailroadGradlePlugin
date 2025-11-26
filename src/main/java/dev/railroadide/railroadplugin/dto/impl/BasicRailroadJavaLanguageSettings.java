package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadJavaLanguageSettings;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.tooling.model.java.InstalledJdk;

public class BasicRailroadJavaLanguageSettings implements RailroadJavaLanguageSettings {
    private final Project project;
    private final JavaPluginExtension javaExtension;

    public BasicRailroadJavaLanguageSettings(Project project) {
        this.project = project;
        this.javaExtension = project.getExtensions().findByType(JavaPluginExtension.class);
    }

    @Override
    public JavaVersion getSourceCompatibility() {
        return javaExtension != null ? javaExtension.getSourceCompatibility() : null;
    }

    @Override
    public JavaVersion getTargetCompatibility() {
        return javaExtension != null ? javaExtension.getTargetCompatibility() : null;
    }

    @Override
    public InstalledJdk getJdk() {
        return project.getExtensions().findByType(InstalledJdk.class);
    }
}
