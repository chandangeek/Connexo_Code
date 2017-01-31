/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLogBookSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Adapter between a {@link SmartMeterProtocolAdapterImpl} and {@link DeviceLogBookSupport}
 *
 * @author gna
 * @since 10/04/12 - 15:29
 */
public class SmartMeterProtocolLogBookAdapter implements DeviceLogBookSupport {

    /**
     * The used {@link SmartMeterProtocol}
     */
    private final SmartMeterProtocol smartMeterProtocol;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;

    public SmartMeterProtocolLogBookAdapter(SmartMeterProtocol smartMeterProtocol, IssueService issueService, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        this.smartMeterProtocol = smartMeterProtocol;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(final List<LogBookReader> logBookReaders) {
        CollectedDataFactory collectedDataFactory = this.collectedDataFactory;
        List<CollectedLogBook> collectedLogBooks = new ArrayList<>();
        if (logBookReaders != null && this.smartMeterProtocol != null) {
            for (LogBookReader reader : logBookReaders) {
                CollectedLogBook deviceLogBook = collectedDataFactory.createCollectedLogBook(reader.getLogBookIdentifier());
                try {
                    if (reader.getLogBookObisCode().equals(LogBookFactory.GENERIC_LOGBOOK_TYPE_OBISCODE)) {
                        final List<MeterEvent> meterEvents = smartMeterProtocol.getMeterEvents(Date.from(reader.getLastLogBook()));
                        deviceLogBook.setMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents, this.meteringService));
                    } else {
                        deviceLogBook.setFailureInformation(ResultType.NotSupported, getWarning(reader.getLogBookObisCode(), MessageSeeds.LOGBOOK_NOT_SUPPORTED, reader.getLogBookObisCode()));
                    }
                } catch (IOException e) {
                    deviceLogBook.setFailureInformation(ResultType.InCompatible, getProblem(reader.getLogBookObisCode(), MessageSeeds.LOGBOOK_ISSUE, reader.getLogBookObisCode(), e));
                }
                collectedLogBooks.add(deviceLogBook);
            }
        }
        return collectedLogBooks;
    }

    private Issue getProblem(Object source, MessageSeed messageSeed, Object... arguments){
        return this.issueService.newProblem(source, messageSeed, arguments);
    }

    private Issue getWarning(Object source, MessageSeed messageSeed, Object... arguments){
        return this.issueService.newWarning(source, messageSeed, arguments);
    }

}