package com.elster.jupiter.metering.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetrologyConfigurationVersionInfo {

    public long id;
    public Instant startTime;
    public Instant endTime;
    public long version;
}