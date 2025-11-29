package dev.railroadide.railroadplugin.dto;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface RailroadGradleTaskArgument extends HierarchicalElement {
    RailroadGradleTask getTask();

    String getName();

    String getDisplayName();

    @Nullable GradleTaskArgumentType getType();

    Class<?> getValueClass();

    String getDescription();

    Set<String> getPossibleValues();

    boolean isClashing();

    enum GradleTaskArgumentType {
        STRING,
        BOOLEAN,
        ENUM,
        FILE,
        NUMBER,
        UNKNOWN
    }

    @SuppressWarnings("unchecked")
    @Override
    default DomainObjectSet<? extends HierarchicalElement> getChildren() {
        return (DomainObjectSet<? extends HierarchicalElement>) ImmutableDomainObjectSet.of(Collections.EMPTY_LIST);
    }
}
