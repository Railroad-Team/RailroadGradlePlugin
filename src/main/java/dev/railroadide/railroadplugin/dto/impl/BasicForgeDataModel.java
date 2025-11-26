package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.ForgeDataModel;

public record BasicForgeDataModel(String minecraftVersion,
                                  String forgeVersion,
                                  String forgeGradleVersion) implements ForgeDataModel {
}
