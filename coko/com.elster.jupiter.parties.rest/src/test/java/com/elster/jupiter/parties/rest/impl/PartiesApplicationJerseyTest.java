/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.UserInfoFactory;

import javax.ws.rs.core.Application;
import java.time.Clock;

import org.mockito.Matchers;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

public class PartiesApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    RestQueryService restQueryService;
    @Mock
    PartyService partyService;
    @Mock
    UserService userService;
    @Mock
    Clock clock;
    @Mock
    UserInfoFactory userInfoFactory;

    @Override
    protected Application getApplication() {
        PartiesApplication application = new PartiesApplication();
        application.setTransactionService(transactionService);
        application.setRestQueryService(restQueryService);
        application.setPartyService(partyService);
        application.setUserService(userService);
        application.setClock(clock);
        application.setNlsService(nlsService);
        application.setUserInfoFactory(userInfoFactory);
        return application;
    }

    @Override
    public void setupMocks() {
        super.setupMocks();
        when(transactionService.execute(Matchers.any())).thenAnswer(invocation -> {
            if (invocation.getArguments()[0] instanceof VoidTransaction) {
               return ((VoidTransaction) invocation.getArguments()[0]).get();
            } else {
                return ((Transaction<?>) invocation.getArguments()[0]).perform();
            }
        });
    }

}