package de.md5lukas.maven.resolver;

import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    @NotNull
    public ResolveResult<URL> resolveArtifactURL(@NotNull Artifact artifact) {
        if (repositories.isEmpty()) {
            return ResolveResult.notFound();
        }

        List<Exception> exceptions = new ArrayList<>();

        Repository lastRepository = artifactToRepository.get(artifact.getFuzzyId());

        if (lastRepository != null) {
            ResolveResult<URL> result = resolveArtifactURL(lastRepository, artifact);

            switch (result.getStatus()) {
                case ERROR:
                    exceptions.add(result.getException());
                    break;
                case SUCCESS:
                    return result;
            }
        }

        for (Repository repository : repositories) {
            if (repository == lastRepository)
                continue;

            ResolveResult<URL> result = resolveArtifactURL(repository, artifact);

            switch (result.getStatus()) {
                case ERROR:
                    exceptions.add(result.getException());
                    break;
                case SUCCESS:
                    artifactToRepository.put(artifact.getFuzzyId(), repository);
                    return result;
            }
        }

        if (exceptions.isEmpty()) {
            return ResolveResult.notFound();
        } else {
            Exception e = new Exception("An error occurred while trying to resolve an URL for a maven artifact");
            exceptions.forEach(e::addSuppressed);
            return ResolveResult.error(e);
        }
    }

    public ResolveResult<String> getChecksum(@NotNull @NonNull Artifact artifact, @NotNull @NonNull MavenChecksum type) {
        ResolveResult<URL> checksumURL = resolveArtifactURL(artifact);

        switch (checksumURL.getStatus()) {
            case ERROR:
                return checksumURL.castError();
            case NOT_FOUND:
                return ResolveResult.notFound();
        }

        URL url = checksumURL.getValue();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            return ResolveResult.success(br.readLine());
        } catch (IOException e) {
            return ResolveResult.error(e);
        }
    }

    @NotNull
    private ResolveResult<URL> resolveArtifactURL(@NotNull Repository repository, @NotNull Artifact artifact) {
        URL url;
        if (artifact.isSnapshot()) {
            ResolveResult<String> snapshotVersion = snapshotResolver.resolveSnapshotVersion(repository, artifact);
            switch (snapshotVersion.getStatus()) {
                case NOT_FOUND:
                    return ResolveResult.notFound();
                case ERROR:
                    return snapshotVersion.castError();
            }
            url = repository.createURL(artifact.getPath(snapshotVersion.getValue()));
        } else {
            url = repository.createURL(artifact.getPath());
        }

        if (checkURLValidity) {
            ResolveResult<Integer> urlCheck = checkURL(url);

            switch (urlCheck.getStatus()) {
                case NOT_FOUND:
                    return ResolveResult.notFound();
                case ERROR:
                    return urlCheck.castError();
            }
        }

        return ResolveResult.success(url);
    }

    @NotNull
    private ResolveResult<Integer> checkURL(@NotNull URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("HEAD");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return ResolveResult.success(connection.getResponseCode());
            } else {
                return ResolveResult.notFound();
            }
        } catch (IOException e) {
            return ResolveResult.error(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
