/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class VersionInfo<T> {
    @JsonProperty("id")
    public T id;
    @JsonProperty("version")
    public Long version;  // allow null values

    public VersionInfo() {
    }

    public VersionInfo(T id, Long version) {
        this.id = id;
        this.version = version;
    }
}
