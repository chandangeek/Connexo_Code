/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityAccessorInfo {
    public Long id;
    public String name;
    public String type;
    public String description;
    public String truststore;
    public String certificate;
    public String expirationTime;
}
