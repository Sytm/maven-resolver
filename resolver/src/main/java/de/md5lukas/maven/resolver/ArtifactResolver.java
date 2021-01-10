package de.md5lukas.maven.resolver;

import de.md5lukas.maven.resolver.cache.SimpleSnapshotCache;
import de.md5lukas.maven.resolver.cache.SnapshotCache;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Class that can resolve the URL of artifacts by searching for them in multiple repositories
 */
public final class ArtifactResolver {

    @NotNull
    private final SnapshotResolver snapshotResolver;

    @NotNull
    private final Map<String, Repository> artifactToRepository;

    /**
     * The artifact resolver checks the validity of URLs by sending a HTTP HEAD request and if that returns the status code 200 the url is deemed valid.
     * <br><br>
     * This should only be disabled if the artifact resolver has only one repository, because otherwise it could happen that the URL for a non-snapshot artifact
     * is not present in that repository
     *
     * @param checkURLValidity Whether the artifact resolver should check urls are valid or not
     */
    @Setter
    private boolean checkURLValidity = true;

    @NotNull
    private final List<Repository> repositories;

    /**
     * Creates a new artifact resolver instance using {@link SimpleSnapshotCache} as the cache with a TTL of 1 day
     */
    public ArtifactResolver() {
        this(TimeUnit.DAYS.toMillis(1));
    }

    /**
     * Creates a new artifact resolver instance using {@link SimpleSnapshotCache} as the cache
     *
     * @param simpleCacheTTL The TTL of the cache
     */
    public ArtifactResolver(long simpleCacheTTL) {
        this(new SimpleSnapshotCache(simpleCacheTTL));
    }

    /**
     * Creates a new artifact resolver instance using the provided {@link SnapshotCache} as a cache
     *
     * @param snapshotCache The snapshot cache implementation to use
     */
    public ArtifactResolver(@NotNull @NonNull SnapshotCache snapshotCache) {
        this.snapshotResolver = new SnapshotResolver(snapshotCache);
        this.artifactToRepository = new HashMap<>();
        this.repositories = new ArrayList<>();
    }

    /**
     * Adds a repository to the artifact resolver where it should look for maven artifacts
     *
     * @param repository The repository to add
     */
    public void addRepository(@NotNull @NonNull Repository repository) {
        this.repositories.add(repository);
    }

    /**
     * Checks every repository for the artifact and returns a URL if it found one. If none of the repositories have the artifact then <code>null</code> is
     * returned.
     * <br><br>
     * If none of the repositories have the artifact, but while trying to access a repository and exception occurred, a new exception is created and all
     * other exception are added to that
     *
     * @param artifact The artifact to try to resolve
     * @return The resolved URL or <code>null</code> if it could not be found
     * @throws Exception If an exception occurred while trying to access the artifact
     */
    @Nullable
    public URL resolveArtifactURL(@NotNull Artifact artifact) throws Exception {
        if (repositories.isEmpty()) {
            return null;
        }

        List<Exception> exceptions = new ArrayList<>();

        Repository lastRepository = artifactToRepository.get(artifact.getFuzzyId());

        if (lastRepository != null) {
            try {
                URL result = resolveArtifactURL(lastRepository, artifact);
                if (result != null) {
                    return result;
                }
            } catch (Exception e) {
                exceptions.add(e);
            }
        }

        for (Repository repository : repositories) {
            if (repository == lastRepository)
                continue;

            try {
                URL result = resolveArtifactURL(repository, artifact);

                if (result != null) {
                    artifactToRepository.put(artifact.getFuzzyId(), repository);
                    return result;
                }
            } catch (Exception e) {
                exceptions.add(e);
            }
        }

        if (exceptions.isEmpty()) {
            return null;
        } else {
            Exception e = new Exception("An error occurred while trying to resolve an URL for a maven artifact");
            exceptions.forEach(e::addSuppressed);
            throw e;
        }
    }

    @Nullable
    private URL resolveArtifactURL(@NotNull Repository repository, @NotNull Artifact artifact) throws Exception {
        URL url;
        if (artifact.isSnapshot()) {
            String snapshotVersion = snapshotResolver.resolveSnapshotVersion(repository, artifact);
            if (snapshotVersion == null) {
                return null;
            }

            url = repository.createURL(artifact.getPath(snapshotVersion));
        } else {
            url = repository.createURL(artifact.getPath());
        }

        if (checkURLValidity) {
            boolean urlCheck = checkURL(url);

            if (!urlCheck) {
                return null;
            }
        }

        return url;
    }

    private boolean checkURL(@NotNull URL url) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("HEAD");

            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
