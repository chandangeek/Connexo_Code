package com.elster.jupiter.yellowfin.groups;

import com.elster.jupiter.yellowfin.groups.impl.AdHocDeviceGroupImpl;
import com.elster.jupiter.yellowfin.groups.impl.DynamicDeviceGroupImpl;
import com.energyict.mdc.device.data.Device;

import java.util.List;
import java.util.Optional;

public interface YellowfinGroupsService {
    String COMPONENTNAME = "YFG";

    Optional<DynamicDeviceGroupImpl> cacheDynamicDeviceGroup(String groupName);
    Optional<AdHocDeviceGroupImpl> cacheAdHocDeviceGroup(List<Device> devices);
}
