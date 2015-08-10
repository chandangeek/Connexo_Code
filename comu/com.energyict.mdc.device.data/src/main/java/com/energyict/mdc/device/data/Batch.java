package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface Batch {

    long getId();

    String getName();

    boolean addDevice(Device device);

    void removeDevice(Device device);

    boolean isMember(Device device);

    void delete();
}
