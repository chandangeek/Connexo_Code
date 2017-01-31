/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.Transaction;

import javax.ws.rs.core.Application;

import org.mockito.Matchers;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

public class EventApplicationTest extends FelixRestApplicationJerseyTest {

    @Mock
    EventService eventService;
    @Mock
    RestQueryService restQueryService;

    @Override
    protected Application getApplication() {
        EventApplication application = new EventApplication();
        application.setRestQueryService(restQueryService);
        application.setTransactionService(transactionService);
        application.setEventService(eventService);
        application.setNlsService(nlsService);
        return application;
    }

    @Override
    public void setupMocks() {
        super.setupMocks();
        when(transactionService.execute(Matchers.any(Transaction.class)))
                .thenAnswer(invocation -> ((Transaction) invocation.getArguments()[0]).perform());
    }

}