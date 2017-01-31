/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system.utils;

import com.elster.jupiter.system.BundleType;
import com.elster.jupiter.system.Component;
import com.elster.jupiter.system.beans.ComponentImpl;
import com.elster.jupiter.util.UpdatableHolder;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DependenciesParser {

    private SubsystemModel model;

    public DependenciesParser(SubsystemModel model) {
        this.model = model;
    }

    public void loadThirdPartyBundlesProperties(URL url) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            List<Component> thirdParties = properties.entrySet().stream().map(entry -> {
                ComponentImpl component = new ComponentImpl();
                component.setSymbolicName((String) entry.getKey());
                component.setVersion((String) entry.getValue());
                component.setBundleType(BundleType.THIRD_PARTY);
                return component;
            }).collect(Collectors.toList());
            model.addThirdParties(thirdParties);
        }
    }

    public void parse(String uri) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(uri, new XmlHandler(model));
    }

    private static class XmlHandler extends DefaultHandler {

        Stack<XmlNode> stack = new Stack<>();
        XmlNode currentNode;

        XmlHandler(SubsystemModel model) {
            this.currentNode = new BootstrapNode(model);
        }

        @Override
        public void startElement(String arg0, String arg1, String xmlTag, Attributes xmlAttrs) throws SAXException {
            stack.push(currentNode);
            currentNode = currentNode.getSubnode(xmlTag, xmlAttrs);
        }

        @Override
        public void endElement(String arg0, String arg1, String xmlTag) throws SAXException {
            currentNode.complete();
            currentNode = stack.pop();
        }

        @Override
        public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
            String value = String.valueOf(arg0).substring(arg1, arg1 + arg2);
            currentNode.setValue(value);
        }
    }

    static abstract class XmlNode {

        XmlNode getSubnode(String tag, Attributes attributes) {
            return new UnknownXmlNode();
        }

        void setValue(String value) {
        }

        void complete() {
        }
    }

    static class UnknownXmlNode extends XmlNode {
    }

    static class BootstrapNode extends XmlNode {

        SubsystemModel model;

        public BootstrapNode(SubsystemModel model) {
            this.model = model;
        }

        @Override
        XmlNode getSubnode(String tag, Attributes attributes) {
            if (MavenProjectNode.TAG.equals(tag)) {
                return new MavenProjectNode(model);
            }
            return super.getSubnode(tag, attributes);
        }
    }

    static class MavenProjectNode extends XmlNode {

        static final String TAG = "project";

        SubsystemModel model;
        List<Component> dependencies = new ArrayList<>();
        List<Component> versionedDependencies = new ArrayList<>();
        Properties mavenProperties = new Properties();

        public MavenProjectNode(SubsystemModel model) {
            this.model = model;
        }

        @Override
        XmlNode getSubnode(String tag, Attributes attributes) {
            if (PropertiesNode.TAG.equals(tag)) {
                return new PropertiesNode(mavenProperties);
            }
            if (DependencyManagementNode.TAG.equals(tag)) {
                return new DependencyManagementNode(versionedDependencies, mavenProperties);
            }
            if (DependenciesNode.TAG.equals(tag)) {
                return new DependenciesNode(dependencies, mavenProperties);
            }
            return super.getSubnode(tag, attributes);
        }

        @Override
        void complete() {
            model.addDependencies(dependencies);
            model.addVersionedDependencies(versionedDependencies);
        }
    }

    static class PropertiesNode extends XmlNode {

        static final String TAG = "properties";

        Properties properties;

        PropertiesNode(Properties properties) {
            this.properties = properties;
        }

        @Override
        XmlNode getSubnode(String tag, Attributes attributes) {
            return new PropertyNode(tag, properties);
        }
    }

    static class PropertyNode extends XmlNode {

        String tag;
        Properties properties;

        PropertyNode(String tag, Properties properties) {
            this.tag = tag;
            this.properties = properties;
        }

        @Override
        void setValue(String value) {
            properties.put(tag, value);
        }
    }

    static class DependencyManagementNode extends XmlNode {

        static final String TAG = "dependencyManagement";

        List<Component> versionedDependencies;
        Properties properties;

        public DependencyManagementNode(List<Component> versionedDependencies, Properties properties) {
            this.versionedDependencies = versionedDependencies;
            this.properties = properties;
        }

        @Override
        XmlNode getSubnode(String tag, Attributes attributes) {
            if (DependenciesNode.TAG.equals(tag)) {
                return new DependenciesNode(versionedDependencies, properties);
            }
            return super.getSubnode(tag, attributes);
        }
    }

    static class DependenciesNode extends XmlNode {

        static final String TAG = "dependencies";

        List<Component> dependencies;
        Properties properties;

        public DependenciesNode(List<Component> dependencies, Properties properties) {
            this.dependencies = dependencies;
            this.properties = properties;
        }

        @Override
        XmlNode getSubnode(String tag, Attributes attributes) {
            if (DependencyNode.TAG.equals(tag)) {
                return new DependencyNode(dependencies, properties);
            }
            return super.getSubnode(tag, attributes);
        }
    }

    static class DependencyNode extends XmlNode {

        static final String TAG = "dependency";

        List<Component> dependencies;
        Properties properties;
        UpdatableHolder<String> groupId = new UpdatableHolder<>("");
        UpdatableHolder<String> artifactId = new UpdatableHolder<>("");
        ComponentImpl component = new ComponentImpl();

        public DependencyNode(List<Component> dependencies, Properties properties) {
            this.dependencies = dependencies;
            this.properties = properties;
        }

        @Override
        XmlNode getSubnode(String tag, Attributes attributes) {
            if (GroupIdNode.TAG.equals(tag)) {
                return new GroupIdNode(groupId);
            }
            if (ArtifactIdNode.TAG.equals(tag)) {
                return new ArtifactIdNode(artifactId);
            }
            if (VersionNode.TAG.equals(tag)) {
                return new VersionNode(component, properties);
            }
            if (TypeNode.TAG.equals(tag)) {
                return new TypeNode(component);
            }
            return super.getSubnode(tag, attributes);
        }

        @Override
        void complete() {
            if (component.getBundleType() == BundleType.NOT_APPLICABLE) {
                return;
            }
            component.setSymbolicName(OsgiUtils.getOsgiBundleName(groupId.get(), artifactId.get()));
            if (component.getSymbolicName().startsWith("com.elster.jupiter") || component.getSymbolicName().startsWith("com.energyict")) {
                component.setBundleType(BundleType.APPLICATION_SPECIFIC);
            } else {
                component.setBundleType(BundleType.THIRD_PARTY);
            }
            dependencies.add(component);
        }
    }

    static class GroupIdNode extends XmlNode {

        static final String TAG = "groupId";

        UpdatableHolder<String> groupId;

        public GroupIdNode(UpdatableHolder<String> groupId) {
            this.groupId = groupId;
        }

        @Override
        void setValue(String value) {
            groupId.update(value);
        }
    }

    static class ArtifactIdNode extends XmlNode {

        static final String TAG = "artifactId";

        UpdatableHolder<String> artifactId;

        public ArtifactIdNode(UpdatableHolder<String> artifactId) {
            this.artifactId = artifactId;
        }

        @Override
        void setValue(String value) {
            artifactId.update(value);
        }
    }

    static class VersionNode extends XmlNode {
        static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{([a-z\\.]+)\\}");//${version.version}
        static final Pattern REVISION_PATTERN = Pattern.compile("(.*)\\$\\{([a-z\\.]+)\\}");
        static final String TAG = "version";

        ComponentImpl component;
        Properties properties;

        public VersionNode(ComponentImpl component, Properties properties) {
            this.component = component;
            this.properties = properties;
        }

        @Override
        void setValue(String value) {
            component.setVersion(getVersion(value));
        }

        private String getVersion(String mavenVersion) {
            Matcher revisionMatcher = REVISION_PATTERN.matcher(mavenVersion);
            if (revisionMatcher.matches()) {
                String group = revisionMatcher.group(2);
                String revision = properties.getProperty(group, "");
                mavenVersion = revisionMatcher.group(1) + revision;
            }
            Matcher matcher = PROPERTY_PATTERN.matcher(mavenVersion);
            if (matcher.matches()) {
                String property = matcher.group(1);
                mavenVersion = properties.getProperty(property, mavenVersion);
            }
            return OsgiUtils.getOsgiVersion(mavenVersion);
        }

    }

    static class TypeNode extends XmlNode {

        static final String TAG = "type";

        ComponentImpl component;

        public TypeNode(ComponentImpl component) {
            this.component = component;
        }

        @Override
        void setValue(String value) {
            if (!"jar".equals(value)) {
                component.setBundleType(BundleType.NOT_APPLICABLE);
            }
        }
    }
}
