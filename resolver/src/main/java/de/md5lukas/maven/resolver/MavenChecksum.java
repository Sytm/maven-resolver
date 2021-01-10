package de.md5lukas.maven.resolver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Enum containing the most common Checksum algorithms used by maven
 */
@RequiredArgsConstructor
public enum MavenChecksum {

    MD5("md5", () -> MessageDigest.getInstance("MD5")),
    SHA1("sha1", () -> MessageDigest.getInstance("SHA-1")),
    SHA256("sha256", () -> MessageDigest.getInstance("SHA-256")),
    SHA512("sha512", () -> MessageDigest.getInstance("SHA-512"));

    /**
     * The type suffix that should get appended to the type of an artifact to get the checksum artifact
     *
     * @return The type suffix
     */
    @Getter
    @NotNull
    private final String typeSuffix;

    @NotNull
    private final MessageDigestSupplier messageDigestSupplier;

    /**
     * Creates a new MessageDigest instance and returns it.
     * <br><br>
     * Throws a {@link NoSuchAlgorithmException} as an unchecked exception if it should occur.
     *
     * @return The message digest for this MavenChecksum
     */
    @SneakyThrows // MD5, SHA-1, SHA-256 and SHA-512 are normally present
    @NotNull
    public MessageDigest getMessageDigest() {
        return getMessageDigestChecked();
    }

    /**
     * Creates a new MessageDigest instance and returns it.
     *
     * @return The message digest for this MavenChecksum
     * @throws NoSuchAlgorithmException If the hasing algorithm is not present
     */
    @NotNull
    public MessageDigest getMessageDigestChecked() throws NoSuchAlgorithmException {
        return messageDigestSupplier.get();
    }

    @FunctionalInterface
    private interface MessageDigestSupplier {
        @NotNull
        MessageDigest get() throws NoSuchAlgorithmException;
    }
}
