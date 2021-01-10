package de.md5lukas.maven.resolver.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface that is used by the internal snapshot version resolver to cache the results retrieved from the repositories
 */
public interface SnapshotCache {

    /**
     * Inserts an element into the cache
     *
     * @param fuzzyId         The "fuzzy id" of the artifact
     * @param snapshotVersion The resolved snapshot version retrieved from a repository
     */
    void put(@NotNull String fuzzyId, @NotNull String snapshotVersion);

    /**
     * Gets an element from the cache.
     * <br><br>
     * If this method returns <code>null</code> the snapshot resolver will try to retrieve a new snapshot version from the repositories.
     *
     * @param fuzzyId The "fuzzy id" of the artifact
     * @return A cached snapshotVersion if present and valid or <code>null</code>
     */
    @Nullable
    String get(String fuzzyId);
}
