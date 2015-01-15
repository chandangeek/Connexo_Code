package com.elster.jupiter.yellowfin.groups;

import com.elster.jupiter.yellowfin.groups.impl.DynamicDeviceGroupImpl;

import java.util.List;
import java.util.Optional;

public interface YellowfinGroupsService {
    String COMPONENTNAME = "YFG";

    Optional<DynamicDeviceGroupImpl> cacheDynamicDeviceGroup(String groupName);
    Optional<AdHocDeviceGroup> cacheAdHocDeviceGroup(List<Long> devices);
}
