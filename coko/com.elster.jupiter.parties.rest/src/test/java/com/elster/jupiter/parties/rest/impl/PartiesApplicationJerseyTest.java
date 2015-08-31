package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.core.Application;
import java.time.Clock;

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

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return new MessageSeed[0];
    }

    @Override
    protected Application getApplication() {
        PartiesApplication application = new PartiesApplication();
        application.setTransactionService(transactionService);
        application.setRestQueryService(restQueryService);
        application.setPartyService(partyService);
        application.setUserService(userService);
        application.setClock(clock);
        return application;
    }

    @Override
    public void setupMocks() {
        super.setupMocks();
        when(transactionService.execute(Matchers.any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ((Transaction<?>) invocation.getArguments()[0]).perform();
            }
        });
    }
}
