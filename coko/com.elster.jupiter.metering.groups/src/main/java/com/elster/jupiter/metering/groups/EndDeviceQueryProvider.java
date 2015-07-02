package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.util.conditions.Condition;

import java.time.Instant;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: igh
 * Date: 22/08/14
 * Time: 9:19
 * To change this template use File | Settings | File Templates.
 */
public interface EndDeviceQueryProvider {

    String getName();
    List<EndDevice> findEndDevices(Condition conditions);
    List<EndDevice> findEndDevices(Instant instant, Condition conditions);
    List<EndDevice> findEndDevices(Instant instant, Condition conditions, int start, int limit);
    Condition getQueryCondition(Condition conditions);

}
