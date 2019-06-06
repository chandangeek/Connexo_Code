package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.rest.impl.MessagingApplication;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import org.mockito.Mock;

import javax.ws.rs.core.Application;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class MessagingApplicationTest extends FelixRestApplicationJerseyTest {

    @Mock
    private RestQueryService restQueryService;
    @Mock
    protected MessageService messageService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private CronExpressionParser cronExpressionParser;
    @Mock
    private AppService appService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private TaskService taskService;
    @Mock
    private ServiceCallService serviceCallService;

    @Override
    protected Application getApplication() {
        when(thesaurus.join(any(Thesaurus.class))).thenReturn(thesaurus);

        MessagingApplication application = new MessagingApplication();
        application.setRestQueryService(restQueryService);
        application.setMessageService(messageService);
        application.setTransactionService(transactionService);
        application.setCronExpressionParser(cronExpressionParser);
        application.setAppService(appService);
        application.setNlsService(nlsService);
        application.setTaskService(taskService);
        application.setServiceCallService(serviceCallService);

        return application;
    }

}

