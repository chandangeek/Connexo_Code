package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.*;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.InvalidateCacheRequest;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.cron.impl.DefaultCronExpressionParser;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import org.assertj.core.data.MapEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AppServiceImplTest {

    private static final String APP_SERVER_NAME = "appServerName";
    private static final String MESSAGING_NAME = "messagingName";
    private static final String COMPONENT_NAME = "ComponentName";
    private static final String TABLE_NAME = "tableName";
    private static final String SERIALIZED = "Serialized";
    private static final int ID = 21564156;
    private AppServiceImpl appService;

    private CountDownLatch arrivalLatch = new CountDownLatch(1); // latch that mocking can count down on when they arrive at the point that verifications may begin

    @Mock
    private BundleContext bundleContext;
    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    @Mock
    private DataMapper<AppServer> appServerFactory;
    @Mock
    private BundleContext context;
    @Mock
    private AppServerImpl appServer;
    @Mock
    private DataMapper<ImportScheduleOnAppServer> importScheduleOnAppServerFactory;
    @Mock
    private DataMapper<ImportFolderForAppServer> importFolderForAppServerFactory;
    @Mock
    private MessageService messageService;
    @Mock
    private SubscriberSpec subscriberSpec;
    @Mock
    private TransactionService transactionService;
    @Mock
    private UserService userService;
    @Mock
    private User batchUser;
    @Mock
    private TaskService taskService;
    @Mock
    private ImportScheduleOnAppServer importTask1, importTask2;
    @Mock
    private ImportFolderForAppServer importFolderForAppServer;
    @Mock
    Path importFolder;
    @Mock
    private ImportSchedule schedule1, schedule2;
    @Mock
    private FileImportService fileImportService;
    @Mock
    private InvalidateCacheRequest invalidateCacheRequest;
    @Mock
    private DestinationSpec destination;
    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private JsonService jsonService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat format;
    @Mock
    private QueryService queryService;

    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws SQLException {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString(), any())).thenReturn(table);
        when(dataModel.isInstalled()).thenReturn(true);
        when(dataModel.mapper(AppServer.class)).thenReturn(appServerFactory);
        when(dataModel.mapper(ImportScheduleOnAppServer.class)).thenReturn(importScheduleOnAppServerFactory);
        when(dataModel.mapper(ImportFolderForAppServer.class)).thenReturn(importFolderForAppServerFactory);
        when(dataModel.isInstalled()).thenReturn(true);
        doReturn(Collections.<SubscriberExecutionSpec>emptyList()).when(appServer).getSubscriberExecutionSpecs();
        when(importScheduleOnAppServerFactory.find("appServer", appServer)).thenReturn(Collections.<ImportScheduleOnAppServer>emptyList());
        when(importFolderForAppServerFactory.getOptional(Matchers.any())).thenReturn(Optional.of(importFolderForAppServer));
        when(importFolderForAppServer.getImportFolder()).thenReturn(Optional.of(importFolder));
        when(appServer.isRecurrentTaskActive()).thenReturn(false);
        when(appServer.messagingName()).thenReturn(MESSAGING_NAME);
        when(messageService.getSubscriberSpec(MESSAGING_NAME, MESSAGING_NAME)).thenReturn(Optional.empty());
        when(messageService.getSubscriberSpec("AllServers", MESSAGING_NAME)).thenReturn(Optional.empty());
        when(userService.findUser("batch executor")).thenReturn(Optional.of(batchUser));
        when(importTask1.getImportSchedule()).thenReturn(Optional.of(schedule1));
        when(importTask2.getImportSchedule()).thenReturn(Optional.of(schedule2));
        when(subscriberSpec.getDestination()).thenReturn(destination);
        when(destination.message(anyString())).thenReturn(messageBuilder);
        when(nlsService.getThesaurus(AppService.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(thesaurus);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(format);
        when(appServerFactory.getOptional(any())).thenReturn(Optional.<AppServer>empty());
        when(appServer.getName()).thenReturn("TEST_APP_SERVER");
        setupBlockingCancellableSubscriberSpec();
        setupFakeTransactionService();

        appService = new AppServiceImpl(ormService, nlsService, transactionService, messageService, new DefaultCronExpressionParser(), jsonService, fileImportService, taskService, userService, queryService, bundleContext);
    }

    @SuppressWarnings("unchecked")
	private void setupFakeTransactionService() {
        when(transactionService.execute(any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((Transaction<?>) invocationOnMock.getArguments()[0]).perform();
            }
        });

    }

    private void setupBlockingCancellableSubscriberSpec() throws SQLException {
        final CountDownLatch cancelLatch = new CountDownLatch(1);
        when(subscriberSpec.receive()).thenAnswer(new Answer<Message>() {
            @Override
            public Message answer(InvocationOnMock invocationOnMock) throws Throwable {
                arrivalLatch.countDown();
                cancelLatch.await();
                return null;
            }
        });
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                cancelLatch.countDown();
                return null;
            }
        }).when(subscriberSpec).cancel();

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testActivateNamedListensToAllServerMessages() throws InterruptedException, SQLException {
        when(context.getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        when(appServerFactory.getOptional(APP_SERVER_NAME)).thenReturn(Optional.<AppServer>of(appServer));
        when(messageService.getSubscriberSpec("AllServers", MESSAGING_NAME)).thenReturn(Optional.of(subscriberSpec));

        try {
            appService.activate(context);

            arrivalLatch.await(); // wait until receive() blocks

            verify(subscriberSpec).receive();
        } finally {
            subscriberSpec.cancel(); // unblock the receive();
        }
    }

    @Test
    public void testDeactivateProperlyShutsDownListeningToAllServerMessages() throws InterruptedException, SQLException {
        when(context.getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        when(appServerFactory.getOptional(APP_SERVER_NAME)).thenReturn(Optional.<AppServer>of(appServer));
        when(messageService.getSubscriberSpec("AllServers", MESSAGING_NAME)).thenReturn(Optional.of(subscriberSpec));

        try {
            appService.activate(context);

            arrivalLatch.await(); // wait until receive() blocks

            appService.deactivate();

            verify(subscriberSpec).cancel();
        } finally {
            subscriberSpec.cancel(); // unblock the receive();
        }
    }

    @Test
    public void testActivateNamedListensToAppServerMessages() throws InterruptedException, SQLException {
        when(context.getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        when(appServerFactory.getOptional(APP_SERVER_NAME)).thenReturn(Optional.<AppServer>of(appServer));
        when(messageService.getSubscriberSpec(MESSAGING_NAME, MESSAGING_NAME)).thenReturn(Optional.of(subscriberSpec));

        try {
            appService.activate(context);

            arrivalLatch.await(); // wait until receive() blocks

            verify(subscriberSpec).receive();
        } finally {
            subscriberSpec.cancel(); // unblock the receive();
        }
    }

    @Test
    public void testDeactivateProperlyShutsDownListeningToAppServerMessages() throws InterruptedException, SQLException {
        when(context.getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        when(appServerFactory.getOptional(APP_SERVER_NAME)).thenReturn(Optional.<AppServer>of(appServer));
        when(messageService.getSubscriberSpec(MESSAGING_NAME, MESSAGING_NAME)).thenReturn(Optional.of(subscriberSpec));

        try {
            appService.activate(context);

            arrivalLatch.await(); // wait until receive() blocks

            appService.deactivate();

            verify(subscriberSpec).cancel();
        } finally {
            subscriberSpec.cancel(); // unblock the receive();
        }
    }

    @Test
    public void testLaunchesConfiguredRecurrentTasks() {
        when(context.getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        when(appServerFactory.getOptional(APP_SERVER_NAME)).thenReturn(Optional.<AppServer>of(appServer));
        when(appServer.isRecurrentTaskActive()).thenReturn(true);

        appService.activate(context);

        verify(taskService).launch();
    }

    @Test
    public void testLaunchConfiguredFileImports() {
        when(context.getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        when(appServerFactory.getOptional(APP_SERVER_NAME)).thenReturn(Optional.<AppServer>of(appServer));
        when(importScheduleOnAppServerFactory.find("appServer", appServer)).thenReturn(Arrays.asList(importTask1, importTask2));

        appService.activate(context);

        verify(fileImportService).schedule(schedule1);
        verify(fileImportService).schedule(schedule2);
    }

    @Test
    public void testSubscribesToInvalidateCacheRequestsAndBroadCastsToAllServers() {

        when(invalidateCacheRequest.getComponentName()).thenReturn(COMPONENT_NAME);
        when(invalidateCacheRequest.getTableName()).thenReturn(TABLE_NAME);
        when(context.getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        when(appServerFactory.getOptional(APP_SERVER_NAME)).thenReturn(Optional.<AppServer>of(appServer));
        when(messageService.getSubscriberSpec("AllServers", MESSAGING_NAME)).thenReturn(Optional.of(subscriberSpec));
        doReturn(null).when(subscriberSpec).receive();
        when(jsonService.serialize(any())).thenReturn(SERIALIZED);

        appService.activate(context);

        appService.handle(invalidateCacheRequest);

        verify(destination).message(SERIALIZED);
        verify(messageBuilder).send();
        ArgumentCaptor<AppServerCommand> commandCaptor = ArgumentCaptor.forClass(AppServerCommand.class);
        verify(jsonService).serialize(commandCaptor.capture());

        assertThat(commandCaptor.getValue().getCommand()).isEqualTo(Command.INVALIDATE_CACHE);
        assertThat(commandCaptor.getValue().getProperties())
                .contains(MapEntry.entry("componentName", COMPONENT_NAME), MapEntry.entry("tableName", TABLE_NAME));
    }

    @Test
    public void testActivateWithoutNameRunsAnonymously() {
        when(context.getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(null);
        when(messageService.getSubscriberSpec("AllServers", MESSAGING_NAME)).thenReturn(Optional.of(subscriberSpec));

        appService.activate(context);

        assertThat(appService.getAppServer().isPresent()).isFalse();
    }

    @Test
    public void testActivateWithUnknownNameRunsAnonymously() {
        when(context.getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        when(appServerFactory.getOptional(APP_SERVER_NAME)).thenReturn(Optional.empty());
        when(messageService.getSubscriberSpec("AllServers", MESSAGING_NAME)).thenReturn(Optional.of(subscriberSpec));

        appService.activate(context);

        assertThat(appService.getAppServer().isPresent()).isFalse();
    }

    @Test
    public void testStop() throws InterruptedException, SQLException, BundleException {
        arrivalLatch = new CountDownLatch(2);
        when(context.getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        when(appServerFactory.getOptional(APP_SERVER_NAME)).thenReturn(Optional.<AppServer>of(appServer));
        when(messageService.getSubscriberSpec("AllServers", MESSAGING_NAME)).thenReturn(Optional.of(subscriberSpec));
        when(messageService.getSubscriberSpec(MESSAGING_NAME, MESSAGING_NAME)).thenReturn(Optional.of(subscriberSpec));
        when(appServer.isRecurrentTaskActive()).thenReturn(true);
        when(importScheduleOnAppServerFactory.find("appServer", appServer)).thenReturn(Arrays.asList(importTask1, importTask2));

        Bundle mainBundle = mock(Bundle.class);
        when(context.getBundle(0)).thenReturn(mainBundle);
        final CountDownLatch stopLatch = new CountDownLatch(1);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                stopLatch.countDown();
                return null;
            }
        }).when(mainBundle).stop();

        try {
            appService.activate(context);

            arrivalLatch.await();

            appService.stop();

            assertThat(Thread.interrupted()).isTrue();

            stopLatch.await();

            verify(mainBundle).stop();

        } finally {
            subscriberSpec.cancel(); // unblock the receive();
            Thread.interrupted(); // make sure to clear the interrupted flag
        }
    }

    @Test
    public void testHandleStop() throws InterruptedException, SQLException, BundleException {
        when(context.getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        when(appServerFactory.getOptional(APP_SERVER_NAME)).thenReturn(Optional.<AppServer>of(appServer));
        when(messageService.getSubscriberSpec("AllServers", MESSAGING_NAME)).thenReturn(Optional.of(subscriberSpec));

        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(SERIALIZED.getBytes());
        AppServerCommand appServerCommand = new AppServerCommand(Command.STOP);
        when(jsonService.deserialize(SERIALIZED.getBytes(), AppServerCommand.class)).thenReturn(appServerCommand);

        doReturn(message).doReturn(null).when(subscriberSpec).receive();

        Bundle mainBundle = mock(Bundle.class);
        when(context.getBundle(0)).thenReturn(mainBundle);
        final CountDownLatch stopLatch = new CountDownLatch(1);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                stopLatch.countDown();
                return null;
            }
        }).when(mainBundle).stop();

        try {
            appService.activate(context);

            stopLatch.await();

            verify(mainBundle).stop();

            verify(subscriberSpec).receive();
        } finally {
            subscriberSpec.cancel(); // unblock the receive();
            Thread.interrupted(); // clear interrupted flag
        }
    }

    @Test
    public void testHandleInvalidateCache() throws InterruptedException, SQLException, BundleException {
        when(context.getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        when(appServerFactory.getOptional(APP_SERVER_NAME)).thenReturn(Optional.<AppServer>of(appServer));
        when(messageService.getSubscriberSpec("AllServers", MESSAGING_NAME)).thenReturn(Optional.of(subscriberSpec));

        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(SERIALIZED.getBytes());
        Properties props = new Properties();
        props.put("componentName", COMPONENT_NAME);
        props.put("tableName", TABLE_NAME);
        AppServerCommand appServerCommand = new AppServerCommand(Command.INVALIDATE_CACHE, props);
        when(jsonService.deserialize(SERIALIZED.getBytes(), AppServerCommand.class)).thenReturn(appServerCommand);

        doReturn(message).doAnswer(new Answer<Message>() {
            @Override
            public Message answer(InvocationOnMock invocationOnMock) throws Throwable {
                arrivalLatch.countDown();
                return null;
            }
        }).when(subscriberSpec).receive();

        try {
            appService.activate(context);

            arrivalLatch.await();

            verify(ormService).invalidateCache(COMPONENT_NAME, TABLE_NAME);

        } finally {
            subscriberSpec.cancel(); // unblock the receive();
        }
    }

    @Test
    public void testHandleFileImportActivated() throws InterruptedException, SQLException, BundleException {
        when(context.getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        when(appServerFactory.getOptional(APP_SERVER_NAME)).thenReturn(Optional.<AppServer>of(appServer));
        when(messageService.getSubscriberSpec("AllServers", MESSAGING_NAME)).thenReturn(Optional.of(subscriberSpec));

        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(SERIALIZED.getBytes());
        Properties props = new Properties();
        props.put("id", String.valueOf(ID));
        AppServerCommand appServerCommand = new AppServerCommand(Command.FILEIMPORT_ACTIVATED, props);
        when(jsonService.deserialize(SERIALIZED.getBytes(), AppServerCommand.class)).thenReturn(appServerCommand);
        when(fileImportService.getImportSchedule(ID)).thenReturn(Optional.of(schedule1));

        doReturn(message).doAnswer(new Answer<Message>() {
            @Override
            public Message answer(InvocationOnMock invocationOnMock) throws Throwable {
                arrivalLatch.countDown();
                return null;
            }
        }).when(subscriberSpec).receive();

        try {
            appService.activate(context);

            arrivalLatch.await();

            verify(fileImportService).schedule(schedule1);

        } finally {
            subscriberSpec.cancel(); // unblock the receive();
        }
    }

    @Test
    public void testGetHostName() throws UnknownHostException {
        System.out.println(InetAddress.getLocalHost().getHostName());

    }

}
