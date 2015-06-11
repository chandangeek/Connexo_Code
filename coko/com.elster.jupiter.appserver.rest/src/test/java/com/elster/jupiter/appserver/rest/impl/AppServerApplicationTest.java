package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.exception.MessageSeed;
import org.mockito.Mock;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;

import java.nio.file.FileSystem;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class AppServerApplicationTest extends FelixRestApplicationJerseyTest {

    @Mock
    protected AppService appService;
    @Mock
    protected MessageService messageService;
    @Mock
    protected FileImportService fileImportService;
    @Mock
    protected RestQueryService restQueryService;
    @Mock
    protected CronExpressionParser cronExpressionParser;
    @Mock
    static FileSystem fileSystem;
    @Mock
    static SecurityContext securityContext;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return new MessageSeed[0];
    }

    @Override
    protected Application getApplication() {
        when(thesaurus.join(any(Thesaurus.class))).thenReturn(thesaurus);

        AppServerApplication application = new AppServerApplication();
        application.setAppService(appService);
        application.setFileImportService(fileImportService);
        application.setMessageService(messageService);
        application.setRestQueryService(restQueryService);
        application.setTransactionService(transactionService);
        application.setCronExpressionParser(cronExpressionParser);
        application.setNlsService(nlsService);
        application.setFileSystem(fileSystem);
        return application;
    }
}
