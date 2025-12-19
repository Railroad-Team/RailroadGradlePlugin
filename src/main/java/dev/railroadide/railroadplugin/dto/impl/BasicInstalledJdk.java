package dev.railroadide.railroadplugin.dto.impl;

import org.gradle.api.JavaVersion;
import org.gradle.tooling.model.java.InstalledJdk;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;

public record BasicInstalledJdk(JavaVersion javaVersion,
                                File javaHome) implements InstalledJdk, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public JavaVersion getJavaVersion() {
        return javaVersion;
    }

    @Override
    public File getJavaHome() {
        return javaHome;
    }
}
