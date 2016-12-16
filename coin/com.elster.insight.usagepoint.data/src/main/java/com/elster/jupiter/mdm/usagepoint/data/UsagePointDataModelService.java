package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import aQute.bnd.annotation.ProviderType;

import java.time.Clock;

@ProviderType
public interface UsagePointDataModelService {
    String COMPONENT_NAME = "UDC";
    String BULK_ITEMIZER_QUEUE_DESTINATION = "ItemizeBulkUsagePoint";
    String BULK_HANDLING_QUEUE_DESTINATION = "HandleBulkUsagePoint";
    String BULK_ITEMIZER_QUEUE_SUBSCRIBER = "usagepoint.bulk.itemizer";
    String BULK_HANDLING_QUEUE_SUBSCRIBER = "usagepoint.bulk.handler";

    Clock clock();

    DataModel dataModel();

    Thesaurus thesaurus();
}
