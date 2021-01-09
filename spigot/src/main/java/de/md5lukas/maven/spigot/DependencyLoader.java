package de.md5lukas.maven.spigot;

import de.md5lukas.maven.resolver.Artifact;
import de.md5lukas.maven.resolver.ArtifactResolver;
import de.md5lukas.maven.resolver.Repository;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DependencyLoader {

    public static DependencyLoader forPlugin(@NotNull @NonNull Plugin plugin) {
        return new DependencyLoader(plugin);
    }

    @NotNull
    private final Plugin plugin;

    @NotNull
    private final Class<? extends Plugin> clazz;

    @NotNull
    private final Logger logger;

    @NotNull
    private final ArtifactResolver resolver;

    @NotNull
    private final File libFolder;

    @NotNull
    private final Map<Artifact, File> artifacts;

    private DependencyLoader(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.clazz = plugin.getClass();
        this.logger = plugin.getLogger();

        this.resolver = new ArtifactResolver();

        this.libFolder = new File(plugin.getDataFolder(), "lib");

        if (!this.libFolder.exists()) {
            logger.info("Creating folder " + this.libFolder.getAbsolutePath());
            if (!this.libFolder.mkdirs()) {
                throw new RuntimeException("Could not create library folder " + this.libFolder.getAbsolutePath());
            }
        }

        this.configureArtifactResolver();
        this.artifacts = this.getArtifacts();
    }

    public void loadAll() {
        List<Map.Entry<Artifact, File>> requiredArtifacts = getRequiredArtifacts();

        for (Map.Entry<Artifact, File> required : requiredArtifacts) {
            Artifact artifact = required.getKey();
            File artifactFile = required.getValue();
            this.logger.log(Level.FINE, "Downloading artifact " + artifact.toString() + " to " + artifactFile.getAbsolutePath());

            // TODO

            this.logger.log(Level.INFO, "Downloaded artifact " + artifact.toString());
        }

        for (Map.Entry<Artifact, File> artifactEntry : this.artifacts.entrySet()) {
            this.logger.log(Level.FINE, "Loading artifact " + artifactEntry.getKey().toString() + " from file " + artifactEntry.getValue().getAbsolutePath());
            Helpers.loadJar(clazz, artifactEntry.getValue());
            this.logger.log(Level.INFO, "Loaded artifact " + artifactEntry.getKey().toString());
        }
    }

    @SneakyThrows(NoSuchAlgorithmException.class)
    private static String download(File file, URL url) throws IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream is = new DigestInputStream(url.openStream(), digest);
             FileOutputStream fos = new FileOutputStream(file)) {
            fos.getChannel().transferFrom(Channels.newChannel(is), 0, Long.MAX_VALUE);
        }

        return Helpers.bytesToHex(digest.digest());
    }

    private void configureArtifactResolver() {
        for (MavenRepository repository : clazz.getAnnotationsByType(MavenRepository.class)) {
            Repository repo = new Repository(repository.name(), repository.url());
            logger.log(Level.FINE, "Detected annotated repository " + repo.toString());
            resolver.addRepository(repo);
        }
    }

    private Map<Artifact, File> getArtifacts() {
        MavenDependency[] annotations = clazz.getAnnotationsByType(MavenDependency.class);
        Map<Artifact, File> result = new HashMap<>(annotations.length);

        for (MavenDependency dep : annotations) {
            Artifact artifact = new Artifact(
                    dep.groupId(),
                    dep.artifactId(),
                    dep.version(),
                    dep.classifier()
            );
            this.logger.log(Level.FINE, "Detected annotated artifact " + artifact.toString());
            result.put(artifact, new File(this.libFolder, artifact.getFileName()));
        }

        return result;
    }

    private List<Map.Entry<Artifact, File>> getRequiredArtifacts() {
        List<Map.Entry<Artifact, File>> required = new ArrayList<>();

        for (Map.Entry<Artifact, File> artifactEntry : artifacts.entrySet()) {
            if (artifactEntry.getValue().exists()) {
                this.logger.log(Level.FINE, "Artifact " + artifactEntry.getKey().toString() + " is found at " + artifactEntry.getValue().getAbsolutePath());
            } else {
                this.logger.log(Level.FINE, "Artifact " + artifactEntry.getKey().toString() + " could not be found at " + artifactEntry.getValue().getAbsolutePath());
                required.add(artifactEntry);
            }
        }

        return required;
    }
}
