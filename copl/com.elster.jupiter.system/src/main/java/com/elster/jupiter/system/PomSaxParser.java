package com.elster.jupiter.system;

import com.elster.jupiter.system.beans.ComponentImpl;
import com.elster.jupiter.system.beans.SubsystemImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

public class PomSaxParser extends DefaultHandler {
    private Map<String, Dependency> dependencies = new HashMap<>();
    private Dependency dependency;
    private StringBuilder content;
    private static String groupId = "groupId";
    private static String artifactId = "artifactId";
    private static String version = "version";
    private static String dependencyTag = "dependency";
    private boolean inItem = false;

    public PomSaxParser() {
        content = new StringBuilder();
    }

    public Map<String, Dependency> getDependencies() {
        return dependencies;
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        content = new StringBuilder();
        if (qName.equalsIgnoreCase(dependencyTag)) {
            inItem = true;
            dependency = new Dependency();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase(groupId) && inItem) {
            dependency.setGroupId(content.toString());
        } else if (qName.equalsIgnoreCase(artifactId) && inItem) {
            dependency.setArtifactId(content.toString() );
        } else if (qName.equalsIgnoreCase(version) && inItem) {
            dependency.setVersion(content.toString());
        } else if (qName.equalsIgnoreCase(dependencyTag)) {
            inItem = false;
            String key = dependency.getGroupId() + dependency.getArtifactId();
            if (dependencies.get(key) != null) {
                dependencies.get(key).setVersion(dependency.getVersion());
            } else if (dependency.getVersion() == null) {
                dependencies.put(key, dependency);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        content.append(ch, start, length);
    }

    public class Dependency {
        private String groupId;
        private String artifactId;
        private String version;

        public Dependency() {
            setGroupId(null);
            setArtifactId(null);
            setVersion(null);
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }

    public Component createComponent(Dependency dependency, SubsystemImpl subsystem) {
        BundleType bundleType;
        String name;
        if (dependency.getArtifactId().startsWith(dependency.getGroupId())) {
            name = dependency.getArtifactId();
        } else {
            name = dependency.getGroupId() + "." + dependency.getArtifactId();
        }
        String artifactId = dependency.getArtifactId();
        String version = dependency.getVersion();
        if (dependency.getGroupId().startsWith("com.elster.jupiter") || dependency.getGroupId().startsWith("com.energyict")) {
            bundleType = BundleType.APPLICATION_SPECIFIC;
        } else {
            bundleType = BundleType.THIRDPARTY;
        }
        return new ComponentImpl(artifactId, name, version, bundleType, subsystem);
    }
}
