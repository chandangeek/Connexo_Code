/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.ImportFolderForAppServer;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.framework.BundleContext;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 7/1/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class AppServerCommandHandlerTest {

    private AppServiceImpl appService;

    @Mock
    private AppServerImpl appServer;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    Table table;
    @Mock
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

    @Mock
    private WebServiceForAppServer supportedEndPoint1, supportedEndPoint2;
    private EndPointConfiguration endPointConfiguration1, endPointConfiguration2, endPointConfiguration3, deletedEndPointConfiguration;

    @Before
    public void setUp() throws Exception {
        when(endPointConfigurationService.getEndPointConfiguration(anyString())).thenReturn(Optional.empty());
        endPointConfiguration1 = mockEndPointConfiguration("epc1");
        endPointConfiguration2 = mockEndPointConfiguration("epc2");
        endPointConfiguration3 = mockEndPointConfiguration("epc3");
        deletedEndPointConfiguration = mock(EndPointConfiguration.class);
        when(deletedEndPointConfiguration.getName()).thenReturn("deleted");
        when(deletedEndPointConfiguration.isActive()).thenReturn(false);

        when(appServer.supportedEndPoints()).thenReturn(Arrays.asList(endPointConfiguration1, endPointConfiguration2));
        when(bundleContext.getProperty("com.elster.jupiter.server.name")).thenReturn("bvn");
        DataMapper dataMapper = mock(DataMapper.class, Answers.RETURNS_DEEP_STUBS.get());
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.mapper(SubscriberExecutionSpecImpl.class)).thenReturn(dataMapper);
        when(dataModel.mapper(AppServer.class)).thenReturn(dataMapper);
        when(dataModel.mapper(AppServerImpl.class)).thenReturn(dataMapper);
        when(dataModel.mapper(ImportFolderForAppServer.class)).thenReturn(dataMapper);
        when(dataModel.addTable(anyString(), any(Class.class))).thenReturn(table);
        QueryExecutor<WebServiceForAppServer> webServicesQuery = mock(QueryExecutor.class);
        when(dataModel.query(WebServiceForAppServer.class)).thenReturn(webServicesQuery);
        when(webServicesQuery.select(any(Condition.class))).thenReturn(Arrays.asList(supportedEndPoint1, supportedEndPoint2));
        when(messageService.getSubscriberSpec(anyString(), anyString())).thenReturn(Optional.empty());
        Finder<EndPointConfiguration> endPointConfigurationFinder = mockFinder(Arrays.asList(endPointConfiguration1, endPointConfiguration2, endPointConfiguration3));
        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(endPointConfigurationFinder);
        when(dataMapper.getOptional(anyString())).thenReturn(Optional.empty());
        when(webServicesService.getPublishedEndPoints()).thenReturn(Arrays.asList(endPointConfiguration1, deletedEndPointConfiguration));

        doReturn(Optional.of(appServer)).when(dataMapper).getOptional("bvn");
        appService = new AppServiceImpl(ormService, nlsService, transactionService, messageService, cronExpressionParser, jsonService,
                fileImportService, taskService, userService, queryService, bundleContext, threadPrincipleService, webServicesService,
                upgradeService, endPointConfigurationService, eventService);


    }

    private EndPointConfiguration mockEndPointConfiguration(String name) {
        EndPointConfiguration mock = mock(EndPointConfiguration.class);
        when(mock.getName()).thenReturn(name);
        when(endPointConfigurationService.getEndPointConfiguration(name)).thenReturn(Optional.of(mock));
        return mock;
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(com.elster.jupiter.domain.util.QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }


    @Test
    public void testReconfigureAddsSupportForActiveEndPoint() throws Exception {
        when(appServer.isActive()).thenReturn(true);
        when(endPointConfiguration1.isActive()).thenReturn(true);
        when(endPointConfiguration2.isActive()).thenReturn(false);
        when(endPointConfiguration3.isActive()).thenReturn(false);
        when(webServicesService.isPublished(endPointConfiguration1)).thenReturn(false);
        appService.reconfigureEndPoint("epc1");

        verify(webServicesService).publishEndPoint(endPointConfiguration1);
        ArgumentCaptor<String> msg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LogLevel> logLevelArgumentCaptor = ArgumentCaptor.forClass(LogLevel.class);

        verify(endPointConfiguration1).log(logLevelArgumentCaptor.capture(), msg.capture());
        assertThat(msg.getValue()).startsWith("Publishing");
        assertThat(logLevelArgumentCaptor.getValue()).isEqualTo(LogLevel.FINE);
        verify(webServicesService, never()).publishEndPoint(endPointConfiguration2);
        verify(webServicesService, never()).removeEndPoint(endPointConfiguration2);
        verify(webServicesService, never()).publishEndPoint(endPointConfiguration3);
        verify(webServicesService, never()).removeEndPoint(endPointConfiguration3);
        verify(webServicesService, never()).publishEndPoint(deletedEndPointConfiguration);
        verify(webServicesService, never()).removeEndPoint(deletedEndPointConfiguration);
    }

    @Test
    public void testReconfigureDropsSupportedForInactiveEndPoint() throws Exception {
        when(appServer.isActive()).thenReturn(true);
        when(endPointConfiguration1.isActive()).thenReturn(false);
        when(endPointConfiguration2.isActive()).thenReturn(false);
        when(endPointConfiguration3.isActive()).thenReturn(false);
        when(webServicesService.isPublished(endPointConfiguration1)).thenReturn(true);
        appService.reconfigureEndPoint("epc1");

        verify(webServicesService).removeEndPoint(endPointConfiguration1);
        ArgumentCaptor<String> msg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LogLevel> logLevelArgumentCaptor = ArgumentCaptor.forClass(LogLevel.class);

        verify(endPointConfiguration1).log(logLevelArgumentCaptor.capture(), msg.capture());
        assertThat(msg.getValue()).startsWith("Stopping");
        assertThat(logLevelArgumentCaptor.getValue()).isEqualTo(LogLevel.FINE);
        verify(webServicesService, never()).publishEndPoint(endPointConfiguration2);
        verify(webServicesService, never()).removeEndPoint(endPointConfiguration2);
        verify(webServicesService, never()).publishEndPoint(endPointConfiguration3);
        verify(webServicesService, never()).removeEndPoint(endPointConfiguration3);
        verify(webServicesService, never()).publishEndPoint(deletedEndPointConfiguration);
        verify(webServicesService, never()).removeEndPoint(deletedEndPointConfiguration);
    }

    @Test
    public void testReconfigureRestartsSupportedEndPoint() throws Exception {
        when(appServer.isActive()).thenReturn(true);
        when(endPointConfiguration1.isActive()).thenReturn(false);
        when(endPointConfiguration2.isActive()).thenReturn(true);
        when(endPointConfiguration3.isActive()).thenReturn(false);
        when(webServicesService.isPublished(endPointConfiguration2)).thenReturn(true);
        appService.reconfigureEndPoint("epc2");

        verify(webServicesService).removeEndPoint(endPointConfiguration2);
        verify(webServicesService).publishEndPoint(endPointConfiguration2);
        ArgumentCaptor<String> msg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LogLevel> logLevelArgumentCaptor = ArgumentCaptor.forClass(LogLevel.class);

        verify(endPointConfiguration2).log(logLevelArgumentCaptor.capture(), msg.capture());
        assertThat(msg.getValue()).startsWith("Restarting");
        assertThat(logLevelArgumentCaptor.getValue()).isEqualTo(LogLevel.FINE);
        verify(webServicesService, never()).publishEndPoint(endPointConfiguration1);
        verify(webServicesService, never()).removeEndPoint(endPointConfiguration1);
        verify(webServicesService, never()).publishEndPoint(endPointConfiguration3);
        verify(webServicesService, never()).removeEndPoint(endPointConfiguration3);
        verify(webServicesService, never()).publishEndPoint(deletedEndPointConfiguration);
        verify(webServicesService, never()).removeEndPoint(deletedEndPointConfiguration);
    }

    @Test
    public void testReconfigureDropsSupportedForDeletedEndPoint() throws Exception {
        when(appServer.isActive()).thenReturn(true);
        appService.reconfigureEndPoint("deleted");

        verify(webServicesService).removeEndPoint(deletedEndPointConfiguration);
        ArgumentCaptor<String> msg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LogLevel> logLevelArgumentCaptor = ArgumentCaptor.forClass(LogLevel.class);

        verify(deletedEndPointConfiguration).log(logLevelArgumentCaptor.capture(), msg.capture());
        assertThat(msg.getValue()).startsWith("Stopping");
        assertThat(logLevelArgumentCaptor.getValue()).isEqualTo(LogLevel.FINE);
        verify(webServicesService, never()).publishEndPoint(endPointConfiguration1);
        verify(webServicesService, never()).removeEndPoint(endPointConfiguration1);
        verify(webServicesService, never()).publishEndPoint(endPointConfiguration2);
        verify(webServicesService, never()).removeEndPoint(endPointConfiguration2);
        verify(webServicesService, never()).publishEndPoint(endPointConfiguration3);
        verify(webServicesService, never()).removeEndPoint(endPointConfiguration3);
    }
}
