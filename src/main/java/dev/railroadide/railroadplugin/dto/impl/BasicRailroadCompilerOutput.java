package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadCompilerOutput;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;

public record BasicRailroadCompilerOutput(boolean inheritsOutputDirectories,
                                          @Nullable File outputDirectory,
                                          @Nullable File testOutputDirectory) implements RailroadCompilerOutput, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public boolean inheritsOutputDirectories() {
        return inheritsOutputDirectories;
    }

    @Override
    public @Nullable File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public @Nullable File getTestOutputDirectory() {
        return testOutputDirectory;
    }
}
