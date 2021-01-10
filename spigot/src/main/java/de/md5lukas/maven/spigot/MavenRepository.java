package de.md5lukas.maven.spigot;

import java.lang.annotation.*;

@Repeatable(MavenRepositoryContainer.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MavenRepository {

    String name();

    String url();
}
