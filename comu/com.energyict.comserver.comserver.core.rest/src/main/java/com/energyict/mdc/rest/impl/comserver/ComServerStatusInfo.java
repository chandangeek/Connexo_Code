package com.energyict.mdc.rest.impl.comserver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ComServerStatusInfo {
    public Boolean active;

    public ComServerStatusInfo() {
    }
}
