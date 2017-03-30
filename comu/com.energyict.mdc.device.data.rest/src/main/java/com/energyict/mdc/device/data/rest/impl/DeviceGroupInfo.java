/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceGroupInfo {

    public long id;
    public String mRID;
    public String name;
    public long version;
    public boolean dynamic;
    public String filter;
    public List<Long> devices;
}
