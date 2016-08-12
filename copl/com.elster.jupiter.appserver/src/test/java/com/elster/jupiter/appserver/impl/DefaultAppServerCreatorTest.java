package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.DequeueOptions;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Provider;
import javax.validation.ValidatorFactory;
import java.util.HashSet;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAppServerCreatorTest {

    private static final String NAME = "name";
    @Captor
    private ArgumentCaptor<AppServer> appServerCaptor;

    @Mock
    private TransactionService transactionService;
    @Mock
    private DataMapper<AppServer> appServerFactory;
    @Mock
    private CronExpression cronExpression;
    @Mock
    private FileImportService fileImportService;
    @Mock
    private MessageService messageService;
    @Mock
    private QueueTableSpec queueTableSpec;
    @Mock
    private DestinationSpec newDestination, allServersDestination;
    @Mock
    private DataModel dataModel;
    @Mock
    private CronExpressionParser cronExpressionParser;
    @Mock
    private JsonService jsonService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private javax.validation.Validator javaxValidator;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private WebServicesService webServicesService;
    @Mock
    private Provider<EndPointForAppServerImpl> webServicesForAppServerProvider;
    @Mock
    private EndPointConfigurationService endPointConfigurationService;
    @Mock
    private EventService eventService;

    @Before
    public void setUp() {
        when(dataModel.mapper(AppServer.class)).thenReturn(appServerFactory);
        when(messageService.getQueueTableSpec("MSG_RAWQUEUETABLE")).thenReturn(Optional.of(queueTableSpec));
        when(queueTableSpec.createDestinationSpec(anyString(), anyInt())).thenReturn(newDestination);
        when(messageService.getDestinationSpec(AppService.ALL_SERVERS)).thenReturn(Optional.of(allServersDestination));
        DestinationSpec.SubscriberSpecBuilder allServersSubscriberSpecBuilder = mockSubscriberSpecBuilder();
        when(allServersDestination.subscribe(anyString())).thenReturn(allServersSubscriberSpecBuilder);
        when(messageService.getDestinationSpec("AppServer_" + NAME.toUpperCase())).thenReturn(Optional.empty());
        when(dataModel.getInstance(AppServerImpl.class)).thenReturn(new AppServerImpl(dataModel, cronExpressionParser, fileImportService, messageService, jsonService, thesaurus, transactionService, threadPrincipalService, webServicesForAppServerProvider, webServicesService, eventService, endPointConfigurationService));
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(javaxValidator);
        when(javaxValidator.validate(any(javax.validation.Validator.class), any(), any())).thenReturn(new HashSet<>());

        setupFakeTransactionService();
    }

    @SuppressWarnings("unchecked")
	private void setupFakeTransactionService() {
        when(transactionService.execute(any())).thenAnswer(invocationOnMock -> ((Transaction<?>) invocationOnMock.getArguments()[0]).perform());
    }

    @Test
    public void testCreateAppServerSavesAppServerInstance() {
        DestinationSpec.SubscriberSpecBuilder subscriberSpecBuilder = mockSubscriberSpecBuilder();
        when(newDestination.subscribe(anyString())).thenReturn(subscriberSpecBuilder);
        new DefaultAppServerCreator(dataModel, messageService).createAppServer(NAME, cronExpression);

        verify(dataModel).persist(appServerCaptor.capture());

        assertThat(appServerCaptor.getValue().getName()).isEqualTo(NAME);
        assertThat(appServerCaptor.getValue().getScheduleFrequency()).isEqualTo(cronExpression);
    }

    @Test
    public void testCreateAppServerCreatesDestinationAndSubscribes() {
        DestinationSpec.SubscriberSpecBuilder subscriberSpecBuilder = mockSubscriberSpecBuilder();
        when(newDestination.subscribe(anyString())).thenReturn(subscriberSpecBuilder);
        new DefaultAppServerCreator(dataModel, messageService).createAppServer(NAME, cronExpression);

        verify(queueTableSpec).createDestinationSpec(anyString(), anyInt());
        verify(subscriberSpecBuilder).systemManaged();
        verify(subscriberSpecBuilder).create();
    }

    @Test
    public void testCreateAppServerSubscribesToAllServersDestination() {
        DestinationSpec.SubscriberSpecBuilder subscriberSpecBuilder = mockSubscriberSpecBuilder();
        when(newDestination.subscribe(anyString())).thenReturn(subscriberSpecBuilder);
        new DefaultAppServerCreator(dataModel, messageService).createAppServer(NAME, cronExpression);

        verify(subscriberSpecBuilder).systemManaged();
        verify(subscriberSpecBuilder).create();
    }

    private DestinationSpec.SubscriberSpecBuilder mockSubscriberSpecBuilder() {
        DestinationSpec.SubscriberSpecBuilder builder = mock(DestinationSpec.SubscriberSpecBuilder.class);
        when(builder.with(any(DequeueOptions.class))).thenReturn(builder);
        when(builder.systemManaged()).thenReturn(builder);
        when(builder.systemManaged(anyBoolean())).thenReturn(builder);
        return builder;
    }

}
