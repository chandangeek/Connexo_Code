/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DeviceMessageEnablementInfo {

    public List<Long> messageIds = new ArrayList<>();
    public List<DeviceMessagePrivilegeInfo> privileges = new ArrayList<>();
    public DeviceConfigurationInfo deviceConfiguration = new DeviceConfigurationInfo();
}
