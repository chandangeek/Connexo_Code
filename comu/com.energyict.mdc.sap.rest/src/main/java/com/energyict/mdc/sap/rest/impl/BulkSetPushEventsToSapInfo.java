/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkSetPushEventsToSapInfo {
    public String action;
    public String filter;
    public List<Long> deviceIds;
    public boolean pushEventsToSap;
}