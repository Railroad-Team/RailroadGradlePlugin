package dev.railroadide.railroadplugin.dto;

public interface FabricDataModel {
    String minecraftVersion();
    String mappingsVersion();
    String loaderVersion();
    String fabricApiVersion();
    LoomVersion loomVersion();

    interface LoomVersion {
        String version();
        boolean isArchitecturyLoom();
    }
}
