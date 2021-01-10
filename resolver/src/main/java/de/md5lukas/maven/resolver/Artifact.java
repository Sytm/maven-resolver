package de.md5lukas.maven.resolver;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * An instance of this class describes a maven artifact in an undefined repository
 */
@Getter
@EqualsAndHashCode
@ToString
public final class Artifact {

    /**
     * The groupId that was provided at creation
     *
     * @return The groupId of the artifact
     */
    @NotNull
    private final String groupId;
    /**
     * The artifactId that was provided at creation
     *
     * @return The artifactId of the artifact
     */
    @NotNull
    private final String artifactId;
    /**
     * The version that was provided at creation
     *
     * @return The version of the artifact
     */
    @NotNull
    private final String version;
    /**
     * The classifier that was provided at creation
     *
     * @return The classifier of the artifact
     */
    @Nullable
    private final String classifier;
    /**
     * The type that was provided at creation
     *
     * @return The type of the artifact
     */
    @NotNull
    private final String type;

    /**
     * Creates a new maven artifact with the given properties describing its full location
     * <br><br>
     * If the <code>classifier</code> is an empty string it is treated the same as <code>null</code>
     *
     * @param groupId    The groupId of the maven artifact
     * @param artifactId The artifactId of the maven artifact
     * @param version    The version of the maven artifact
     * @param classifier The classifier of the maven artifact
     * @param type       The type of the maven artifact
     */
    public Artifact(@NotNull @NonNull String groupId, @NotNull @NonNull String artifactId,
                    @NotNull @NonNull String version, @Nullable String classifier, @NotNull @NonNull String type) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;

        if ("".equals(classifier))
            classifier = null;
        this.classifier = classifier;


        this.type = type;
    }

    /**
     * Creates a new maven artifact with the given properties.
     * <br><br>
     * For the <code>type</code> property the default value <code>jar</code> is used
     * <br><br>
     * If the <code>classifier</code> is an empty string it is treated the same as <code>null</code>
     *
     * @param groupId    The groupId of the maven artifact
     * @param artifactId The artifactId of the maven artifact
     * @param version    The version of the maven artifact
     * @param classifier The classifier of the maven artifact
     */
    public Artifact(@NotNull String groupId, @NotNull String artifactId, @NotNull String version, @Nullable String classifier) {
        this(groupId, artifactId, version, classifier, "jar");
    }

    /**
     * Creates a new maven artifact with the given properties.
     * <br><br>
     * For the <code>type</code> property the default value <code>"jar"</code> is used<br>
     * For the <code>classifier</code> property the default value <code>null</code> is used
     *
     * @param groupId    The groupId of the maven artifact
     * @param artifactId The artifactId of the maven artifact
     * @param version    The version of the maven artifact
     */
    public Artifact(@NotNull String groupId, @NotNull String artifactId, @NotNull String version) {
        this(groupId, artifactId, version, null);
    }

    /**
     * Checks if the artifact is a snapshot by looking if a <code>"-SNAPSHOT"</code> suffix is present
     *
     * @return If the artifact is a snapshot
     */
    public boolean isSnapshot() {
        return version.toUpperCase(Locale.ROOT).endsWith("-SNAPSHOT");
    }

    /**
     * Creates a new artifact based on the coordinates of this artifact, appending the required suffix to the type to get the
     * coordinates of the checksum artifact
     *
     * @param algorithm The hashing algorithm to get the artifact for
     * @return A new artifact instance describing the coordinates to the artifact containing the checksum
     */
    public Artifact getChecksumArtifact(@NotNull @NonNull MavenChecksum algorithm) {
        return new Artifact(groupId, artifactId, version, classifier, this.type + '.' + algorithm.getTypeSuffix());
    }

    /**
     * Creates a "fuzzy id" of the artifact only containing the <code>groupId</code>, <code>artifactId</code> and <code>version</code>.<br>
     * This "fuzzy id" can be used for caching the repository of an artifact, tolerating changes to the <code>classifier</code> and/or <code>type</code>
     *
     * @return The "fuzzy id" of the artifact
     */
    @NotNull
    public String getFuzzyId() {
        return groupId + ':' + artifactId + ':' + version;
    }

    //<editor-fold desc="Path helpers">

    /**
     * Creates the path for the artifact in a maven repository expecting it to not have a snapshot version.
     *
     * @return The path in the maven repository
     * @throws IllegalStateException If this is a snapshot artifact
     * @see #getPath(String) To get the path if this is a snapshot artifact
     */
    @NotNull
    public String getPath() {
        if (isSnapshot()) {
            throw new IllegalStateException("This artifact is a snapshot artifact and doesn't have a normal path");
        }
        StringBuilder path = new StringBuilder();

        appendBasePath(path);
        appendFileName(path, version);

        return path.toString();
    }

    /**
     * Creates the path for the artifact in maven repository expecting it to have a snapshot version
     *
     * @param snapshotVersion The resolved <code>snapshotVersion</code> of the artifact
     * @return The path in the maven repository
     * @throws IllegalStateException If this is not a snapshot artifact
     * @see #getPath() To get the path if this is not a snapshot artifact
     */
    @NotNull
    public String getPath(@NotNull @NonNull String snapshotVersion) {
        if (!isSnapshot()) {
            throw new IllegalStateException("This artifact is not a snapshot artifact and doesn't have a snapshot path");
        }
        StringBuilder path = new StringBuilder();

        appendBasePath(path);
        appendFileName(path, snapshotVersion);

        return path.toString();
    }

    /**
     * Creates the path for the snapshot metadata for the artifact in the maven repository.
     *
     * @return The path to the snapshot metadata path
     * @throws IllegalStateException If this is not a snapshot artifact
     */
    @NotNull
    public String getSnapshotMetadataPath() {
        if (!isSnapshot()) {
            throw new IllegalStateException("This artifact is not a snapshot artifact and doesn't have a snapshot metadata");
        }
        StringBuilder path = new StringBuilder();

        appendBasePath(path);

        path.append("maven-metadata.xml");

        return path.toString();
    }

    /**
     * Creates the filename for the artifact.
     *
     * @return The filename of the artifact
     */
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

        stringBuilder.append('.').append(type);

        return stringBuilder;
    }
    //</editor-fold>
}
