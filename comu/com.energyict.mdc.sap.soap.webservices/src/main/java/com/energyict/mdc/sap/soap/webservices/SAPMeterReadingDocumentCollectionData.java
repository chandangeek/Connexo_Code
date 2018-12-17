/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.servicecall.ServiceCall;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface SAPMeterReadingDocumentCollectionData {

    Integer getReadindCollectionInterval();

    Integer getReadingDateWindow();

    Instant getScheduledReadingDate();

    Optional<Channel> getMeterChannel();

    Optional<ReadingType> getMeterReadingType();

    String getDeviceName();

    boolean isPastCase();

    boolean isRegular();

    void calculate();

    ServiceCall getServiceCall();
}