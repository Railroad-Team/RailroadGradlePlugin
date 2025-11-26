package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.FabricDataModel;

public record BasicFabricDataModel(String minecraftVersion, String mappingsVersion, String loaderVersion,
                                   String fabricApiVersion,
                                   LoomVersion loomVersion) implements FabricDataModel {
    public record BasicLoomVersion(String version, boolean isArchitecturyLoom) implements LoomVersion {
    }
}
