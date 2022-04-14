/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.Pair;

import com.energyict.mdc.common.device.data.Device;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;
import java.util.Optional;

@ConsumerType
public interface SAPMeterReadingDocumentReason {
    long SECONDS_IN_DAY = 86400; //24 hours

    /**
     * Reading reason code
     */
    List<String> getReasonCodeCodes();

    /**
     * Data source code
     */
    List<String> getDataSourceTypeCodeCodes();

    /**
     * Collection interval support
     */
    boolean hasCollectionInterval();

    /**
     * Date shift in seconds
     */
    long getShiftDate();

    /**
     * Pair of Macro and Measuring codes
     *
     * @return macro and measuring codes
     */
    Optional<Pair<String, String>> getExtraDataSourceMacroAndMeasuringCodes();

    /**
     * Using current dateTime support
     */
    boolean shouldUseCurrentDateTime();

    /**
     * Invoked by the service call when processing data with reading reason code
     */
    void process(SAPMeterReadingDocumentCollectionData sapMeterReadingDocumentCollectionData);

    boolean validateComTaskExecutionIfNeeded(Device device, boolean isRegular, ReadingType readingType);
}