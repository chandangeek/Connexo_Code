package com.elster.jupiter.mdm.eventpropagator.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ProcessDefinitionInfo {
    public String name;
    public String deploymentId;
}