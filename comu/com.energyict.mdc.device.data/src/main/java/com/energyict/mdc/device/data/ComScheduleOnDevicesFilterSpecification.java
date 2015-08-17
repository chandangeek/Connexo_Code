package com.energyict.mdc.device.data;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple structure to hold all IDs so we can store the filter in the DB
 */
public class ComScheduleOnDevicesFilterSpecification {

    public String mRID;
    public String serialNumber;
    public List<Long> deviceTypes;
    public List<Long> deviceConfigurations;
}
