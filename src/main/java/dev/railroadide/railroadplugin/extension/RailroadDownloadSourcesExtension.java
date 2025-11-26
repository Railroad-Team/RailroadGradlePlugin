package dev.railroadide.railroadplugin.extension;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;

import javax.inject.Inject;
import java.util.Set;

public class RailroadDownloadSourcesExtension {
    public static final String EXTENSION_NAME = "downloadSources";

    private final Property<Boolean> downloadSources;
    private final Property<Boolean> downloadJavadoc;
    private final SetProperty<String> configurations;
    private final Property<Boolean> logResolvedFiles;

    @Inject
    public RailroadDownloadSourcesExtension(ObjectFactory objects) {
        this.downloadSources = objects.property(Boolean.class).convention(true);
        this.downloadJavadoc = objects.property(Boolean.class).convention(true);
        this.configurations = objects.setProperty(String.class).convention(Set.of(
                "compileClasspath",
                "runtimeClasspath",
                "testCompileClasspath"
        ));
        this.logResolvedFiles = objects.property(Boolean.class).convention(true);
    }

    @Input
    public Property<Boolean> getDownloadSources() {
        return downloadSources;
    }

    @Input
    public Property<Boolean> getDownloadJavadoc() {
        return downloadJavadoc;
    }

    @Input
    public SetProperty<String> getConfigurations() {
        return configurations;
    }

    @Input
    public Property<Boolean> getLogResolvedFiles() {
        return logResolvedFiles;
    }
}
