package dev.railroadide.railroadplugin.dto;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface RailroadCompilerOutput {
    boolean inheritsOutputDirectories();

    @Nullable
    File getOutputDirectory();

    @Nullable
    File getTestOutputDirectory();
}
