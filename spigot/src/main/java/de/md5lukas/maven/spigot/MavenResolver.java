package de.md5lukas.maven.spigot;

import de.md5lukas.maven.resolver.MavenChecksum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MavenResolver {
    boolean useMavenCentral() default true;

    boolean useSonatype() default false;

    MavenChecksum checksumAlgorithm() default MavenChecksum.SHA1;

    boolean ignoreNotFoundChecksum() default false;
}
