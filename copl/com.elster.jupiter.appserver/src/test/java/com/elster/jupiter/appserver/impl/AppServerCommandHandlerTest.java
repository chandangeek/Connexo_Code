package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.ImportFolderForAppServer;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.framework.BundleContext;

import javax.inject.Provider;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 7/1/16.
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class AppServerCommandHandlerTest {

    private AppServerImpl appServer;
    private AppServiceImpl appService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;
    @Mock
    private CronExpressionParser cronExpressionParser;
    @Mock
    private FileImportService fileImportService;
    @Mock
    private EndPointConfigurationService endPointConfigurationService;
    @Mock
    private MessageService messageService;
    @Mock
    private JsonService jsonService;
    @Mock
    private WebServicesService webServicesService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ThreadPrincipalService threadPrincipleService;
    @Mock
    private Provider<EndPointForAppServerImpl> webServiceForAppServiceProvider;
    @Mock
    private EventService eventService;
    @Mock
    private TaskService taskService;
    @Mock
    private OrmService ormService;
    @Mock
    private NlsService nlsService;
    @Mock
    private UserService userService;
    @Mock
    private QueryService queryService;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private UpgradeService upgradeService;

    @Before
    public void setUp() throws Exception {
        when(bundleContext.getProperty("com.elster.jupiter.server.name")).thenReturn("bvn");
        DataMapper<ImportFolderForAppServer> importFolderMapper = mock(DataMapper.class, Answers.RETURNS_DEEP_STUBS.get());
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        DataMapper<AppServer> allServerMapper = mock(DataMapper.class, Answers.RETURNS_DEEP_STUBS.get());
        when(dataModel.mapper(AppServer.class)).thenReturn(allServerMapper);
        when(dataModel.mapper(ImportFolderForAppServer.class)).thenReturn(importFolderMapper);
        when(importFolderMapper.getOptional(anyString())).thenReturn(Optional.empty());

        appServer = new AppServerImpl(dataModel, cronExpressionParser, fileImportService, messageService, jsonService,
                thesaurus, transactionService, threadPrincipleService, webServiceForAppServiceProvider, webServicesService,
                eventService, endPointConfigurationService);
        doReturn(Optional.of(appServer)).when(allServerMapper).getOptional("bvn");
        appService = new AppServiceImpl(ormService, nlsService, transactionService, messageService, cronExpressionParser, jsonService,
                fileImportService, taskService, userService, queryService, bundleContext, threadPrincipleService, webServicesService,
                upgradeService, endPointConfigurationService, eventService);

        appServer.activate();

    }

    @Test
    public void testSupportEndPoint() throws Exception {
        appService.reconfigureEndPoint("Metering");


    }
}
