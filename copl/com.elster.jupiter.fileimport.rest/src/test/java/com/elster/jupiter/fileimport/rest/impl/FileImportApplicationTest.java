package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.rest.impl.FileImportApplication;
import com.elster.jupiter.fileimport.rest.impl.PropertyUtils;
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

public class FileImportApplicationTest extends FelixRestApplicationJerseyTest {

    @Mock
    protected FileImportService fileImportService;
    @Mock
    protected RestQueryService restQueryService;
    @Mock
    protected CronExpressionParser cronExpressionParser;
    @Mock
    static SecurityContext securityContext;
    @Mock
    static FileSystem fileSystem;
    @Mock
    PropertyUtils propertyUtils;

    @Mock
    public AppService appService;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return new MessageSeed[0];
    }

    @Override
    protected Application getApplication() {
        when(thesaurus.join(any(Thesaurus.class))).thenReturn(thesaurus);

        FileImportApplication application = new FileImportApplication();
        application.setFileImportService(fileImportService);
        application.setRestQueryService(restQueryService);
        application.setTransactionService(transactionService);
        application.setCronExpressionParser(cronExpressionParser);
        application.setNlsService(nlsService);
        application.setFileSystem(fileSystem);
        application.setAppService(appService);

        return application;
    }
}
