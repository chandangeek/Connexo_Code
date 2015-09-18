package com.energyict.mdc.multisense.api.redknee;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

/**
 * Created by bvn on 9/17/15.
 */
public class Simulator {
    public static void main(String[] args) {
        try {
            Simulator simulator = new Simulator();
            Configuration configuration = simulator.readConfiguration(args.length>0?args[0]:"simulator.json");
            System.out.println(configuration);
            ConsumptionExportGenerator generator = new ConsumptionExportGenerator();
            RknApplication rknApplication = new RknApplication(generator);
            ResourceConfig resourceConfig = ResourceConfig.forApplication(rknApplication);
            resourceConfig.register(JacksonFeature.class);
            resourceConfig.register(ObjectMapper.class);
            generator.setConfiguration(configuration);
            generator.start();
            simulator.startJetty(resourceConfig);
        } catch (FileNotFoundException e) {
            System.err.println("simulator.json not found");
        } catch (IOException e) {
            System.err.println("Error while reading simulator.json: " + e);
        } catch (Exception e) {
            System.err.println("Failed to start Jetty: " + e);
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
     */
    public void startJetty(Application rknApplication) throws Exception {

        URI baseUri = UriBuilder.fromUri("http://localhost/").port(8080).build();
        ResourceConfig config = ResourceConfig.forApplication(rknApplication);

        Server jettyServer = JettyHttpContainerFactory.createServer(baseUri, config);



//        Server jettyServer = new Server(8080);
//        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        context.setContextPath("/");
//
//        jettyServer.setHandler(context);
//
//        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
//        jerseyServlet.setInitOrder(0);
//
//        // Tells the Jersey Servlet which REST service/class to load.
//        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", RknProxyResource.class.getCanonicalName());
        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }

}
