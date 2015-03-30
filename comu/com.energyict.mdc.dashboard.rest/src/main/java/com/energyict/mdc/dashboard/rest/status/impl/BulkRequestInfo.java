package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecificationMessage;
import java.util.List;

/**
 * Created by bvn on 3/27/15.
 */
public class BulkRequestInfo {
    public List<Long> connections;
    public ConnectionTaskFilterSpecificationMessage filter;
}
