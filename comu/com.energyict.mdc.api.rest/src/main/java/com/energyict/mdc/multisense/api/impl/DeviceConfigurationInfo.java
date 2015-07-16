package com.energyict.mdc.multisense.api.impl;

import java.util.List;

/**
 * Created by bvn on 5/12/15.
 */
public class DeviceConfigurationInfo extends LinkInfo {
    public String name;
    public String description;
    public LinkInfo deviceType;
    public List<LinkInfo> connectionMethods;

}
