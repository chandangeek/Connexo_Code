/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.events;

import com.elster.jupiter.cbo.Status;

public interface EndDeviceEventRecordBuilder {

    EndDeviceEventRecordBuilder setAliasName(String aliasName);

    EndDeviceEventRecordBuilder setDescription(String description);

    EndDeviceEventRecordBuilder setIssuerID(String issuerID);

    EndDeviceEventRecordBuilder setIssuerTrackingID(String issuerTrackingID);

    EndDeviceEventRecordBuilder setLogBookId(long logBookId);

    EndDeviceEventRecordBuilder setLogBookPosition(int logBookPosition);

    EndDeviceEventRecordBuilder setmRID(String mRID);

    EndDeviceEventRecordBuilder setName(String name);

    EndDeviceEventRecordBuilder setProcessingFlags(long processingFlags);

    EndDeviceEventRecordBuilder setReason(String reason);

    EndDeviceEventRecordBuilder setSeverity(String severity);

    EndDeviceEventRecordBuilder setStatus(Status status);

    EndDeviceEventRecordBuilder addProperty(String key, String value);

    EndDeviceEventRecord create();

}
