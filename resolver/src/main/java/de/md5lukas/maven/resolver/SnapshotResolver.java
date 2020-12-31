package de.md5lukas.maven.resolver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SnapshotResolver {

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
    public static String resolveSnapshotVersion(Repository repository, Artifact artifact) throws IOException, XMLStreamException {
        XMLEventReader reader = null;
        try {
            reader = getXMLInputFactory().createXMLEventReader(repository.resolve(artifact.getSnapshotMetadataPath()));

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

            return removeSnapshotSuffix(artifact.getVersion()) + '-' + timestamp + '-' + buildNumber;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
