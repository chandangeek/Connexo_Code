package com.energyict.mdc.multisense.api.redknee;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.Collections;
import java.util.List;

public class Configuration {

    private Integer outputFrequency=0;
    private Integer timeAcceleration=60;
    private String destinationFilePath="/tmp";
    private String connexoUrl="http://localhost:8080/";
    private Integer simulatorPort=8080;

    @XStreamImplicit
    private List<UsagePoint> usagePoints = Collections.emptyList();

    public Configuration() {

    }

    Configuration(Integer outputFrequency, Integer timeAcceleration, String destinationFilePath, String connexoUrl, Integer simulatorPort, List<UsagePoint> usagePoints) {
        this.outputFrequency = outputFrequency;
        this.timeAcceleration = timeAcceleration;
        this.destinationFilePath = destinationFilePath;
        this.connexoUrl = connexoUrl;
        this.simulatorPort = simulatorPort;
        this.usagePoints = usagePoints;
    }

    public Integer getTimeAcceleration() {
        return timeAcceleration;
    }

    public Integer getOutputFrequency() {
        return outputFrequency;
    }

    public Integer getSimulatorPort() {
        return simulatorPort;
    }

    public String getDestinationFilePath() {
        return destinationFilePath;
    }

    public String getConnexoUrl() {
        return connexoUrl;
    }

    public void setDestinationFilePath(String destinationFilePath) {
        this.destinationFilePath = destinationFilePath;
    }

    public List<UsagePoint> getUsagePoints() {
        return usagePoints;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "\n\toutputFrequency= every " + outputFrequency +"s"+
                "\n\ttimeAcceleration= x" + timeAcceleration +
                ",\n\tdestinationFilePath='" + destinationFilePath + '\'' +
                ",\n\tconnexoUrl='" + connexoUrl + '\'' +
                ",\n\tsimulator port='" + simulatorPort + '\'' +
                ",\n\tusagePoints=" + usagePoints +
                '}';
    }
}
