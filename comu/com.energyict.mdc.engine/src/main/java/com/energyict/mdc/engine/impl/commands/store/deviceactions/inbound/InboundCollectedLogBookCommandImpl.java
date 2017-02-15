/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.LogBooksCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.tasks.LogBooksTask;

import java.util.ArrayList;
import java.util.List;

public class InboundCollectedLogBookCommandImpl extends LogBooksCommandImpl {

    private final List<ServerCollectedData> collectedData;

    public InboundCollectedLogBookCommandImpl(GroupedDeviceCommand groupedDeviceCommand, LogBooksTask logBooksTask, ComTaskExecution comTaskExecution, List<ServerCollectedData> collectedData) {
        super(groupedDeviceCommand, logBooksTask, comTaskExecution);
        this.collectedData = collectedData;
    }

    @Override
    public String getDescriptionTitle() {
        return "Collect inbound logbook data";
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        for (ServerCollectedData dataItem : collectedData) {
            if (dataItem instanceof CollectedLogBook) {
                this.addCollectedDataItem(dataItem);
            }
        }
    }

    @Override
    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.getListOfCollectedLogBooks().isEmpty()) {
            builder.addLabel("No log book data collected");
        } else {
            if (isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
                PropertyDescriptionBuilder logbookObisCodesBuilder = builder.addListProperty("logbooks");
                for (CollectedLogBook logBook : getListOfCollectedLogBooks()) {
                    logbookObisCodesBuilder.append("(");
                    logbookObisCodesBuilder.append(logBook.getLogBookIdentifier().getLogBookObisCode());
                    logbookObisCodesBuilder.append(" - ");
                    logbookObisCodesBuilder.append("nrOfEvents: ").append(logBook.getCollectedMeterEvents().size());
                    logbookObisCodesBuilder.append(")");
                    logbookObisCodesBuilder.next();
}
            } else {
                builder.addProperty("nrOfLogbooksCollected").append(getListOfCollectedLogBooks().size());
            }
        }
    }

    private List<CollectedLogBook> getListOfCollectedLogBooks() {
        List<CollectedLogBook> collectedLogBooks = new ArrayList<>();
        for (CollectedData data : getCollectedData()) {
            if (data instanceof CollectedLogBook) {
                collectedLogBooks.add((CollectedLogBook) data);
            }
        }
        return collectedLogBooks;
    }
}
