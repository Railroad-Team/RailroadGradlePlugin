package dev.railroadide.railroadplugin.dto;

import org.gradle.api.JavaVersion;
import org.gradle.tooling.model.java.InstalledJdk;

public interface RailroadJavaLanguageSettings {
    JavaVersion getSourceCompatibility();
    JavaVersion getTargetCompatibility();
    InstalledJdk getJdk();
}
