package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.util.HasId;

@ProviderType
public interface Batch extends HasId {

    String getName();

    boolean addDevice(Device device);

    void removeDevice(Device device);

    boolean isMember(Device device);

    void delete();
}
