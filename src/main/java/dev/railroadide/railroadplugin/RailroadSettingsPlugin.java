package dev.railroadide.railroadplugin;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.jetbrains.annotations.NotNull;

public class RailroadSettingsPlugin implements Plugin<Settings> {
    static String loomVersion = null;

    @Override
    public void apply(@NotNull Settings target) {
        target.pluginManagement(pluginManagementSpec ->
                pluginManagementSpec.resolutionStrategy(rs ->
                        rs.eachPlugin(details -> {
                            if (details.getRequested().getId().getId().equals("fabric-loom")) {
                                loomVersion = details.getRequested().getVersion();
                            }
                        })));
    }
}
