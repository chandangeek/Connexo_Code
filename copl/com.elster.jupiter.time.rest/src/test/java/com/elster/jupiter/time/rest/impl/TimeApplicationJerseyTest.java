/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.json.JsonService;
import org.mockito.Matchers;
import org.mockito.Mock;

import javax.ws.rs.core.Application;

import static org.mockito.Mockito.when;

public class TimeApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    TimeService timeService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    JsonService jsonService;

    @Override
    protected Application getApplication() {
        TimeApplication application = new TimeApplication();
        application.setTimeService(timeService);
        application.setRestQueryService(restQueryService);
        application.setTransactionService(transactionService);
        application.setNlsService(nlsService);
        application.setJsonService(jsonService);
        return application;
    }

    @Override
    public void setupMocks() {
        super.setupMocks();
        when(transactionService.execute(Matchers.any(Transaction.class)))
                .thenAnswer(invocation -> ((Transaction) invocation.getArguments()[0]).perform());
    }
}
