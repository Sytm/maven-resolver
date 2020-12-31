package de.md5lukas.maven.resolver;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@ToString
public final class Repository {

    @NotNull
    @Getter
    private final String name;

    @NotNull
    @Getter
    private final String baseUrl;

    public Repository(@NotNull String name, @NotNull String url) {
        this.name = name;
        if (!url.endsWith("/"))
            url = url + '/';
        this.baseUrl = url;
    }

    @NotNull
    @SneakyThrows
    public URL resolveURL(@NotNull String path) {
        return new URL(baseUrl + path);
    }

    @NotNull
    public InputStream resolve(@NotNull String path) throws IOException {
        URL target = resolveURL(path);
        HttpURLConnection connection = (HttpURLConnection) target.openConnection();

        connection.connect();

        if (connection.getResponseCode() == 404) {
            throw new FileNotFoundException("Could not find file (404): " + target.toString());
        }

        return connection.getInputStream();
    }
}
