package dev.railroadide.railroadplugin.dto;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.HierarchicalElement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface RailroadConfiguration extends HierarchicalElement {
    String getName();

    boolean isCanBeResolved();

    boolean isCanBeConsumed();

    boolean isVisible();

    boolean isTransitive();

    List<String> getExtendsFrom();

    Map<String, String> getAttributes();

    DomainObjectSet<? extends RailroadDependency> getDependencies();

    /**
     * Problems hit while resolving this configuration, one human-readable line each. Empty when it resolved
     * cleanly or is not resolvable.
     */
    List<String> getResolutionFailures();

    @Nullable
    RailroadModule getParent();
}
