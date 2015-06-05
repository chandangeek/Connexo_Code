package com.elster.jupiter.appserver.impl;


import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.appserver.ServerMessageQueueMissing;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppServerImplTest {

    private static final String NAME = "Name";
    private static final String SERIALIZED = "SERIALIZED";

    private AppServerImpl appServer;

    @Mock
    private SubscriberSpec subscriberSpec;
    @Mock
    private DataMapper<SubscriberExecutionSpecImpl> subscriberExecutionSpecFactory;
    @Mock
    private DataMapper<ImportScheduleOnAppServerImpl> importScheduleOnAppServerFactory;
    @Mock
    private CronExpression cronExpression;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DestinationSpec destination;
    @Mock
    private SubscriberExecutionSpecImpl exSpec1, exSpec2;
    @Mock
    private ImportScheduleOnAppServerImpl importScheduleOnAppServer;
    @Mock
    private AppServerCommand command;
    @Mock
    private FileImportService fileImportService;
    @Mock
    private MessageService messageService;
    @Mock
    private JsonService jsonService;
    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private DataModel dataModel;
    @Mock
    private CronExpressionParser cronExpressionParser;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat format;
    @Mock
    private ImportSchedule importSchedule;

    @Before
    public void setUp() {
        when(dataModel.mapper(SubscriberExecutionSpecImpl.class)).thenReturn(subscriberExecutionSpecFactory);
        when(dataModel.mapper(ImportScheduleOnAppServerImpl.class)).thenReturn(importScheduleOnAppServerFactory);
        when(subscriberSpec.getDestination()).thenReturn(destination);
        when(dataModel.getInstance(AppServerImpl.class)).thenReturn(new AppServerImpl(dataModel, cronExpressionParser, fileImportService, messageService, jsonService, thesaurus));

        when(dataModel.getInstance(SubscriberExecutionSpecImpl.class)).thenReturn(new SubscriberExecutionSpecImpl(dataModel, messageService));
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(format);
        when(messageService.getDestinationSpec(any())).thenReturn(Optional.of(destination));

        appServer = AppServerImpl.from(dataModel, NAME, cronExpression);

        when(importSchedule.getId()).thenReturn(1L);
        when(fileImportService.getImportSchedule(1L)).thenReturn(Optional.of(importSchedule));
        when(importScheduleOnAppServer.getAppServer()).thenReturn(appServer);
        when(importScheduleOnAppServer.getImportSchedule()).thenReturn(Optional.of(importSchedule));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreateSubscriberExecutionSpecIsPersisted() {
        SubscriberExecutionSpecImpl spec = appServer.createSubscriberExecutionSpec(subscriberSpec, 5);

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
        String messagingName = "AppServer_" + NAME.toUpperCase();
        when(messageService.getDestinationSpec(messagingName)).thenReturn(Optional.empty());

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

    @Test
    public void testGetImportScheduleOnAppServer(){
        when(importScheduleOnAppServerFactory.find("appServer", appServer)).thenReturn(Arrays.asList(importScheduleOnAppServer));

        assertThat(appServer.getImportSchedulesOnAppServer()).hasSize(1).contains(importScheduleOnAppServer);
    }

    @Test
    public void testAddImportScheduleOnAppServer(){
        ImportScheduleOnAppServerImpl importScheduleOnAppServer = appServer.addImportScheduleOnAppServer(importSchedule);

        verify(importScheduleOnAppServerFactory).persist(importScheduleOnAppServer);
        assertThat(importScheduleOnAppServer.getAppServer()).isEqualTo(appServer);
        assertThat(importScheduleOnAppServer.getImportSchedule().get()).isEqualTo(importSchedule);
    }

    @Test
    public void testRemoveImportScheduleOnAppServer() {
        when(importScheduleOnAppServerFactory.find("appServer", appServer)).thenReturn(Arrays.asList(importScheduleOnAppServer));
        appServer.removeImportScheduleOnAppServer(importScheduleOnAppServer);

        verify(importScheduleOnAppServerFactory).remove(importScheduleOnAppServer);
        assertThat(appServer.getImportSchedulesOnAppServer()).hasSize(0);
    }
}
