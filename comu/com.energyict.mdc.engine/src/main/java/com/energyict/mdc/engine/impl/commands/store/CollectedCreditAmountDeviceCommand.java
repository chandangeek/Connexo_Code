/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedCreditAmountEvent;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provides functionality to update the CreditAmount of a Device
 */
public class CollectedCreditAmountDeviceCommand extends DeviceCommandImpl<CollectedCreditAmountEvent> {

    public static final String DESCRIPTION_TITLE = "Collected credit amount";

    private final CollectedCreditAmount collectedCreditAmount;
    private final ComTaskExecution comTaskExecution;

    public CollectedCreditAmountDeviceCommand(ServiceProvider serviceProvider, CollectedCreditAmount collectedCreditAmount, ComTaskExecution comTaskExecution) {
        super(comTaskExecution, serviceProvider);
        this.collectedCreditAmount = collectedCreditAmount;
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        comServerDAO.updateCreditAmount(collectedCreditAmount);
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        addBuilderProperty(builder, "credit type");
        addBuilderProperty(builder, "credit amount");
    }

    private Consumer<Integer> addBuilderProperty(DescriptionBuilder builder, String propertyName) {
        return property -> builder.addProperty(propertyName).append(property.toString().toLowerCase());
    }

    protected Optional<CollectedCreditAmountEvent> newEvent(List<Issue> issues) {
        CollectedCreditAmountEvent event = new CollectedCreditAmountEvent(new ComServerEventServiceProvider(), collectedCreditAmount);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }
}