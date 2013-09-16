package com.elster.jupiter.appserver.impl;


import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.ServerMessageQueueMissing;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppServerImplTest {

    private static final String NAME = "Name";
    private static final String SERIALIZED = "SERIALIZED";
    private AppServerImpl appServer;

    @Mock
    private SubscriberSpec subscriberSpec;
    @Mock
    private OrmClient ormClient;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private DataMapper<SubscriberExecutionSpec> subscriberExecutionSpecFactory;
    @Mock
    private CronExpression cronExpression;
    @Mock
    private DestinationSpec destination;
    @Mock
    private SubscriberExecutionSpec exSpec1, exSpec2;
    @Mock
    private AppServerCommand command;
    @Mock
    private MessageService messageService;
    @Mock
    private JsonService jsonService;
    @Mock
    private MessageBuilder messageBuilder;

    @Before
    public void setUp() {

        when(serviceLocator.getOrmClient()).thenReturn(ormClient);
        when(ormClient.getSubscriberExecutionSpecFactory()).thenReturn(subscriberExecutionSpecFactory);
        when(subscriberSpec.getDestination()).thenReturn(destination);
        when(serviceLocator.getMessageService()).thenReturn(messageService);
        when(serviceLocator.getJsonService()).thenReturn(jsonService);

        Bus.setServiceLocator(serviceLocator);

        appServer = new AppServerImpl(NAME, cronExpression);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testCreateSubscriberExecutionSpecIsPersisted() {
        SubscriberExecutionSpec spec = appServer.createSubscriberExecutionSpec(subscriberSpec, 5);

        verify(subscriberExecutionSpecFactory).persist(spec);
    }

    @Test
    public void testCreateSubscriberExecutionSpecHasCorrectAppServer() {
        SubscriberExecutionSpec spec = appServer.createSubscriberExecutionSpec(subscriberSpec, 5);

        assertThat(spec.getAppServer()).isEqualTo(appServer);
    }

    @Test
    public void testCreateSubscriberExecutionSpecHasCorrectSubscriber() {
        SubscriberExecutionSpec spec = appServer.createSubscriberExecutionSpec(subscriberSpec, 5);

        assertThat(spec.getSubscriberSpec()).isEqualTo(subscriberSpec);
    }

    @Test
    public void testCreateSubscriberExecutionSpecHasCorrectNumberOfThreads() {
        SubscriberExecutionSpec spec = appServer.createSubscriberExecutionSpec(subscriberSpec, 5);

        assertThat(spec.getThreadCount()).isEqualTo(5);
    }

    @Test
    public void testCreationNameOk() {
        assertThat(appServer.getName()).isEqualTo(NAME);
    }

    @Test
    public void testCreationCronExpressionOk() {
        assertThat(appServer.getScheduleFrequency()).isEqualTo(cronExpression);
    }

    @Test
    public void testGetSubscriberExecutionSpecs() {
        when(subscriberExecutionSpecFactory.find("appServer", appServer)).thenReturn(Arrays.asList(exSpec1, exSpec2));

        assertThat(appServer.getSubscriberExecutionSpecs()).hasSize(2).contains(exSpec1).contains(exSpec2);
    }

    @Test
    public void testSendCommand() {
        String messagingName = "AppServer_" + NAME;
        when(messageService.getDestinationSpec(messagingName)).thenReturn(Optional.of(destination));
        when(destination.message(SERIALIZED)).thenReturn(messageBuilder);
        when(jsonService.serialize(command)).thenReturn(SERIALIZED);

        appServer.sendCommand(command);

        verify(messageBuilder).send();
    }

    @Test(expected = ServerMessageQueueMissing.class)
    public void testSendCommandWithServerMessageQueueMissing() {
        String messagingName = "AppServer_" + NAME;
        when(messageService.getDestinationSpec(messagingName)).thenReturn(Optional.<DestinationSpec>absent());

        appServer.sendCommand(command);

    }

    @Test
    public void testRecurrentTaskActiveToTrue() {
        appServer.setRecurrentTaskActive(true);

        assertThat(appServer.isRecurrentTaskActive()).isTrue();
    }

    @Test
    public void testRecurrentTaskActiveToFalse() {
        appServer.setRecurrentTaskActive(false);

        assertThat(appServer.isRecurrentTaskActive()).isFalse();
    }

}
