package de.md5lukas.maven.spigot;

import de.md5lukas.maven.resolver.Artifact;
import de.md5lukas.maven.resolver.ArtifactResolver;
import de.md5lukas.maven.resolver.MavenChecksum;
import de.md5lukas.maven.resolver.Repository;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public final class DependencyLoader {

    public static DependencyLoader forPlugin(@NotNull @NonNull Plugin plugin) {
        return new DependencyLoader(plugin);
    }

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

    @NotNull
    private final MavenChecksum checksumAlgorithm;

    private final boolean ignoreNotFoundChecksum;

    private DependencyLoader(@NotNull Plugin plugin) {
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

        MavenResolver resolverAnnotation = clazz.getAnnotation(MavenResolver.class);

        if (resolverAnnotation == null) {
            throw new IllegalStateException("The plugin class has no maven resolver annotation");
        }

        if (resolverAnnotation.useMavenCentral()) {
            this.resolver.addRepository(Repository.MAVEN_CENTRAL);
        }
        if (resolverAnnotation.useSonatype()) {
            this.resolver.addRepository(Repository.SONATYPE);
        }
        this.checksumAlgorithm = resolverAnnotation.checksumAlgorithm();
        this.ignoreNotFoundChecksum = resolverAnnotation.ignoreNotFoundChecksum();

        this.loadCustomRepositories();
        this.artifacts = this.getArtifacts();
    }

    public void loadAll() throws Exception {
        List<Map.Entry<Artifact, File>> requiredArtifacts = getRequiredArtifacts();

        for (Map.Entry<Artifact, File> required : requiredArtifacts) {
            Artifact artifact = required.getKey();
            File artifactFile = required.getValue();
            this.logger.fine("Downloading artifact " + artifact.toString() + " to " + artifactFile.getAbsolutePath());

            URL artifactURL = this.resolver.resolveArtifactURL(artifact);

            if (artifactURL == null) {
                throw new DependencyNotFoundException("Could not find artifact " + artifact.toString());
            }

            Helpers.downloadFile(artifactFile, artifactURL);

            this.logger.info("Downloaded artifact " + artifact.toString());

            Artifact checksumArtifact = artifact.getChecksumArtifact(this.checksumAlgorithm);
            URL checksumArtifactURL = resolver.resolveArtifactURL(checksumArtifact);

            if (checksumArtifactURL != null) {
                this.logger.fine("Downloading checksum artifact " + checksumArtifact.toString());

                byte[] loadedChecksum = Helpers.readChecksum(checksumArtifactURL);

                byte[] actualChecksum = Helpers.digestFile(checksumAlgorithm.getMessageDigest(), artifactFile);

                if (!Arrays.equals(loadedChecksum, actualChecksum)) {
                    throw new DependencyNotFoundException("Could not verify checksum of artifact " + artifact.toString() + " downloaded from " + artifactURL.toString());
                }

                this.logger.info("Verified checksum of artifact " + artifact.toString());
            } else if (!this.ignoreNotFoundChecksum) {
                throw new DependencyNotFoundException("Could not find checksum artifact " + checksumArtifact.toString());
            } else {
                this.logger.warning("Could not find checksum artifact " + checksumArtifact.toString());
            }
        }

        for (Map.Entry<Artifact, File> artifactEntry : this.artifacts.entrySet()) {
            this.logger.fine("Loading artifact " + artifactEntry.getKey().toString() + " from file " + artifactEntry.getValue().getAbsolutePath());
            Helpers.loadJar(clazz, artifactEntry.getValue());
            this.logger.info("Loaded artifact " + artifactEntry.getKey().toString());
        }
    }

    @SneakyThrows
    public void loadAllUnchecked() {
        loadAll();
    }

    private void loadCustomRepositories() {
        for (MavenRepository repository : clazz.getAnnotationsByType(MavenRepository.class)) {
            Repository repo = new Repository(repository.name(), repository.url());
            this.logger.fine("Detected annotated repository " + repo.toString());
            this.resolver.addRepository(repo);
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
                    dep.classifier(),
                    dep.type()
            );
            this.logger.fine("Detected annotated artifact " + artifact.toString());
            result.put(artifact, new File(this.libFolder, artifact.getFileName()));
        }

        return result;
    }

    private List<Map.Entry<Artifact, File>> getRequiredArtifacts() {
        List<Map.Entry<Artifact, File>> required = new ArrayList<>();

        for (Map.Entry<Artifact, File> artifactEntry : artifacts.entrySet()) {
            if (artifactEntry.getValue().exists()) {
                this.logger.fine("Artifact " + artifactEntry.getKey().toString() + " is found at " + artifactEntry.getValue().getAbsolutePath());
            } else {
                this.logger.fine("Artifact " + artifactEntry.getKey().toString() + " could not be found at " + artifactEntry.getValue().getAbsolutePath());
                required.add(artifactEntry);
            }
        }

        return required;
    }
}
