package de.md5lukas.maven.resolver;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * Class defining a remote maven repository
 */
@ToString
@Getter
public final class Repository {

    /**
     * Singleton instance for the maven central repository
     */
    public static final Repository MAVEN_CENTRAL = new Repository("central", "https://repo1.maven.org/maven2/");
    /**
     * Singleton instance for the sonatype repository
     */
    public static final Repository SONATYPE = new Repository("sonatype", "https://oss.sonatype.org/content/groups/public/");

    /**
     * The name of the repository
     *
     * @return The name of the repository that was provided at creation
     */
    @NotNull
    private final String name;

    /**
     * The URL that points to the root of the maven repository
     *
     * @return The parsed URL of the repository that was provided at creation
     */
    @NotNull
    private final URL url;

    /**
     * Creates a new repository instance.
     * <br><br>
     * The constructor checks if the provided <code>url</code> ends with a <code>/</code> and appends one if one isn't present
     *
     * @param name The name of the repository, primary use is for readability purposes
     * @param url The url of the maven repository
     * @throws UnsupportedOperationException If the url does not use the <code>http</code> or <code>https</code> protocol
     */
    @SneakyThrows
    public Repository(@NotNull @NonNull String name, @NotNull @NonNull String url) {
        this.name = name;
        if (!url.endsWith("/"))
            url = url + '/';
        this.url = new URL(url);
        if (!("http".equals(this.url.getProtocol()) || "https".equals(this.url.getProtocol()))) {
            throw new UnsupportedOperationException("The maven repository only supports repository over http or https");
        }
    }

    /**
     * Basically appends the path to the {@link #getUrl() URL} of the repository.
     *
     * @param path The path to append
     * @return The created url in the repository
     */
    @NotNull
    @SneakyThrows
    public URL createURL(@NotNull @NonNull String path) {
        return new URL(this.url, path);
    }
}
