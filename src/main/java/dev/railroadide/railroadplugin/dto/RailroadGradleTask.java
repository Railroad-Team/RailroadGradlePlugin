package dev.railroadide.railroadplugin.dto;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.HierarchicalElement;

public interface RailroadGradleTask extends GradleTask, HierarchicalElement {
    RailroadModule module();

    DomainObjectSet<? extends RailroadGradleTaskArgument> getArguments();
}
