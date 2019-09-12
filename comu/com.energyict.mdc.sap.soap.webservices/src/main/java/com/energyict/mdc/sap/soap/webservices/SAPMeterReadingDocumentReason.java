/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface SAPMeterReadingDocumentReason {

    /**
     * Reading reason code
     */
    List<String> getCodes();

    /**
     * Collection interval support
     */
    boolean hasCollectionInterval();

    /**
     * Collection interval support
     */
    long gedAdditionalTime();

    /**
     * Bulk request support
     */
    boolean isBulk();

    /**
     * Single request support
     */
    boolean isSingle();

    /**
     * Invoked by the service call when processing data with reading reason code
     */
    void process(SAPMeterReadingDocumentCollectionData sapMeterReadingDocumentCollectionData);
}