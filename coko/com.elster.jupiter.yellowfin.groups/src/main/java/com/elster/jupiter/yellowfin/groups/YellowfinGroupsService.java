package com.elster.jupiter.yellowfin.groups;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.yellowfin.groups.impl.DynamicDeviceGroupImpl;

import java.util.List;
import java.util.Optional;

public interface YellowfinGroupsService {
    String COMPONENTNAME = "YFG";
    String ADHOC_SEARCH_LIFE_CYCLE_QUEUE_DEST = "AdHocSearch";
    String ADHOC_SEARCH_LIFE_CYCLE_QUEUE_TASK = "AdHoc Search";
    String ADHOC_SEARCH_LIFE_CYCLE_QUEUE_DEST_DISPLAYNAME = "Connexo Facts search";

    Thesaurus thesaurus();

    Optional<DynamicDeviceGroupImpl> cacheDynamicDeviceGroup(String groupName);
    Optional<AdHocDeviceGroup> cacheAdHocDeviceGroup(List<Long> devices);
    void purgeAdHocSearch();
}
