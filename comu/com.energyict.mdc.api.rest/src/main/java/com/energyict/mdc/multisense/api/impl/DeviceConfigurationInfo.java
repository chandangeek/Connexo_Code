package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.util.List;

/**
 * Created by bvn on 5/12/15.
 */
public class DeviceConfigurationInfo extends LinkInfo<Long> {
    public String name;
    public String description;
    public LinkInfo deviceType;
    public List<LinkInfo> connectionMethods;
    public List<LinkInfo> securityPropertySets;
    public List<LinkInfo> comTaskEnablements;
    public List<LinkInfo> deviceMessageEnablements;
}
