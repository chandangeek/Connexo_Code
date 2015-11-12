package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.sql.SqlFragment;

import java.time.Instant;
import java.util.List;

/**
 * Responsible for converting actual content of the group into a list of EndDevices
 */
public interface EndDeviceQueryProvider {

    String getName();

    List<EndDevice> findEndDevices(Instant instant, List<SearchablePropertyCondition> conditions);

    List<EndDevice> findEndDevices(Instant instant, List<SearchablePropertyCondition> conditions, int start, int limit);

    SqlFragment toFragment(SqlFragment sqlFragment, String columnName);

}
