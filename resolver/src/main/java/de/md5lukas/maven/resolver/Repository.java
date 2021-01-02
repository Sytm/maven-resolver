package de.md5lukas.maven.resolver;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

@ToString
@Getter
public final class Repository {

    @NotNull
    private final String name;

    @NotNull
    private final URL url;

    @SneakyThrows
    public Repository(@NotNull String name, @NotNull String url) {
        this.name = name;
        if (!url.endsWith("/"))
            url = url + '/';
        this.url = new URL(url);
    }

    @NotNull
    @SneakyThrows
    public URL createURL(@NotNull String path) {
        return new URL(url + path);
    }
}
