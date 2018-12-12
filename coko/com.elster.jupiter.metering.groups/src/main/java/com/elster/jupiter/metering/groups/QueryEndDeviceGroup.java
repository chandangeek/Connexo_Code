/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.EndDevice;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface QueryEndDeviceGroup extends EndDeviceGroup, QueryGroup<EndDevice> {
    String TYPE_IDENTIFIER = "QEG";
}
