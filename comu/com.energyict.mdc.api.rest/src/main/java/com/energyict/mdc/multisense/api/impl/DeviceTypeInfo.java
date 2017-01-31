/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.util.List;

/**
 * Created by bvn on 5/12/15.
 */
public class DeviceTypeInfo extends LinkInfo<Long> {
    public String name;
    public String description;
    public List<LinkInfo> deviceConfigurations;
    public List<LinkInfo> deviceMessageFiles;
}
