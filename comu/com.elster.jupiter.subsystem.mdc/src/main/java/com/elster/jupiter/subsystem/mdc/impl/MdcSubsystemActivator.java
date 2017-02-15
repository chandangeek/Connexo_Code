/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.subsystem.mdc.impl;

import com.elster.jupiter.license.License;
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

@org.osgi.service.component.annotations.Component(name = "com.elster.jupiter.subsystem.mdc", immediate = true)
public class MdcSubsystemActivator {

    public static final String MULTISENSE_ID = "MultiSense";
    public static final String MULTISENSE_NAME = "Connexo MultiSense";
    public static final String APPLICATION_KEY = "MDC";

    private static final Logger LOGGER = Logger.getLogger(MdcSubsystemActivator.class.getName());
    private static final String ROOT = "";

    private volatile SubsystemService subsystemService;

    private SubsystemImpl subsystem;

    @Activate
    public void activate(BundleContext context) {
        SubsystemModel model = new SubsystemModel();
        DependenciesParser parser = new DependenciesParser(model);

        loadProperties(parser, findBundleResource(context, ROOT, "third-party-bundles.properties"));
        parse(parser, findBundleResource(context, "META-INF/maven/com.elster.jupiter.subsystem/mdc", "pom.xml"));
        parse(parser, findBundleResource(context, ROOT, "mdc*.pom"));
        model.addDependency(buildSelfComponent(context));

        List<Component> components = model.mergeDependencies();

        subsystem = new SubsystemImpl(MULTISENSE_ID, MULTISENSE_NAME, context.getBundle().getVersion().toString());
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

    @Reference(target = "(com.elster.jupiter.license.application.key=" + APPLICATION_KEY + ")")
    public void setLicense(License license) {
    }

    // We need to add this component manually because the mdc subsystem bundle can't have itself in the list of dependencies
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
