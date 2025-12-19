package dev.railroadide.railroadplugin.dto.impl;

import org.gradle.tooling.model.gradle.GradleScript;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;

/**
 * Minimal implementation of {@link GradleScript} exposing only the source file.
 */
public class BasicGradleScriptAdapter implements GradleScript, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final File sourceFile;

    public BasicGradleScriptAdapter(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public File getSourceFile() {
        return sourceFile;
    }
}
