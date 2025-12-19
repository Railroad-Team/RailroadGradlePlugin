package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.FabricDataModel;

import java.io.Serial;
import java.io.Serializable;

public record BasicFabricDataModel(String minecraftVersion,
                                   String mappingsVersion,
                                   String loaderVersion,
                                   String fabricApiVersion,
                                   LoomVersion loomVersion) implements FabricDataModel, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String minecraftVersion() {
        return minecraftVersion;
    }

    @Override
    public String mappingsVersion() {
        return mappingsVersion;
    }

    @Override
    public String loaderVersion() {
        return loaderVersion;
    }

    @Override
    public String fabricApiVersion() {
        return fabricApiVersion;
    }

    @Override
    public LoomVersion loomVersion() {
        return loomVersion;
    }

    public record BasicLoomVersion(String version, boolean isArchitecturyLoom) implements LoomVersion, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public String version() {
            return version;
        }

        @Override
        public boolean isArchitecturyLoom() {
            return isArchitecturyLoom;
        }
    }
}
