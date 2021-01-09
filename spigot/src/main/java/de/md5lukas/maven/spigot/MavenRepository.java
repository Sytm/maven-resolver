package de.md5lukas.maven.spigot;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Repeatable(MavenRepositoryContainer.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MavenRepository {

    @NotNull
    String name();

    @NotNull
    String url();
}
