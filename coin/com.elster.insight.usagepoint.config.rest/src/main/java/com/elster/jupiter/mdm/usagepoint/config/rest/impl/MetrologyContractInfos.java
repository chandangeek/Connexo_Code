package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetrologyContractInfos {
    public long total;
    public List<MetrologyContractInfo> contracts;
}
