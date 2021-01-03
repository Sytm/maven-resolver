package de.md5lukas.maven.resolver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public enum MavenChecksum {

    MD5("md5"),
    SHA1("sha1");

    @Getter
    @NotNull
    private final String extensionAppendix;
}
