package de.md5lukas.maven.resolver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
public enum MavenChecksum {

    MD5("md5", () -> MessageDigest.getInstance("MD5")),
    SHA1("sha1", () -> MessageDigest.getInstance("SHA-1")),
    SHA256("sha256", () -> MessageDigest.getInstance("SHA-256")),
    SHA512("sha512", () -> MessageDigest.getInstance("SHA-512"));

    @Getter
    @NotNull
    private final String extensionAppendix;

    @NotNull
    private final MessageDigestSupplier messageDigestSupplier;

    @SneakyThrows // MD5, SHA-1, SHA-256 and SHA-512 are normally present
    @NotNull
    public MessageDigest getMessageDigest() {
        return messageDigestSupplier.get();
    }

    @FunctionalInterface
    private interface MessageDigestSupplier {
        @NotNull
        MessageDigest get() throws NoSuchAlgorithmException;
    }
}
