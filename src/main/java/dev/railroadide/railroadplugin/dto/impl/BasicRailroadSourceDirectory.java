package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadSourceDirectory;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;

public record BasicRailroadSourceDirectory(boolean generated,
                                           File directory,
                                           String type) implements RailroadSourceDirectory, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
