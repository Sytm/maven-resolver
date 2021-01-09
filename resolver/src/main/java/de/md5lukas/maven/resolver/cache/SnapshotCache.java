package de.md5lukas.maven.resolver.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SnapshotCache {

    void put(@NotNull String fuzzyId, @NotNull String snapshotVersion);

    @Nullable
    String get(String fuzzyId);
}
