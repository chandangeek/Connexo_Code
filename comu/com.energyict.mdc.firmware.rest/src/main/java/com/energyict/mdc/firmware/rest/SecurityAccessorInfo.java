/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest;

import aQute.bnd.annotation.ProviderType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityAccessorInfo {
    public Long id;
    public String name;
    public String description;
    public String truststore;
    public Instant expirationTime;
}
