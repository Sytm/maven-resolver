package de.md5lukas.maven.resolver;

import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SimpleCache<K, V> {

    @Setter
    private long ttl;

    private Map<K, CacheEntry> map;

    public SimpleCache() {
        ttl = TimeUnit.DAYS.toMillis(1);
        map = new HashMap<>();
    }

    public void put(K k, V v) {
        sweep();
        if (ttl <= 0)
            return;
        map.put(k, new CacheEntry(v));
    }

    public V get(K k) {
        CacheEntry cacheEntry = map.get(k);

        if (cacheEntry == null) {
            return null;
        }

        if (System.currentTimeMillis() > cacheEntry.validUntil) {
            map.remove(k);
            return null;
        }

        return cacheEntry.value;
    }

    private void sweep() {
        List<K> remove = new ArrayList<>();

        long now = System.currentTimeMillis();

        for (Map.Entry<K, CacheEntry> entry : map.entrySet()) {
            if (now > entry.getValue().validUntil) {
                remove.add(entry.getKey());
            }
        }

        for (K k : remove) {
            map.remove(k);
        }
    }

    private class CacheEntry {

        private final V value;
        private final long validUntil;

        private CacheEntry(V value) {
            this.value = value;
            this.validUntil = System.currentTimeMillis() + ttl;
        }
    }
}
