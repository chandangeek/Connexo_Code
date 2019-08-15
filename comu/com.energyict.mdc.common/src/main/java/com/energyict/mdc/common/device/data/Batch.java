/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface Batch extends HasId {

    String getName();

    boolean addDevice(Device device);

    void removeDevice(Device device);

    boolean isMember(Device device);
}
