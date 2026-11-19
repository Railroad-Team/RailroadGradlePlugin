package dev.railroadide.railroadplugin.dto;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * Resolves a project's configurations once each, leniently, and records every problem instead of swallowing it, so
 * the model can tell "no dependencies" apart from "resolution failed".
 */
final class ConfigurationResolver {
    private static final int MAX_CAUSES = 4;

    private final Project project;
    private final Map<String, Result> results = new HashMap<>();
    private final LinkedHashSet<String> failures = new LinkedHashSet<>();

    ConfigurationResolver(Project project) {
        this.project = project;
    }

    /**
     * @param files                existing artifact files, in resolution order
     * @param artifactsByComponent the first artifact file of each resolved component
     * @param root                 the root of the dependency graph, or {@code null} when the graph could not be resolved
     * @param failures             problems hit while resolving this configuration
     */
    record Result(List<File> files,
                  Map<ComponentIdentifier, File> artifactsByComponent,
                  @Nullable ResolvedComponentResult root,
                  List<String> failures) {
    }

    Result resolve(Configuration configuration) {
        Result result = results.get(configuration.getName());
        if (result == null) {
            result = doResolve(configuration);
            results.put(configuration.getName(), result);
            failures.addAll(result.failures());
        }

        return result;
    }

    void recordFailure(String context, Throwable failure) {
        failures.add(describe(context, failure));
    }

    List<String> failures() {
        return List.copyOf(failures);
    }

    private Result doResolve(Configuration configuration) {
        String context = "configuration '" + qualifiedName(configuration) + "'";
        List<String> problems = new ArrayList<>();
        LinkedHashSet<File> files = new LinkedHashSet<>();
        Map<ComponentIdentifier, File> artifactsByComponent = new HashMap<>();

        try {
            ArtifactCollection artifacts = configuration.getIncoming()
                    .artifactView(view -> view.setLenient(true))
                    .getArtifacts();
            for (ResolvedArtifactResult artifact : artifacts.getArtifacts()) {
                File file = artifact.getFile();
                artifactsByComponent.putIfAbsent(artifact.getId().getComponentIdentifier(), file);
                if (file.exists())
                    files.add(file);
            }

            for (Throwable failure : artifacts.getFailures()) {
                problems.add(describe(context, failure));
            }
        } catch (Exception exception) {
            // Typically the whole configuration is unresolvable here (for example Gradle refusing resolution outside
            // the owning project's lock), so the graph below would fail the same way.
            problems.add(describe(context, exception));
            return new Result(List.copyOf(files), artifactsByComponent, null, List.copyOf(problems));
        }

        ResolvedComponentResult root = null;
        try {
            root = configuration.getIncoming().getResolutionResult().getRoot();
        } catch (Exception exception) {
            problems.add(describe(context, exception));
        }

        return new Result(List.copyOf(files), artifactsByComponent, root, List.copyOf(problems));
    }

    private String qualifiedName(Configuration configuration) {
        String path = project.getPath();
        return (path.endsWith(":") ? path : path + ":") + configuration.getName();
    }

    private static String describe(String context, Throwable failure) {
        var description = new StringBuilder(context).append(": ");
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        String previousMessage = null;
        int depth = 0;
        for (Throwable current = failure; current != null && seen.add(current) && depth < MAX_CAUSES; current = current.getCause()) {
            String message = current.getMessage();
            if (message == null || message.isBlank())
                message = current.getClass().getName();

            if (!message.equals(previousMessage)) {
                if (depth > 0)
                    description.append(" -> ");

                description.append(message.strip());
                depth++;
            }

            previousMessage = message;
        }

        return description.toString();
    }
}
