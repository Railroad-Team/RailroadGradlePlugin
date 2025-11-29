package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadGradleTask;
import dev.railroadide.railroadplugin.dto.RailroadGradleTaskArgument;
import org.gradle.api.internal.tasks.options.OptionDescriptor;
import org.gradle.tooling.model.HierarchicalElement;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

public record BasicRailroadGradleTaskArgument(RailroadGradleTask gradleTask,
                                              OptionDescriptor option) implements RailroadGradleTaskArgument {
    @Override
    public RailroadGradleTask getTask() {
        return gradleTask;
    }

    @Override
    public String getName() {
        return option.getName();
    }

    @Override
    public String getDisplayName() {
        return option.getName();
    }

    @Override
    public GradleTaskArgumentType getType() {
        Class<?> argType = option.getArgumentType();
        if (argType == Boolean.class || argType == boolean.class) {
            return GradleTaskArgumentType.BOOLEAN;
        } else if (argType.isEnum()) {
            return GradleTaskArgumentType.ENUM;
        } else if (argType.isPrimitive() || Number.class.isAssignableFrom(argType)) {
            return GradleTaskArgumentType.NUMBER;
        } else if (argType == String.class) {
            return GradleTaskArgumentType.STRING;
        } else if (argType == File.class || argType == Path.class) {
            return GradleTaskArgumentType.FILE;
        } else {
            return GradleTaskArgumentType.UNKNOWN;
        }
    }

    @Override
    public Class<?> getValueClass() {
        return option.getArgumentType();
    }

    @Override
    public String getDescription() {
        return option.getDescription();
    }

    @Override
    public Set<String> getPossibleValues() {
        return option.getAvailableValues();
    }

    @Override
    public boolean isClashing() {
        return option.isClashing();
    }

    @Override
    public @Nullable HierarchicalElement getParent() {
        return getTask();
    }
}
