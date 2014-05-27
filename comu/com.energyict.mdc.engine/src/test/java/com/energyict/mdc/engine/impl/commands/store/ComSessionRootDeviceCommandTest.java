package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.transaction.Transaction;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.model.ComServer;

import java.sql.SQLException;

import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
* Tests the {@link ComSessionRootDeviceCommand} component.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-08-22 (16:53)
*/
@RunWith(MockitoJUnitRunner.class)
public class ComSessionRootDeviceCommandTest {

    private ComServerDAOImpl mockComServerDAOButPerformTransactions() {
        final ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        doCallRealMethod().when(comServerDAO).storeMeterReadings(any(DeviceIdentifier.class), any(MeterReading.class));
        when(comServerDAO.executeTransaction(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ((Transaction<?>) invocation.getArguments()[0]).perform();
            }
        });
        return comServerDAO;
    }

    @Test
    public void testNoChildrenAfterConstruction () {
        // Business method
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();

        // Asserts
        assertThat(command.getChildren()).isEmpty();
    }

    @Test
    public void testAddOneChild () {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        DeviceCommand deviceCommand = this.mockDeviceCommand();

        // Business method
        command.add(deviceCommand);

        // Asserts
        assertThat(command.getChildren()).containsOnly(deviceCommand);
    }

    @Test
    public void testAddOnlyCreateComSessionChild () {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        CreateComSessionDeviceCommand deviceCommand = this.mockCreateComSessionDeviceCommand();

        // Business method
        command.add(deviceCommand);

        // Asserts
        assertThat(command.getChildren()).containsOnly(deviceCommand);
    }

    @Test
    public void testMultipleChildrenWithoutCreateComSession () {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        DeviceCommand deviceCommand1 = this.mockDeviceCommand();
        DeviceCommand deviceCommand2 = this.mockDeviceCommand();
        DeviceCommand deviceCommand3 = this.mockDeviceCommand();

        // Business method
        command.addAll(deviceCommand1, deviceCommand2, deviceCommand3);

        // Asserts
        assertThat(command.getChildren()).containsOnly(deviceCommand1, deviceCommand2, deviceCommand3);
    }

    @Test
    public void testMultipleChildrenWithCreateComSessionFirst () {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        CreateComSessionDeviceCommand deviceCommand1 = this.mockCreateComSessionDeviceCommand();
        DeviceCommand deviceCommand2 = this.mockDeviceCommand();
        DeviceCommand deviceCommand3 = this.mockDeviceCommand();

        // Business method
        command.add(deviceCommand1);
        command.add(deviceCommand2);
        command.add(deviceCommand3);

        // Asserts
        assertThat(command.getChildren()).containsSequence(deviceCommand2, deviceCommand3, deviceCommand1);
    }

    @Test
    public void testMultipleChildrenWitCreateComSessionLast () {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        DeviceCommand deviceCommand1 = this.mockDeviceCommand();
        DeviceCommand deviceCommand2 = this.mockDeviceCommand();
        CreateComSessionDeviceCommand deviceCommand3 = this.mockCreateComSessionDeviceCommand();

        // Business method
        command.add(deviceCommand1);
        command.add(deviceCommand2);
        command.add(deviceCommand3);

        // Asserts
        assertThat(command.getChildren()).containsSequence(deviceCommand1, deviceCommand2, deviceCommand3);
    }

    @Test
    public void testMultipleChildrenWithCreateComSessionInMiddle () {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        DeviceCommand deviceCommand1 = this.mockDeviceCommand();
        CreateComSessionDeviceCommand deviceCommand2 = this.mockCreateComSessionDeviceCommand();
        DeviceCommand deviceCommand3 = this.mockDeviceCommand();

        // Business method
        command.add(deviceCommand1);
        command.add(deviceCommand2);
        command.add(deviceCommand3);

        // Asserts
        assertThat(command.getChildren()).containsSequence(deviceCommand1, deviceCommand3, deviceCommand2);
    }

    @Test
    public void testExecuteForwardsToChildren () throws BusinessException, SQLException {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        DeviceCommand deviceCommand1 = this.mockDeviceCommand();
        DeviceCommand deviceCommand2 = this.mockDeviceCommand();
        DeviceCommand deviceCommand3 = this.mockDeviceCommand();
        command.addAll(deviceCommand1, deviceCommand2, deviceCommand3);
        ComServerDAO comServerDAO = mockComServerDAOButPerformTransactions();

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(deviceCommand1).execute(comServerDAO);
        verify(deviceCommand2).execute(comServerDAO);
        verify(deviceCommand3).execute(comServerDAO);
    }

    @Test
    public void testExecuteForwardsFailureLoggerToChildren () throws BusinessException, SQLException {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        CreateComSessionDeviceCommand deviceCommand1 = this.mockCreateComSessionDeviceCommand();
        DeviceCommand deviceCommand2 = this.mockDeviceCommand();
        DeviceCommand deviceCommand3 = this.mockDeviceCommand();
        command.add(deviceCommand1);
        command.add(deviceCommand2);
        command.add(deviceCommand3);
        ComServerDAO comServerDAO = mockComServerDAOButPerformTransactions();

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(deviceCommand2).logExecutionWith(deviceCommand1);
        verify(deviceCommand3).logExecutionWith(deviceCommand1);
    }

    @Test
    public void testExecuteDoesNotForwardFailureLoggerToChildrenWhereThereIsNone () throws BusinessException, SQLException {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        DeviceCommand deviceCommand1 = this.mockDeviceCommand();
        DeviceCommand deviceCommand2 = this.mockDeviceCommand();
        DeviceCommand deviceCommand3 = this.mockDeviceCommand();
        command.add(deviceCommand1);
        command.add(deviceCommand2);
        command.add(deviceCommand3);
        ComServerDAO comServerDAO = mockComServerDAOButPerformTransactions();

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(deviceCommand1, never()).logExecutionWith(any(DeviceCommand.ExecutionLogger.class));
        verify(deviceCommand2, never()).logExecutionWith(any(DeviceCommand.ExecutionLogger.class));
        verify(deviceCommand3, never()).logExecutionWith(any(DeviceCommand.ExecutionLogger.class));
    }

    @Test
    public void testExecuteDuringShutdownForwardsToChildren () throws BusinessException, SQLException {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        DeviceCommand deviceCommand1 = this.mockDeviceCommand();
        DeviceCommand deviceCommand2 = this.mockDeviceCommand();
        DeviceCommand deviceCommand3 = this.mockDeviceCommand();
        command.addAll(deviceCommand1, deviceCommand2, deviceCommand3);
        ComServerDAO comServerDAO = mockComServerDAOButPerformTransactions();

        // Business method
        command.executeDuringShutdown(comServerDAO);

        // Asserts
        verify(deviceCommand1).executeDuringShutdown(comServerDAO);
        verify(deviceCommand1, never()).execute(comServerDAO);
        verify(deviceCommand2).executeDuringShutdown(comServerDAO);
        verify(deviceCommand2, never()).execute(comServerDAO);
        verify(deviceCommand3).executeDuringShutdown(comServerDAO);
        verify(deviceCommand3, never()).execute(comServerDAO);
    }

    @Test
    public void testExecuteDuringShutdownForwardsFailureLoggerToChildren () throws BusinessException, SQLException {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        CreateComSessionDeviceCommand deviceCommand1 = this.mockCreateComSessionDeviceCommand();
        DeviceCommand deviceCommand2 = this.mockDeviceCommand();
        DeviceCommand deviceCommand3 = this.mockDeviceCommand();
        command.add(deviceCommand1);
        command.add(deviceCommand2);
        command.add(deviceCommand3);
        ComServerDAO comServerDAO = mockComServerDAOButPerformTransactions();

        // Business method
        command.executeDuringShutdown(comServerDAO);

        // Asserts
        verify(deviceCommand2).logExecutionWith(deviceCommand1);
        verify(deviceCommand3).logExecutionWith(deviceCommand1);
    }

    @Test
    public void testExecuteDuringShutdownDoesNotForwardFailureLoggerToChildrenWhereThereIsNone () throws BusinessException, SQLException {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        DeviceCommand deviceCommand1 = this.mockDeviceCommand();
        DeviceCommand deviceCommand2 = this.mockDeviceCommand();
        DeviceCommand deviceCommand3 = this.mockDeviceCommand();
        command.add(deviceCommand1);
        command.add(deviceCommand2);
        command.add(deviceCommand3);
        ComServerDAO comServerDAO = mockComServerDAOButPerformTransactions();

        // Business method
        command.executeDuringShutdown(comServerDAO);

        // Asserts
        verify(deviceCommand1, never()).logExecutionWith(any(DeviceCommand.ExecutionLogger.class));
        verify(deviceCommand2, never()).logExecutionWith(any(DeviceCommand.ExecutionLogger.class));
        verify(deviceCommand3, never()).logExecutionWith(any(DeviceCommand.ExecutionLogger.class));
    }

    @Test(expected = ApplicationException.class)
    public void testChildExecutionFailureIsPropagated () throws BusinessException, SQLException {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        DeviceCommand deviceCommand1 = this.mockDeviceCommand();
        String errorMessage = "ComSessionRootDeviceCommandTest#testChildExecutionFailureIsPropagated";
        ComServerDAO comServerDAO = mockComServerDAOButPerformTransactions();
        ApplicationException toBeThrown = new ApplicationException(errorMessage);
        Mockito.doThrow(toBeThrown).when(deviceCommand1).execute(comServerDAO);
        DeviceCommand deviceCommand2 = this.mockDeviceCommand();
        DeviceCommand deviceCommand3 = this.mockDeviceCommand();
        command.addAll(deviceCommand1, deviceCommand2, deviceCommand3);

        // Business method
        try {
            command.execute(comServerDAO);
        }
        catch (ApplicationException e) {
            assertThat(e.getMessage()).isEqualTo(errorMessage);
            throw e;
        }

        // Expected an ApplicationException from one of the children
    }

    @Test(expected = ApplicationException.class)
    public void testExecutionFailsAsSoonAsPossible () throws BusinessException, SQLException {
        ComServerDAO comServerDAO = mockComServerDAOButPerformTransactions();
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        DeviceCommand deviceCommand1 = this.mockDeviceCommand();
        Mockito.doThrow(ApplicationException.class).when(deviceCommand1).execute(comServerDAO);
        DeviceCommand deviceCommand2 = this.mockDeviceCommand();
        DeviceCommand deviceCommand3 = this.mockDeviceCommand();
        command.addAll(deviceCommand1, deviceCommand2, deviceCommand3);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(deviceCommand1).execute(comServerDAO);
        verify(deviceCommand2, never()).execute(comServerDAO);
        verify(deviceCommand3, never()).execute(comServerDAO);
    }

    @Test
    public void testToStringForEmptyComposite () {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();

        // Business method
        String stringRepresentation = command.toString();

        // Asserts
        assertThat(stringRepresentation).isNotNull();
        assertThat(stringRepresentation).isEmpty();
    }

    @Test
    public void testToStringForOneWellKnownSubCommand () {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        command.add(new ForPrintingPurposesOnly("testToStringForOneWellKnownSubCommand"));

        // Business method
        String stringRepresentation = command.toString();

        // Asserts
        assertThat(stringRepresentation).isNotNull();
        assertThat(stringRepresentation).contains("testToStringForOneWellKnownSubCommand");
    }

    @Test
    public void testToStringRespectsTheOrderOfCommands () {
        CompositeDeviceCommand command = new ComSessionRootDeviceCommand();
        command.add(new ForPrintingPurposesOnly("First"));
        command.add(new ForPrintingPurposesOnly("Second"));
        command.add(new ForPrintingPurposesOnly("Third"));

        // Business method
        String stringRepresentation = command.toString();

        // Asserts
        assertThat(stringRepresentation).isNotNull();
        assertThat(stringRepresentation).contains("First\nSecond\nThird");
    }

    private DeviceCommand mockDeviceCommand () {
        return mock(DeviceCommand.class);
    }

    private CreateComSessionDeviceCommand mockCreateComSessionDeviceCommand () {
        return mock(CreateComSessionDeviceCommand.class);
    }

    private class ForPrintingPurposesOnly implements DeviceCommand {
        private String hardCodedToStringValue;

        private ForPrintingPurposesOnly (String hardCodedToStringValue) {
            super();
            this.hardCodedToStringValue = hardCodedToStringValue;
        }

        @Override
        public void execute (ComServerDAO comServerDAO) {
            // This class is not intended for execution
        }

        @Override
        public void executeDuringShutdown (ComServerDAO comServerDAO) {
            // This class is not intended for execution
        }

        @Override
        public ComServer.LogLevel getJournalingLogLevel () {
            return ComServer.LogLevel.TRACE;
        }

        @Override
        public void logExecutionWith (ExecutionLogger logger) {
            // This class is not intended for execution so nothing to log
        }

        @Override
        public String toJournalMessageDescription(ComServer.LogLevel serverLogLevel) {
            return this.hardCodedToStringValue;
        }
    }

}