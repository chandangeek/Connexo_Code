package com.elster.jupiter.mdm.eventpropagator.impl;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ProcessDefinitionInfos {
    public List<ProcessDefinitionInfo>  processDefinitionList;
}
