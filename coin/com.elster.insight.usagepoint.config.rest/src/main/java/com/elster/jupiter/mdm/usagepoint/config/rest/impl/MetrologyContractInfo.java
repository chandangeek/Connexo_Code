package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetrologyContractInfo {
    public long id;
    public String name;
    public boolean mandatory;
    public List<ReadingTypeDeliverablesInfo> readingTypeDeliverables;

    public MetrologyContractInfo() {
    }
}


