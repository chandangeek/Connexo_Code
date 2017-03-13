/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;


import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import javax.ws.rs.core.Link;

public class DeviceAlarmStatusInfo extends LinkInfo<Long> {
    public String id;
    public String name;
    public Boolean clearedStatus;
}
