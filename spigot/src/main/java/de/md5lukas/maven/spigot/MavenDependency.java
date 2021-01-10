package de.md5lukas.maven.spigot;

import java.lang.annotation.*;

/**
 * A required maven dependency that should get downloaded and loaded into JVM
 */
@Repeatable(MavenDependencyContainer.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MavenDependency {

    /**
     * The groupId of the maven artifact in the repository
     *
     * @return The groupId
     */
    String groupId();

    /**
     * The artifactId of the maven artifact in the repository
     *
     * @return The artifactId
     */
    String artifactId();

    /**
     * The version of the maven artifact in the repository
     *
     * @return The version
     */
    String version();

    /**
     * The classifier of the maven artifact in the repository
     *
     * @return The classifier
     */
    String classifier() default "";

    /**
     * The type of the maven artifact in the repository
     *
     * @return The type
     */
    String type() default "jar";
}
