/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.redknee;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Configuration {

    private Integer outputFrequency=0;
    private Integer timeAcceleration=60;
    private String destinationFilePath="/tmp";
    private String connexoUrl=null;
    private Integer simulatorPort=8080;
    private String readingType;
    @XStreamImplicit
    private List<UsagePoint> usagePoints = Collections.emptyList();

    public Configuration() {

    }

    Configuration(Integer outputFrequency, Integer timeAcceleration, String destinationFilePath, String connexoUrl, Integer simulatorPort, List<UsagePoint> usagePoints, String readingType) {
        this.outputFrequency = outputFrequency;
        this.timeAcceleration = timeAcceleration;
        this.destinationFilePath = destinationFilePath;
        this.connexoUrl = connexoUrl;
        this.simulatorPort = simulatorPort;
        this.usagePoints = usagePoints;
        this.readingType = readingType;
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

    public Optional<String> getConnexoUrl() {
        if (connexoUrl==null || connexoUrl.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(connexoUrl);
    }

    public String getReadingType() {
        return readingType;
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
                ",\n\treading type='" + readingType + '\'' +
                ",\n\tusagePoints=" + usagePoints +
                '}';
    }
}
