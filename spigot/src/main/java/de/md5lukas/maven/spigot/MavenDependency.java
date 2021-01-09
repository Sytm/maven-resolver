package de.md5lukas.maven.spigot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.*;

@Repeatable(MavenDependencyContainer.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MavenDependency {

    @NotNull
    String groupId();

    @NotNull
    String artifactId();

    @NotNull
    String version();

    @Nullable
    String classifier() default "";
}
