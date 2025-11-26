package dev.railroadide.railroadplugin.dto;

import java.io.File;

public interface RailroadSourceDirectory {
    boolean isGenerated();
    File getDirectory();
    String getType();
}
