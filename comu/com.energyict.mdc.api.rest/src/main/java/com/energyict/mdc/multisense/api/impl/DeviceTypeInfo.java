package com.energyict.mdc.multisense.api;

import java.util.List;

/**
 * Created by bvn on 5/12/15.
 */
public class DeviceTypeInfo extends LinkInfo {
    public Long id;
    public String name;
    public String description;
    public List<DeviceConfigurationInfo> deviceConfigurations;
}
