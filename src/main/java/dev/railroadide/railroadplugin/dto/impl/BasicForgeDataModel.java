package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.ForgeDataModel;

import java.io.Serial;
import java.io.Serializable;

public record BasicForgeDataModel(String minecraftVersion,
                                  String forgeVersion,
                                  String forgeGradleVersion) implements ForgeDataModel, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String minecraftVersion() {
        return minecraftVersion;
    }

    @Override
    public String forgeVersion() {
        return forgeVersion;
    }

    @Override
    public String forgeGradleVersion() {
        return forgeGradleVersion;
    }
}
