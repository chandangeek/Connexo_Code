package com.energyict.mdc.multisense.api.redknee;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by bvn on 9/17/15.
 */
public class Simulator {
    public static void main(String[] args) {
        try {
            Simulator simulator = new Simulator();
            Configuration configuration = simulator.readConfiguration("simulator.json");
            System.out.println(configuration);
            ConsumptionExportGenerator generator = new ConsumptionExportGenerator();
            generator.setConfiguration(configuration);
            generator.start();
        } catch (FileNotFoundException e) {
            System.err.println("simulator.json not found");
        } catch (IOException e) {
            System.err.println("Error while reading simulator.json: " + e);
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
}
