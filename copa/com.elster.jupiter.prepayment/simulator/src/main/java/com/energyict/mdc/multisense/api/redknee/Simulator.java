/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.redknee;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by bvn on 9/17/15.
 */
public class Simulator {
    private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

    public static void main(String[] args) {
        try {
            Simulator simulator = new Simulator();
            Configuration configuration = simulator.readConfiguration(args.length > 0 ? args[0] : "simulator.json");
            logger.info(configuration.toString());
            ConsumptionExportGenerator generator = new ConsumptionExportGenerator(Clock.systemDefaultZone(), new ScheduledThreadPoolExecutor(1));
            if (!configuration.getConnexoUrl().isPresent()) {
                logger.info("No url for proxy: will not proxy requests to connexo");
            }
            RknApplication rknApplication = new RknApplication(generator, configuration);
            ResourceConfig resourceConfig = ResourceConfig.forApplication(rknApplication);
            resourceConfig.register(JacksonFeature.class);
            generator.setConfiguration(configuration);
            generator.start();
            simulator.startJetty(resourceConfig, configuration.getSimulatorPort());
        } catch (FileNotFoundException e) {
            logger.error("Configuration not found.");
            logger.error("Either make sure the current directory contains the configuration file 'simulator.json' or provide the file as argument to the simulator.");
        } catch (IOException e) {
            logger.error("Error while reading configuration: " + e);
        } catch (Exception e) {
            logger.error("Failed to start simulator: " + e);
        }
    }

    public Configuration readConfiguration(String fileName) throws IOException {
        try (FileReader settings = new FileReader(fileName)) {
            XStream xstream = new XStream(new JettisonMappedXmlDriver());
            xstream.alias("simulator", Configuration.class);
            xstream.processAnnotations(Configuration.class);
            return (Configuration) xstream.fromXML(settings);
        }
    }

    /**
     * https://nikolaygrozev.wordpress.com/2014/10/16/rest-with-embedded-jetty-and-jersey-in-a-single-jar-step-by-step/
     * @throws Exception
     * @param rknApplication
     * @param port
     */
    public void startJetty(Application rknApplication, int port) throws Exception {

        URI baseUri = UriBuilder.fromUri("http://localhost/").port(port).build();
        ResourceConfig config = ResourceConfig.forApplication(rknApplication);

        Server jettyServer = JettyHttpContainerFactory.createServer(baseUri, config);

        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }

}
