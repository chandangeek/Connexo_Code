/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.metering.UsagePoint;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UsagePointDataModelService {

    String COMPONENT_NAME = "UDC";

    String BULK_ITEMIZER_QUEUE_DESTINATION = "ItemizeBulkUsagePoint";
    String BULK_HANDLING_QUEUE_DESTINATION = "HandleBulkUsagePoint";
    String BULK_ITEMIZER_QUEUE_SUBSCRIBER = "usagepoint.bulk.itemizer";
    String BULK_HANDLING_QUEUE_SUBSCRIBER = "usagepoint.bulk.handler";

    UsagePointValidation forValidation(UsagePoint usagePoint);

    UsagePointEstimation forEstimation(UsagePoint usagePoint);

}
