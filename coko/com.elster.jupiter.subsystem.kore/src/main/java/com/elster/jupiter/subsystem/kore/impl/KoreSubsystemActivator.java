package com.elster.jupiter.subsystem.kore.impl;

import com.elster.jupiter.system.Component;
import com.elster.jupiter.system.PomSaxParser;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.system.beans.SubsystemImpl;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@org.osgi.service.component.annotations.Component(name = "com.elster.jupiter.subsystem.kore", immediate = true)
public class KoreSubsystemActivator {

    public static final String PLATFORM_ID = "Pulse";
    public static final String PLATFORM_NAME = "Connexo Pulse";

    private volatile SubsystemService subsystemService;
    private Logger logger = Logger.getLogger(KoreSubsystemActivator.class.getName());

    @Activate
    public void activate(BundleContext context) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        PomSaxParser saxp = new PomSaxParser();
        try {
            SAXParser parser = factory.newSAXParser();            
            parser.parse(context.getBundle().getResource("META-INF/maven/com.elster.jupiter.subsystem/kore/pom.xml").toURI().toString(), saxp);
            parser.parse(context.getBundle().findEntries("", "drools-integration*.pom", false).nextElement().toURI().toString(), saxp);
            parser.parse(context.getBundle().findEntries("", "platform*.pom", false).nextElement().toURI().toString(), saxp);
            parser.parse(context.getBundle().findEntries("", "third-party*.pom", false).nextElement().toURI().toString(), saxp);
            parser.parse(context.getBundle().findEntries("", "kore.bom*.pom", false).nextElement().toURI().toString(), saxp);
            parser.parse(context.getBundle().findEntries("", "platform.bom*.pom", false).nextElement().toURI().toString(), saxp);
            parser.parse(context.getBundle().findEntries("", "third-party.bom*.pom", false).nextElement().toURI().toString(), saxp);
        } catch (SAXException e) {
            logger.log(Level.SEVERE, "SAXException");
        } catch (ParserConfigurationException e) {
            logger.log(Level.SEVERE, "ParserConfigurationException");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException");
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "URISyntaxException");
        }

        SubsystemImpl subsystem = new SubsystemImpl(PLATFORM_ID, PLATFORM_NAME, context.getBundle().getVersion().toString());
        List<Component> dependenciesList = saxp.getDependencies().values().stream().map(dependency -> saxp.createComponent(dependency, subsystem)).collect(Collectors.toList());
        subsystem.addComponents(dependenciesList);
        this.subsystemService.registerSubsystem(subsystem);
    }

    @Reference
    public void setSubsystemService(SubsystemService subsystemService) {
        this.subsystemService = subsystemService;
    }
}
