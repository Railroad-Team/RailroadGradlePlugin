package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadSourceDirectory;

import java.io.File;

public class BasicRailroadSourceDirectory implements RailroadSourceDirectory {
    private final boolean generated;
    private final File directory;
    private final String type;

    public BasicRailroadSourceDirectory(boolean generated, File directory, String type) {
        this.generated = generated;
        this.directory = directory;
        this.type = type;
    }

    @Override
    public boolean isGenerated() {
        return generated;
    }

    @Override
    public File getDirectory() {
        return directory;
    }

    @Override
    public String getType() {
        return type;
    }
}
