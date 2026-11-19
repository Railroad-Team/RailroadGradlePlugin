package dev.railroadide.railroadplugin.action;

import dev.railroadide.railroadplugin.dto.RailroadModule;
import dev.railroadide.railroadplugin.dto.RailroadProject;
import dev.railroadide.railroadplugin.dto.impl.BasicRailroadProject;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;
import org.gradle.tooling.UnknownModelException;
import org.gradle.tooling.UnsupportedVersionException;
import org.gradle.tooling.model.gradle.BasicGradleProject;
import org.gradle.tooling.model.gradle.GradleBuild;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fetches the {@link RailroadModule} of every project in the build, each in its own project's context, and assembles
 * them into a {@link RailroadProject}.
 * <p>
 * Use this instead of requesting {@code RailroadProject} directly: Gradle 9+ only lets a model builder resolve
 * configurations of the project it was invoked for, so a single root-level model cannot resolve subproject
 * classpaths.
 * <pre>{@code
 * RailroadProject project = connection.action(new RailroadProjectAction())
 *         .withArguments("--init-script", initScript)
 *         .run();
 * }</pre>
 * Modules are fetched in parallel when the build sets {@code org.gradle.tooling.parallel=true}.
 */
public final class RailroadProjectAction implements BuildAction<RailroadProject> {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public RailroadProject execute(BuildController controller) {
        GradleBuild build = controller.getBuildModel();
        List<FetchModule> fetches = build.getProjects().stream().map(FetchModule::new).toList();

        List<RailroadModule> fetched;
        try {
            fetched = controller.run(fetches);
        } catch (UnsupportedVersionException exception) {
            // Gradle older than 6.8 cannot run nested actions; fetch one project at a time instead.
            fetched = new ArrayList<>(fetches.size());
            for (FetchModule fetch : fetches) {
                fetched.add(fetch.execute(controller));
            }
        }

        List<RailroadModule> modules = fetched.stream().filter(Objects::nonNull).toList();
        String rootPath = build.getRootProject().getPath();
        RailroadModule root = modules.stream()
                .filter(module -> rootPath.equals(module.getPath()))
                .findFirst()
                .orElse(null);

        return new BasicRailroadProject(
                root != null ? root.getName() : build.getRootProject().getName(),
                root != null ? root.getDescription() : null,
                null,
                modules,
                root != null ? root.getJavaLanguageSettings() : null
        );
    }

    private record FetchModule(BasicGradleProject project) implements BuildAction<RailroadModule> {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public RailroadModule execute(BuildController controller) {
            try {
                return controller.getModel(project, RailroadModule.class);
            } catch (UnknownModelException exception) {
                // The Railroad plugin is not applied to this project.
                return null;
            }
        }
    }
}
