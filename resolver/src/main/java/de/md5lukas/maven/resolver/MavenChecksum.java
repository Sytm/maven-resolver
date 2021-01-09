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
    SHA1("sha1", () -> MessageDigest.getInstance("SHA-1"));

    @Getter
    @NotNull
    private final String extensionAppendix;

    @NotNull
    private final MessageDigestSupplier messageDigestSupplier;

    @SneakyThrows // MD5 and SHA-1 are present
    @NotNull
    public MessageDigest getMessageDigest() {
        return messageDigestSupplier.get();
    }

    @FunctionalInterface
    private interface MessageDigestSupplier {
        MessageDigest get() throws NoSuchAlgorithmException;
    }
}
