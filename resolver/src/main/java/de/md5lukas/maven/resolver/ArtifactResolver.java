package de.md5lukas.maven.resolver;

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

public final class ArtifactResolver {

    @NotNull
    private final SnapshotResolver snapshotResolver;

    @NotNull
    private final Map<String, Repository> artifactToRepository;

    @Setter
    private boolean checkURLValidity = true;

    @NotNull
    private final List<Repository> repositories;

    public ArtifactResolver() {
        this.snapshotResolver = new SnapshotResolver();
        this.artifactToRepository = new HashMap<>();
        this.repositories = new ArrayList<>();
    }

    public void addRepository(@NotNull @NonNull Repository repository) {
        this.repositories.add(repository);
    }

    public void setSnapshotVersionCacheTTL(long ttl) {
        snapshotResolver.setCacheTTL(ttl);
    }

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
