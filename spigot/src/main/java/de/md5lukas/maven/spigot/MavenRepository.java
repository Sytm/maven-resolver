package de.md5lukas.maven.spigot;

import java.lang.annotation.*;

/**
 * With this annotation you can define custom repositories in which should be looked for the dependencies.
 */
@Repeatable(MavenRepositoryContainer.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MavenRepository {

    /**
     * The name of the repositories.
     *
     * @return The name
     */
    String name();

    /**
     * The url of the repository. The url should end with a forward slash
     *
     * @return The url of the repository
     */
    String url();
}
