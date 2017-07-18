/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.tou;

import com.elster.jupiter.demo.impl.commands.CommandWithTransaction;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Creates demo data that supports the time of use aspects of the Belgian energy market.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-17 (11:41)
 */
public class CreateBelgianMarketTimeOfUseDataCommand extends CommandWithTransaction {

    private final Provider<CreateEventSetCommand> eventSetCommandProvider;
    private final Provider<CreateCalendarCommand> calendarCommandProvider;

    @Inject
    public CreateBelgianMarketTimeOfUseDataCommand(Provider<CreateEventSetCommand> eventSetCommandProvider, Provider<CreateCalendarCommand> calendarCommandProvider) {
        this.eventSetCommandProvider = eventSetCommandProvider;
        this.calendarCommandProvider = calendarCommandProvider;
    }

    @Override
    public void run() {
        this.calendarCommandProvider.get().findOrCreateCalendar(this.eventSetCommandProvider.get().createEventSet());
    }

}