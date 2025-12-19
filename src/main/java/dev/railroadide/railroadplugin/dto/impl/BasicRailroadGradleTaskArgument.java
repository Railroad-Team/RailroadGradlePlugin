package dev.railroadide.railroadplugin.dto.impl;

import dev.railroadide.railroadplugin.dto.RailroadGradleTask;
import dev.railroadide.railroadplugin.dto.RailroadGradleTaskArgument;
import org.gradle.tooling.model.HierarchicalElement;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

public record BasicRailroadGradleTaskArgument(RailroadGradleTask gradleTask,
                                              String name,
                                              String displayName,
                                              @Nullable GradleTaskArgumentType type,
                                              String valueClassName,
                                              String description,
                                              Set<String> possibleValues,
                                              boolean clashing) implements RailroadGradleTaskArgument, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public RailroadGradleTask getTask() {
        return gradleTask;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public @Nullable GradleTaskArgumentType getType() {
        return type;
    }

    @Override
    public Class<?> getValueClass() {
        if (valueClassName == null)
            return null;

        return switch (valueClassName) {
            case "boolean" -> boolean.class;
            case "byte" -> byte.class;
            case "short" -> short.class;
            case "int" -> int.class;
            case "long" -> long.class;
            case "char" -> char.class;
            case "float" -> float.class;
            case "double" -> double.class;
            case "void" -> Void.TYPE;
            default -> {
                try {
                    yield Class.forName(valueClassName);
                } catch (ClassNotFoundException e) {
                    yield null;
                }
            }
        };
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<String> getPossibleValues() {
        return possibleValues == null ? Collections.emptySet() : Collections.unmodifiableSet(possibleValues);
    }

    @Override
    public boolean isClashing() {
        return clashing;
    }

    @Override
    public @Nullable HierarchicalElement getParent() {
        return getTask();
    }
}
