package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadJavaLanguageSettings;
import org.gradle.api.JavaVersion;
import org.gradle.tooling.model.java.InstalledJdk;

import java.io.Serial;
import java.io.Serializable;

public record BasicRailroadJavaLanguageSettings(JavaVersion sourceCompatibility,
                                                JavaVersion targetCompatibility,
                                                InstalledJdk jdk) implements RailroadJavaLanguageSettings, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public JavaVersion getSourceCompatibility() {
        return sourceCompatibility;
    }

    @Override
    public JavaVersion getTargetCompatibility() {
        return targetCompatibility;
    }

    @Override
    public InstalledJdk getJdk() {
        return jdk;
    }
}
