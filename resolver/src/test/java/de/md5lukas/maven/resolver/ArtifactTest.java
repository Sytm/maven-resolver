package de.md5lukas.maven.resolver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArtifactTest {

    @Test
    void defaultTypeIsJar() {
        assertEquals("jar", new Artifact("", "", "").getType());
    }

    @Test
    void detectsSnapshotProperlyTrue() {
        assertTrue(new Artifact("", "", "1.0.0-SNAPSHOT").isSnapshot());
    }

    @Test
    void detectsSnapshotProperlyFalse() {
        assertFalse(new Artifact("", "", "1.0.0").isSnapshot());
    }

    @Test
    void fileNameIsProperlyCreated() {
        String fileName = new Artifact("de.md5lukas.maven", "resolver", "1.0.0").getFileName();
        assertEquals("resolver-1.0.0.jar", fileName);
    }

    @Test
    void pathIsProperlyCreated() {
        String path = new Artifact("de.md5lukas.maven", "resolver", "1.0.0").getPath();
        assertEquals("de/md5lukas/maven/resolver/1.0.0/resolver-1.0.0.jar", path);
    }

    @Test
    void classifierIsProperlyAppended() {
        String path = new Artifact("de.md5lukas.maven", "resolver", "1.0.0", "sources").getPath();
        assertEquals("de/md5lukas/maven/resolver/1.0.0/resolver-1.0.0-sources.jar", path);
    }

    @Test
    void extensionIsProperlyModified() {
        String path = new Artifact("de.md5lukas.maven", "resolver", "1.0.0", null, "pom").getPath();
        assertEquals("de/md5lukas/maven/resolver/1.0.0/resolver-1.0.0.pom", path);
    }

    @Test
    void snapshotMetadataPathIsProperlyCreated() {
        String path = new Artifact("de.md5lukas.maven", "resolver", "1.0.0-SNAPSHOT").getSnapshotMetadataPath();
        assertEquals("de/md5lukas/maven/resolver/1.0.0-SNAPSHOT/maven-metadata.xml", path);
    }

    @Test
    void snapshotPathIsProperlyCreated() {
        String path = new Artifact("de.md5lukas.maven", "resolver", "1.0.0-SNAPSHOT").getPath("1.0.0-1234-1");
        assertEquals("de/md5lukas/maven/resolver/1.0.0-SNAPSHOT/resolver-1.0.0-1234-1.jar", path);
    }
}
