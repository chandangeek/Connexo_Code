/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BogusInfo {
    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("loadProfileCount")
    public int loadProfileCount;
    @JsonProperty("registerCount")
    public int registerCount;
    @JsonProperty("logBookCount")
    public int logBookCount;
    @JsonProperty("deviceConfigurationCount")
    public int deviceConfigurationCount;
    @JsonProperty("canBeDirectlyAddressed")
    public boolean canBeDirectlyAddressed;
    @JsonProperty("canBeGateway")
    public boolean canBeGateway;

    public BogusInfo() {
    }
}
