package com.elster.jupiter.yellowfin.groups;

import com.energyict.mdc.device.data.Device;

import java.util.List;
import java.util.Optional;

public interface YellowfinGroupsService {
    String COMPONENTNAME = "YFG";

    Optional<CachedDeviceGroup> cacheDynamicDeviceGroup(String groupName);
    Optional<CachedDeviceGroup> cacheAdHocDeviceGroup(List<Device> devices);
}
