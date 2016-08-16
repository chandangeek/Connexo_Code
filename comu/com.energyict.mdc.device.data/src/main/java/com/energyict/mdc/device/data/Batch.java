package com.energyict.mdc.device.data;

import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface Batch extends HasId {

    String getName();

    boolean addDevice(Device device);

    void removeDevice(Device device);

    boolean isMember(Device device);
}
