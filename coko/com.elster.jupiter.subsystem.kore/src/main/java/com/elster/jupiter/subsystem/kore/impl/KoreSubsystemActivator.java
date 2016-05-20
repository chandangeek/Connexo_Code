package com.elster.jupiter.subsystem.kore.impl;

import com.elster.jupiter.system.BundleType;
import com.elster.jupiter.system.Component;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.system.beans.ComponentImpl;
import com.elster.jupiter.system.beans.SubsystemImpl;
import com.elster.jupiter.system.utils.DependenciesParser;
import com.elster.jupiter.system.utils.SubsystemModel;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@org.osgi.service.component.annotations.Component(name = "com.elster.jupiter.subsystem.kore", immediate = true)
public class KoreSubsystemActivator {

    public static final String PLATFORM_ID = "Pulse";
    public static final String PLATFORM_NAME = "Connexo Pulse";

    private static final Logger LOGGER = Logger.getLogger(KoreSubsystemActivator.class.getName());
    private static final String ROOT = "";

    private volatile SubsystemService subsystemService;
    private SubsystemImpl subsystem;

    @Activate
    public void activate(BundleContext context) {
        SubsystemModel model = new SubsystemModel();
        DependenciesParser parser = new DependenciesParser(model);

        loadProperties(parser, findBundleResource(context, ROOT, "third-party-bundles.properties"));
        parse(parser, findBundleResource(context, "META-INF/maven/com.elster.jupiter.subsystem/kore", "pom.xml"));
        parse(parser, findBundleResource(context, ROOT, "kore*.pom"));
        parse(parser, findBundleResource(context, ROOT, "platform*.pom"));
        model.addDependency(buildSelfComponent(context));

        List<Component> components = model.mergeDependencies();

        subsystem = new SubsystemImpl(PLATFORM_ID, PLATFORM_NAME, context.getBundle().getVersion().toString());
        subsystem.addComponents(components);
        this.subsystemService.registerSubsystem(subsystem);
    }

    @Deactivate
    public void deactivate(BundleContext context) {
        this.subsystemService.unregisterSubsystem(subsystem);
    }

    @Reference
    public void setSubsystemService(SubsystemService subsystemService) {
        this.subsystemService = subsystemService;
    }

    // We need to add this component manually because the kore subsystem bundle can't have itself in the list of dependencies
    private ComponentImpl buildSelfComponent(BundleContext context) {
        ComponentImpl self = new ComponentImpl();
        self.setSymbolicName(context.getBundle().getSymbolicName());
        self.setVersion(context.getBundle().getVersion().toString());
        self.setBundleType(BundleType.APPLICATION_SPECIFIC);
        return self;
    }

    private void loadProperties(DependenciesParser parser, URL url) {
        try {
            parser.loadThirdPartyBundlesProperties(url);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to load properties file [url=" + url.toString() + "]", e);
        }
    }

    private void parse(DependenciesParser parser, URL url) {
        if (url != null) {
            try {
                parser.parse(url.toURI().toString());
            } catch (URISyntaxException | ParserConfigurationException | SAXException | IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to parse file [url=" + url.toString() + "]", e);
            }
        }
    }

    private URL findBundleResource(BundleContext context, String path, String filePattern) {
        Enumeration<URL> entries = context.getBundle().findEntries(path, filePattern, false);
        if (entries != null && entries.hasMoreElements()) {
            return entries.nextElement();
        }
        LOGGER.log(Level.SEVERE, "Unable to find resource [path=" + path + ", filePattern=" + filePattern + "]");
        return null;
    }
}
