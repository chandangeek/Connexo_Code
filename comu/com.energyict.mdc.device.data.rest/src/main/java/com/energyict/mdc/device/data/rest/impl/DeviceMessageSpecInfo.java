package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;

import java.util.List;

/**
 * Created by bvn on 10/24/14.
 */
public class DeviceMessageSpecInfo {
    public String id;
    public String name;
    public Boolean willBePickedUpByPlannedComTask;
    public Boolean willBePickedUpByComTask;
    public List<PropertyInfo> properties;
}
