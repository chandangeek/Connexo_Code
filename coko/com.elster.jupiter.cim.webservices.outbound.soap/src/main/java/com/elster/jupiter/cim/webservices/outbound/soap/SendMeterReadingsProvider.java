/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap;

import com.elster.jupiter.metering.ReadingStorer;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SendMeterReadingsProvider {

    String NAME = "CIM SendMeterReadings";

    void call(ReadingStorer readingStorer, boolean isCreated);
}
