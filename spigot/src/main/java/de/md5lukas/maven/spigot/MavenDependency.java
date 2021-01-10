package de.md5lukas.maven.spigot;

import java.lang.annotation.*;

@Repeatable(MavenDependencyContainer.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MavenDependency {

    String groupId();

    String artifactId();

    String version();

    String classifier() default "";

    String type() default "jar";
}
