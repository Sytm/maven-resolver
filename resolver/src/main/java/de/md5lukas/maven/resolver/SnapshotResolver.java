package de.md5lukas.maven.resolver;

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
    private final SimpleCache<String, String> snapshotVersionCache;

    public SnapshotResolver() {
        this.snapshotVersionCache = new SimpleCache<>();
    }

    public void setCacheTTL(long ttl) {
        snapshotVersionCache.setTtl(ttl);
    }

    @NotNull
    public ResolveResult<String> resolveSnapshotVersion(@NotNull Repository repository, @NotNull Artifact artifact) {
        String id = artifact.getFuzzyId();

        String snapshotVersion = snapshotVersionCache.get(id);
        if (snapshotVersion != null) {
            return ResolveResult.success(snapshotVersion);
        }

        XMLEventReader reader = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) repository.createURL(artifact.getSnapshotMetadataPath()).openConnection();

            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                connection.disconnect();
                return ResolveResult.notFound();
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
            snapshotVersionCache.put(id, snapshotVersion);

            return ResolveResult.success(snapshotVersion);
        } catch (Exception e) {
            return ResolveResult.error(e);
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
