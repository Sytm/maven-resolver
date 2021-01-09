package de.md5lukas.maven.spigot;

public final class DependencyNotFoundException extends RuntimeException {

    DependencyNotFoundException(String message) {
        super(message);
    }
}
