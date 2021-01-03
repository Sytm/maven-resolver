package de.md5lukas.maven.resolver;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Locale;

@Getter
@EqualsAndHashCode
@ToString
public final class Artifact {

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

    public Artifact(@NotNull @NonNull String groupId, @NotNull @NonNull String artifactId,
                    @NotNull @NonNull String version, @Nullable String classifier, @NotNull @NonNull String extension) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;

        if ("".equals(classifier))
            classifier = null;
        this.classifier = classifier;

        this.extension = extension;
    }

    public Artifact(@NotNull String groupId, @NotNull String artifactId, @NotNull String version, @Nullable String classifier) {
        this(groupId, artifactId, version, classifier, "jar");
    }

    public Artifact(@NotNull String groupId, @NotNull String artifactId, @NotNull String version) {
        this(groupId, artifactId, version, null);
    }

    public boolean isSnapshot() {
        return version.toUpperCase(Locale.ROOT).endsWith("-SNAPSHOT");
    }

    public Artifact getChecksumArtifact(@NotNull @NonNull MavenChecksum type) {
        return new Artifact(groupId, artifactId, version, classifier, extension + '.' + type.getExtensionAppendix());
    }

    @NotNull
    public String getFuzzyId() {
        return groupId + '/' + artifactId + '/' + version;
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
