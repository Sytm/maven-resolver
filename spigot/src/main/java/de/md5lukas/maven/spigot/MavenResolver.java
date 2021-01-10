package de.md5lukas.maven.spigot;

import de.md5lukas.maven.resolver.MavenChecksum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required annotation for the maven resolver to work.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MavenResolver {
    /**
     * Shortcut to make the resolver use the maven central repository
     *
     * @return Whether maven central repository should be used or not
     */
    boolean useMavenCentral() default true;

    /**
     * Shortcut to make the resolver use the sonatype repository
     *
     * @return Whether sonatype repository should be used or not
     */
    boolean useSonatype() default false;

    /**
     * Which maven checksum algorithm should be used.
     * <br><br>
     * {@link MavenChecksum#SHA1} is the most common and shouldn't cause any issues
     *
     * @return The checksum algorithm to use
     */
    MavenChecksum checksumAlgorithm() default MavenChecksum.SHA1;

    /**
     * Ignore that a maven artifact has no checksum artifact instead of causing an error.
     * <br>
     * If a checksum is present it will be checked and can cause an exception.
     *
     * @return Whether missing checksums should be ignored or not
     */
    boolean ignoreNotFoundChecksum() default false;
}
