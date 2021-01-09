package de.md5lukas.maven.resolver;

import de.md5lukas.maven.resolver.cache.SimpleSnapshotCache;
import de.md5lukas.maven.resolver.cache.SnapshotCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.net.HttpURLConnection;

public final class SnapshotResolver {

    @Nullable
    private static XMLInputFactory xmlInputFactory = null;

    private static final int SNAPSHOT_SUFFIX_LENGTH = "-SNAPSHOT".length();

    @NotNull
    private static XMLInputFactory getXMLInputFactory() {
        if (xmlInputFactory == null) {
            xmlInputFactory = XMLInputFactory.newInstance();
        }
        return xmlInputFactory;
    }

    private static String removeSnapshotSuffix(String version) {
        return version.substring(0, version.length() - SNAPSHOT_SUFFIX_LENGTH);
    }

    @NotNull
    private final SnapshotCache snapshotCache;

    public SnapshotResolver(@NotNull SnapshotCache snapshotCache) {
        this.snapshotCache = snapshotCache;
    }

    @Nullable
    public String resolveSnapshotVersion(@NotNull Repository repository, @NotNull Artifact artifact) throws Exception {
        String id = artifact.getFuzzyId();

        String snapshotVersion = snapshotCache.get(id);
        if (snapshotVersion != null) {
            return snapshotVersion;
        }

        XMLEventReader reader = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) repository.createURL(artifact.getSnapshotMetadataPath()).openConnection();

            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                connection.disconnect();
                return null;
            }

            reader = getXMLInputFactory().createXMLEventReader(connection.getInputStream());

            String timestamp = null, buildNumber = null;

            while (reader.hasNext()) {
                XMLEvent nextEvent = reader.nextEvent();

                if (nextEvent.isStartElement()) {
                    StartElement startElement = nextEvent.asStartElement();

                    String name = startElement.getName().getLocalPart();

                    if ("timestamp".equals(name)) {
                        nextEvent = reader.nextEvent();
                        timestamp = nextEvent.asCharacters().getData();
                    } else if ("buildNumber".equals(name)) {
                        nextEvent = reader.nextEvent();
                        buildNumber = nextEvent.asCharacters().getData();
                    }

                    if (timestamp != null && buildNumber != null) {
                        break;
                    }
                }
            }

            snapshotVersion = removeSnapshotSuffix(artifact.getVersion()) + '-' + timestamp + '-' + buildNumber;
            snapshotCache.put(id, snapshotVersion);

            return snapshotVersion;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignored) {
                }
            }
        }
    }
}
