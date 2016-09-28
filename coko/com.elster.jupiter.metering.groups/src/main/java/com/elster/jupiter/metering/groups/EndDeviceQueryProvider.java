package com.elster.jupiter.metering.groups;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.search.SearchablePropertyCondition;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.List;

/**
 * Responsible for converting actual content of the group into a list of EndDevices.
 */
@ConsumerType
public interface EndDeviceQueryProvider {

    String getName();

    List<EndDevice> findEndDevices(Instant instant, List<SearchablePropertyCondition> conditions);

    List<EndDevice> findEndDevices(Instant instant, List<SearchablePropertyCondition> conditions, int start, int limit);

    Query<EndDevice> getEndDeviceQuery(List<SearchablePropertyCondition> conditions);

}
