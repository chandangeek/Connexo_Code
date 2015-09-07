package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.exception.MessageSeed;
import org.mockito.Mock;

import javax.ws.rs.core.Application;
import java.nio.file.FileSystem;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileImportApplicationTest extends FelixRestApplicationJerseyTest {

    @Mock
    protected FileImportService fileImportService;
    @Mock
    protected RestQueryService restQueryService;
    @Mock
    protected CronExpressionParser cronExpressionParser;
    @Mock
    protected AppService appService;
    @Mock
    static FileSystem fileSystem;

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
        application.addApplication(mockApp("SYS", "Admin"));
        application.addApplication(mockApp("MDC", "MultiSense"));
        return application;
    }

    private App mockApp(String key, String name) {
        App app = mock(App.class);
        when(app.getKey()).thenReturn(key);
        when(app.getName()).thenReturn(name);
        return app;
    }
}
