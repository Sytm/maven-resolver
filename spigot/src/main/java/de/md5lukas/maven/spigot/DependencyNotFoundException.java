package de.md5lukas.maven.spigot;

/**
 * Exception that is thrown if a dependency could either not be found or the checksum could not be verified
 */
public final class DependencyNotFoundException extends RuntimeException {

    DependencyNotFoundException(String message) {
        super(message);
    }
}
