package de.md5lukas.maven.resolver.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleSnapshotCache implements SnapshotCache {

    private final long ttl;

    @NotNull
    private final Map<String, CacheEntry> map;

    public SimpleSnapshotCache(long ttl) {
        this.ttl = ttl;
        map = new HashMap<>();
    }

    @Override
    public void put(@NotNull String fuzzyId, @NotNull String snapshotVersion) {
        sweep();
        if (ttl <= 0)
            return;
        map.put(fuzzyId, new CacheEntry(snapshotVersion));
    }

    @Override
    @Nullable
    public String get(String fuzzyId) {
        CacheEntry cacheEntry = map.get(fuzzyId);

        if (cacheEntry == null) {
            return null;
        }

        if (System.currentTimeMillis() > cacheEntry.validUntil) {
            map.remove(fuzzyId);
            return null;
        }

        return cacheEntry.snapshotVersion;
    }

    private void sweep() {
        List<String> remove = new ArrayList<>();

        long now = System.currentTimeMillis();

        for (Map.Entry<String, CacheEntry> entry : map.entrySet()) {
            if (now > entry.getValue().validUntil) {
                remove.add(entry.getKey());
            }
        }

        for (String k : remove) {
            map.remove(k);
        }
    }

    private class CacheEntry {

        @NotNull
        private final String snapshotVersion;
        private final long validUntil;

        private CacheEntry(@NotNull String snapshotVersion) {
            this.snapshotVersion = snapshotVersion;
            this.validUntil = System.currentTimeMillis() + ttl;
        }
    }
}
