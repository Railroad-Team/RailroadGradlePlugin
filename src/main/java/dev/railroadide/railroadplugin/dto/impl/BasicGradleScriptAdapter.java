package dev.railroadide.railroadplugin.dto.impl;

import org.gradle.tooling.model.gradle.GradleScript;

import java.io.File;

/**
 * Minimal implementation of {@link GradleScript} exposing only the source file.
 */
public class BasicGradleScriptAdapter implements GradleScript {
    private final File sourceFile;

    public BasicGradleScriptAdapter(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public File getSourceFile() {
        return sourceFile;
    }
}
