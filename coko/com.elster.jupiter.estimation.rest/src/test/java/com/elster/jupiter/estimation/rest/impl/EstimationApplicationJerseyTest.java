package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.rest.PropertyUtils;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.ws.rs.core.Application;

import static org.mockito.Mockito.when;

public class EstimationApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    EstimationService estimationService;
    @Mock
    MeteringService meteringService;
    @Mock
    TimeService timeService;
    @Mock
    MeteringGroupsService meteringGroupsService;
    @Mock
    RestQueryService restQueryService;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        EstimationApplication estimationApplication = new EstimationApplication();
        estimationApplication.setEstimationService(estimationService);
        estimationApplication.setRestQueryService(restQueryService);
        estimationApplication.setTransactionService(transactionService);
        estimationApplication.setNlsService(nlsService);
        estimationApplication.setTimeService(timeService);
        estimationApplication.setMeteringGroupsService(meteringGroupsService);
        return estimationApplication;
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
