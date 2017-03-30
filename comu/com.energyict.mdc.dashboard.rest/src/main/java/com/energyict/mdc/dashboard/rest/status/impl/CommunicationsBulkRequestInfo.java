/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecificationMessage;

import java.util.List;

/**
 * Created by bvn on 3/27/15.
 */
public class CommunicationsBulkRequestInfo {
    public List<Long> communications;
    public ComTaskExecutionFilterSpecificationMessage filter;
}
