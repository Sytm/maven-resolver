package de.md5lukas.maven.resolver;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Locale;

@Getter
@ToString
public final class Artifact {

    @NotNull
    private final Repository repository;
    @NotNull
    private final String groupId;
    @NotNull
    private final String artifactId;
    @NotNull
    private final String version;
    @Nullable
    private final String classifier;
    @NotNull
    private final String extension;

    public Artifact(@NotNull @NonNull Repository repository, @NotNull @NonNull String groupId, @NotNull @NonNull String artifactId,
                    @NotNull @NonNull String version, @Nullable String classifier, @NotNull @NonNull String extension) {
        this.repository = repository;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;

        if ("".equals(classifier))
            classifier = null;
        this.classifier = classifier;

        this.extension = extension;
    }

    public Artifact(@NotNull Repository repository, @NotNull String groupId,
                    @NotNull String artifactId, @NotNull String version, @Nullable String classifier) {
        this(repository, groupId, artifactId, version, classifier, "jar");
    }

    public Artifact(@NotNull Repository repository, @NotNull String groupId, @NotNull String artifactId, @NotNull String version) {
        this(repository, groupId, artifactId, version, null);
    }

    public boolean isSnapshot() {
        return version.toUpperCase(Locale.ROOT).endsWith("-SNAPSHOT");
    }

    @Nullable
    private String snapshotVersion = null;

    @NotNull
    public String getSnapshotVersion() throws IOException, XMLStreamException {
        if (!isSnapshot())
            throw new IllegalArgumentException("Only artifacts that are a snapshot can have a snapshot version");

        if (snapshotVersion == null) {
            snapshotVersion = SnapshotResolver.resolveSnapshotVersion(repository, this);
        }

        return snapshotVersion;
    }

    @Nullable
    private URL url = null;

    @NotNull
    public URL getURL() throws IOException, XMLStreamException {
        if (url == null) {
            if (isSnapshot()) {
                url = repository.resolveURL(getPath(getSnapshotVersion()));
            } else {
                url = repository.resolveURL(getPath());
            }
        }

        return url;
    }

    @NotNull
    public InputStream openStream() throws IOException, XMLStreamException {
        return getURL().openStream();
    }

    public void downloadToFile(File target) throws IOException, XMLStreamException {
        try (ReadableByteChannel inputChannel = Channels.newChannel(openStream());
             FileChannel outputChannel = new FileOutputStream(target).getChannel()) {
            outputChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE);
        }
    }

    public void downloadToFolder(File folder) throws IOException, XMLStreamException {
        downloadToFile(new File(folder, getFileName()));
    }

    //<editor-fold desc="Path helpers">
    @NotNull
    public String getPath() {
        StringBuilder path = new StringBuilder();

        appendBasePath(path);
        appendFileName(path, version);

        return path.toString();
    }

    @NotNull
    public String getPath(@NotNull String snapshotVersion) {
        StringBuilder path = new StringBuilder();

        appendBasePath(path);
        appendFileName(path, snapshotVersion);

        return path.toString();
    }

    @NotNull
    public String getSnapshotMetadataPath() {
        StringBuilder path = new StringBuilder();

        appendBasePath(path);

        path.append("maven-metadata.xml");

        return path.toString();
    }

    @NotNull
    public String getFileName() {
        return appendFileName(new StringBuilder(), version).toString();
    }

    private void appendBasePath(StringBuilder stringBuilder) {
        for (String groupIdPart : groupId.split("\\.")) {
            stringBuilder.append(groupIdPart).append('/');
        }

        stringBuilder.append(artifactId).append('/');
        stringBuilder.append(version).append('/');
    }

    private StringBuilder appendFileName(@NotNull StringBuilder stringBuilder, @NotNull String version) {
        stringBuilder.append(artifactId).append('-').append(version);

        if (classifier != null) {
            stringBuilder.append('-').append(classifier);
        }

        stringBuilder.append('.').append(extension);

        return stringBuilder;
    }
    //</editor-fold>
}
