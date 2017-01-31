/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;


import java.util.Collections;
import java.util.List;

public class ProcessDefinitionInfo {

    public String name;
    public String processId;
    public String version;
    public String active;
    public String type;
    public String displayType;
    public String deploymentId;
    public String appKey;
    public String versionDB;
    public List<?> privileges;
    public List<?> properties = Collections.emptyList();

    public ProcessDefinitionInfo(String name, String processId, String version, String active, String type, String displayType, String deploymentId, List<?> privileges, List<?> properties){
        this.name = name;
        this.processId = processId;
        this.version = version;
        this.active = active;
        this.type = type;
        this.displayType = displayType;
        this.deploymentId = deploymentId;
        this.privileges = privileges;
        this.properties = properties;
    }

    public ProcessDefinitionInfo(){

    }
}
