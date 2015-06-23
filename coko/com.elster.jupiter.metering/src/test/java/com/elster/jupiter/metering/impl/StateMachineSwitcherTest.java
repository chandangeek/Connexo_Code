package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.IncompatibleFiniteStateMachineChangeException;
import com.elster.jupiter.metering.events.SwitchStateMachineFailureEvent;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.sql.SqlFragment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link StateMachineSwitcher} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-19 (16:36)
 */
@RunWith(MockitoJUnitRunner.class)
public class StateMachineSwitcherTest {

    private static final long OLD_STATE_MACHINE_ID = 97L;
    private static final long NEW_STATE_MACHINE_ID = 103L;
    private static final long DEVICE_ID = 107L;
    private static final int BATCH_SIZE = 2;
    public static final String NOT_MAPPED_STATE_NAME = "Not Mapped";
    public static final Instant EFFECTIVE_TIMESTAMP = Instant.ofEpochMilli(100000L);

    @Mock
    private DataModel dataModel;
    @Mock
    private TransactionService transactionService;
    @Mock
    private TransactionContext transactionContext;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private FiniteStateMachine oldStateMachine;
    @Mock
    private FiniteStateMachine newStateMachine;
    @Mock
    private Subquery deviceAmrIdSubquery;
    @Mock
    private EventService eventService;
    @Mock
    private JsonService jsonService;
    @Mock
    private MessageService messageService;
    @Mock
    private FiniteStateMachineService finiteStateMachineService;

    @Before
    public void initializeMocks() throws SQLException {
        when(this.oldStateMachine.getId()).thenReturn(OLD_STATE_MACHINE_ID);
        when(this.newStateMachine.getId()).thenReturn(NEW_STATE_MACHINE_ID);
        when(this.transactionService.getContext()).thenReturn(this.transactionContext);
        when(this.dataModel.getConnection(true)).thenReturn(this.connection);
        SqlFragment sqlFragment = mock(SqlFragment.class);
        when(this.deviceAmrIdSubquery.toFragment()).thenReturn(sqlFragment);
        when(sqlFragment.getText()).thenReturn("select dev.id from DDC_DEVICE dev where dev.devtype = 1");
        when(this.connection.prepareStatement(anyString())).thenReturn(this.preparedStatement);
        when(this.preparedStatement.executeQuery()).thenReturn(this.resultSet);
    }

    @Test(expected = IncompatibleFiniteStateMachineChangeException.class)
    public void validationFailsWhenNotAllStatesMap() throws SQLException {
        StateMachineSwitcher switcher = this.getValidatingTestInstance();
        this.mockIncompatibleStates();

        try {
            // Business method
            switcher.validate(EFFECTIVE_TIMESTAMP, this.oldStateMachine, this.newStateMachine, this.deviceAmrIdSubquery);

        }
        catch (IncompatibleFiniteStateMachineChangeException e) {
            // Expected but need to catch it to also verify closing of resources
            verify(this.resultSet).close();
            verify(this.preparedStatement).close();
            throw e;    // Rethrow to satisfy the expected exception rule in the annotation
        }
    }

    @Test
    public void validationSucceedsWhenAllStatesMap() throws SQLException {
        StateMachineSwitcher switcher = this.getValidatingTestInstance();
        this.mockNoIncompatibleStates();

        // Business method
        switcher.validate(EFFECTIVE_TIMESTAMP, this.oldStateMachine, this.newStateMachine, this.deviceAmrIdSubquery);

        // Asserts
        verify(this.resultSet).close();
        verify(this.preparedStatement).close();
    }

    @Test
    public void switchEventsArePublishedInBatch() throws SQLException {
        StateMachineSwitcher publisher = this.getPublishingTestInstance(BATCH_SIZE);
        this.mockNoIncompatibleStates();
        this.mockEndDevices(5);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
        when(destinationSpec.message(any(byte[].class))).thenReturn(messageBuilder);
        when(this.messageService.getDestinationSpec(SwitchStateMachineEvent.DESTINATION)).thenReturn(Optional.of(destinationSpec));

        // Business method
        publisher.publishEvents(EFFECTIVE_TIMESTAMP, this.oldStateMachine, this.newStateMachine, this.deviceAmrIdSubquery);

        // Asserts: 5 devices in batches of 2 should create 3 batches
        verify(destinationSpec, times(3)).message(anyString());
        // All 3 batch messages should have been sent to the destination
        verify(messageBuilder, times(3)).send();
    }

    @Test
    public void processWithDeviceThatNoLongerExistsDoesNotCrashUnexpectedly() {
        String payload = "processWithDeviceThatNoLongerExistsDoesNotCrashUnexpectedly";
        when(this.jsonService.deserialize(payload.getBytes(), SwitchStateMachineEvent.class))
                .thenReturn(
                        new SwitchStateMachineEvent(
                                Instant.now().toEpochMilli(),
                                OLD_STATE_MACHINE_ID,
                                NEW_STATE_MACHINE_ID,
                                Collections.singletonList(DEVICE_ID)));
        DataMapper<EndDevice> mapper = mock(DataMapper.class);
        when(this.dataModel.mapper(EndDevice.class)).thenReturn(mapper);
        when(mapper.getOptional(DEVICE_ID)).thenReturn(Optional.<EndDevice>empty());
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());
        StateMachineSwitcher handler = this.getHandlingTestInstance();

        // Business method
        handler.process(message);

        // Asserts
        verify(mapper).getOptional(DEVICE_ID);
    }

    @Test
    public void processWithNewFiniteStateMachineNoLongerExistsDoesNotCrashUnexpectedly() {
        String payload = "processWithNewFiniteStateMachineNoLongerExistsDoesNotCrashUnexpectedly";
        when(this.jsonService.deserialize(payload.getBytes(), SwitchStateMachineEvent.class))
                .thenReturn(
                        new SwitchStateMachineEvent(
                                Instant.now().toEpochMilli(),
                                OLD_STATE_MACHINE_ID,
                                NEW_STATE_MACHINE_ID,
                                Collections.singletonList(DEVICE_ID)));
        DataMapper<EndDevice> mapper = mock(DataMapper.class);
        when(this.dataModel.mapper(EndDevice.class)).thenReturn(mapper);
        ServerEndDevice endDevice = mock(ServerEndDevice.class);
        when(endDevice.getId()).thenReturn(DEVICE_ID);
        when(mapper.getOptional(DEVICE_ID)).thenReturn(Optional.of(endDevice));
        when(this.finiteStateMachineService.findFiniteStateMachineById(NEW_STATE_MACHINE_ID)).thenReturn(Optional.<FiniteStateMachine>empty());
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());
        StateMachineSwitcher handler = this.getHandlingTestInstance();

        // Business method
        handler.process(message);

        // Asserts
        verify(mapper).getOptional(DEVICE_ID);
        verify(this.finiteStateMachineService).findFiniteStateMachineById(NEW_STATE_MACHINE_ID);
    }

    @Test
    public void processWithSuccess() {
        String payload = "processWithSuccess";
        Instant now = Instant.now();
        when(this.jsonService.deserialize(payload.getBytes(), SwitchStateMachineEvent.class))
                .thenReturn(
                        new SwitchStateMachineEvent(
                                now.toEpochMilli(),
                                OLD_STATE_MACHINE_ID,
                                NEW_STATE_MACHINE_ID,
                                Collections.singletonList(DEVICE_ID)));
        DataMapper<EndDevice> mapper = mock(DataMapper.class);
        when(this.dataModel.mapper(EndDevice.class)).thenReturn(mapper);
        ServerEndDevice endDevice = mock(ServerEndDevice.class);
        when(endDevice.getId()).thenReturn(DEVICE_ID);
        State stateBeforeSwitching = mock(State.class);
        String stateNameBeforeSwitching = "StateBeforeSwitching";
        when(stateBeforeSwitching.getName()).thenReturn(stateNameBeforeSwitching);
        when(endDevice.getState()).thenReturn(Optional.of(stateBeforeSwitching));
        when(mapper.getOptional(DEVICE_ID)).thenReturn(Optional.of(endDevice));
        when(this.finiteStateMachineService.findFiniteStateMachineById(NEW_STATE_MACHINE_ID)).thenReturn(Optional.of(this.newStateMachine));
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());
        StateMachineSwitcher handler = this.getHandlingTestInstance();

        // Business method
        handler.process(message);

        // Asserts
        verify(mapper).getOptional(DEVICE_ID);
        verify(this.finiteStateMachineService).findFiniteStateMachineById(NEW_STATE_MACHINE_ID);
        verify(endDevice).changeStateMachine(this.newStateMachine, now);
    }

    @Test
    public void processWithMappedStateThatNoLongerExistsPublishesEvent() {
        String payload = "processWithMappedStateThatNoLongerExistsPublishesEvent";
        Instant now = Instant.now();
        when(this.jsonService.deserialize(payload.getBytes(), SwitchStateMachineEvent.class))
                .thenReturn(
                        new SwitchStateMachineEvent(
                                now.toEpochMilli(),
                                OLD_STATE_MACHINE_ID,
                                NEW_STATE_MACHINE_ID,
                                Collections.singletonList(DEVICE_ID)));
        DataMapper<EndDevice> mapper = mock(DataMapper.class);
        when(this.dataModel.mapper(EndDevice.class)).thenReturn(mapper);
        ServerEndDevice endDevice = mock(ServerEndDevice.class);
        when(endDevice.getId()).thenReturn(DEVICE_ID);
        State stateBeforeSwitching = mock(State.class);
        String stateNameBeforeSwitching = "StateBeforeSwitching";
        when(stateBeforeSwitching.getName()).thenReturn(stateNameBeforeSwitching);
        when(endDevice.getState()).thenReturn(Optional.of(stateBeforeSwitching));
        doThrow(AbstractEndDeviceImpl.StateNoLongerExistsException.class).when(endDevice).changeStateMachine(this.newStateMachine, now);
        when(mapper.getOptional(DEVICE_ID)).thenReturn(Optional.of(endDevice));
        when(this.finiteStateMachineService.findFiniteStateMachineById(NEW_STATE_MACHINE_ID)).thenReturn(Optional.of(this.newStateMachine));
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());
        StateMachineSwitcher handler = this.getHandlingTestInstance();

        // Business method
        handler.process(message);

        // Asserts
        verify(mapper).getOptional(DEVICE_ID);
        verify(this.finiteStateMachineService).findFiniteStateMachineById(NEW_STATE_MACHINE_ID);
        verify(endDevice).changeStateMachine(this.newStateMachine, now);
        ArgumentCaptor<SwitchStateMachineFailureEvent> eventArgumentCaptor = ArgumentCaptor.forClass(SwitchStateMachineFailureEvent.class);
        verify(this.eventService).postEvent(eq(EventType.SWITCH_STATE_MACHINE_FAILED.topic()), eventArgumentCaptor.capture());
        assertThat(eventArgumentCaptor.getValue()).isNotNull();
        assertThat(eventArgumentCaptor.getValue().getEndDeviceId()).isEqualTo(DEVICE_ID);
        assertThat(eventArgumentCaptor.getValue().getEndDeviceStateName()).isEqualTo(stateNameBeforeSwitching);
        assertThat(eventArgumentCaptor.getValue().getNewFiniteStateMachineId()).isEqualTo(NEW_STATE_MACHINE_ID);
        assertThat(eventArgumentCaptor.getValue().getOldFiniteStateMachineId()).isEqualTo(OLD_STATE_MACHINE_ID);
    }

    private void mockNoIncompatibleStates() throws SQLException {
        when(this.resultSet.next()).thenReturn(false);
    }

    private void mockIncompatibleStates() throws SQLException {
        when(this.resultSet.next()).thenReturn(true, false);
        when(this.resultSet.getString(1)).thenReturn(NOT_MAPPED_STATE_NAME);
        when(this.newStateMachine.getState(NOT_MAPPED_STATE_NAME)).thenReturn(Optional.<State>empty());
        State oldState = mock(State.class);
        when(this.oldStateMachine.getState(NOT_MAPPED_STATE_NAME)).thenReturn(Optional.of(oldState));
    }

    private void mockEndDevices(int numberOfDevices) {
        List<EndDevice> devices = new ArrayList<>(numberOfDevices);
        for (int i = 0; i < numberOfDevices; i++) {
            ServerEndDevice device = mock(ServerEndDevice.class);
            long id = i + 1;
            when(device.getId()).thenReturn(id);
            devices.add(device);
        }
        QueryExecutor<EndDevice> queryExecutor = mock(QueryExecutor.class);
        when(this.dataModel.query(eq(EndDevice.class), anyVararg())).thenReturn(queryExecutor);
        when(queryExecutor.select(any(Condition.class))).thenReturn(devices);
    }

    private StateMachineSwitcher getValidatingTestInstance() {
        return StateMachineSwitcher.forValidation(this.dataModel);
    }

    private StateMachineSwitcher getPublishingTestInstance(int batchSize) {
        return StateMachineSwitcher.forPublishing(batchSize, this.dataModel, this.messageService, this.jsonService);
    }

    private StateMachineSwitcher getHandlingTestInstance() {
        return StateMachineSwitcher.forHandling(this.dataModel, this.eventService, this.jsonService, this.finiteStateMachineService);
    }

}