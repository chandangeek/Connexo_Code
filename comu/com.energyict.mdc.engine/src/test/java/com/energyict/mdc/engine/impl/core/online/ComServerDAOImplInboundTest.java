package com.energyict.mdc.engine.impl.core.online;

import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.MdwInterface;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.communication.tasks.ServerComTaskExecutionFactory;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.messages.EndDeviceMessage;
import com.energyict.mdc.messages.EndDeviceMessageFactory;
import com.energyict.mdc.engine.impl.meterdata.identifiers.DeviceMessageIdentifierById;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.engine.impl.core.inbound.InboundDAO;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdw.core.CommunicationDevice;
import com.energyict.test.MockEnvironmentTranslations;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the methods of the {@link com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl}
 * that relate to the {@link InboundDAO} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-17 (10:32)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComServerDAOImplInboundTest {

    @ClassRule
    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

    @Mock
    private Device device;
    @Mock
    private DeviceIdentifier deviceIdentifier;
    @Mock
    private ServerManager manager;
    @Mock
    private MdwInterface mdwInterface;
    @Mock
    private EndDeviceMessageFactory deviceMessageFactory;
    @Mock
    private ServerComTaskExecutionFactory comTaskExecutionFactory;

    private ComServerDAO comServerDAO = new ComServerDAOImpl();

    @Before
    public void initializeMocksAndFactories () throws SQLException, BusinessException {
        this.mockMdwInterfaceTransactionExecutor();
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        when(this.manager.getMdwInterface()).thenReturn(this.mdwInterface);
        when(this.manager.getDeviceMessageFactory()).thenReturn(this.deviceMessageFactory);
        when(this.manager.getComTaskExecutionFactory()).thenReturn(this.comTaskExecutionFactory);
        ManagerFactory.setCurrent(this.manager);
    }

    /**
     * Tests the situation where the Device does not exist.
     */
    @Test(expected = DataAccessException.class)
    public void testConfirmSentMessagesAndGetPendingForNonExistingDevice () {
        doThrow(NotFoundException.class).when(this.deviceIdentifier).findDevice();
        comServerDAO.confirmSentMessagesAndGetPending(this.deviceIdentifier, 10);
    }

    /**
     * Tests the situation where the Device does not have any
     * sent or pending messages.
     */
    @Test
    public void testConfirmSentMessagesAndGetPendingWithNoMessages () {
        // Business method
        List<OfflineDeviceMessage> pendingMessages = comServerDAO.confirmSentMessagesAndGetPending(this.deviceIdentifier, 10);

        // Asserts
        verify(this.device).getMessagesByState(DeviceMessageStatus.SENT);
        verify(this.device).getMessagesByState(DeviceMessageStatus.PENDING);
        assertThat(pendingMessages).isNotNull();
        assertThat(pendingMessages).isEmpty();
    }

    /**
     * Tests the situation where the Device has only pending messages but no sent.
     */
    @Test
    public void testConfirmSentMessagesAndGetPendingWithOnlySentMessages () throws BusinessException, SQLException {
        DeviceMessage message1 = mock(DeviceMessage.class, withSettings().extraInterfaces(EndDeviceMessage.class));
        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        when(message1.goOffline()).thenReturn(offlineDeviceMessage1);
        DeviceMessage message2 = mock(DeviceMessage.class, withSettings().extraInterfaces(EndDeviceMessage.class));
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        when(message2.goOffline()).thenReturn(offlineDeviceMessage2);
        when(this.device.getMessagesByState(DeviceMessageStatus.PENDING)).thenReturn(Arrays.<DeviceMessage>asList(message1, message2));

        // Business method
        List<OfflineDeviceMessage> pendingMessages = comServerDAO.confirmSentMessagesAndGetPending(this.deviceIdentifier, 10);

        // Asserts
        verify(this.device).getMessagesByState(DeviceMessageStatus.SENT);
        verify(this.device).getMessagesByState(DeviceMessageStatus.PENDING);
        assertThat(pendingMessages).containsOnly(offlineDeviceMessage1, offlineDeviceMessage2);
        this.verifyOnlySetSentCalled((EndDeviceMessage) message1);
        this.verifyOnlySetSentCalled((EndDeviceMessage) message2);
    }

    private void verifyOnlySetSentCalled (EndDeviceMessage message) throws BusinessException, SQLException {
        verify(message).moveTo(DeviceMessageStatus.SENT);
        verify(message, never()).moveTo(DeviceMessageStatus.CONFIRMED);
        verify(message, never()).moveTo(DeviceMessageStatus.FAILED);
        verify(message, never()).moveTo(DeviceMessageStatus.INDOUBT);
        verify(message, never()).moveTo(DeviceMessageStatus.PENDING);
    }

    /**
     * Tests the situation where the Device has only exactly the expected number of sent messages.
     */
    @Test
    public void testConfirmSentMessagesAndGetPendingWithMatchingSentMessages () throws BusinessException, SQLException {
        DeviceMessage message1 = mock(DeviceMessage.class, withSettings().extraInterfaces(EndDeviceMessage.class));
        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        when(message1.goOffline()).thenReturn(offlineDeviceMessage1);
        DeviceMessage message2 = mock(DeviceMessage.class, withSettings().extraInterfaces(EndDeviceMessage.class));
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        when(message2.goOffline()).thenReturn(offlineDeviceMessage2);
        when(this.device.getMessagesByState(DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(message1, message2));

        // Business method
        comServerDAO.confirmSentMessagesAndGetPending(this.deviceIdentifier, 2);

        // Asserts
        verify(this.device).getMessagesByState(DeviceMessageStatus.SENT);
        verify(this.device).getMessagesByState(DeviceMessageStatus.PENDING);
        this.verifyOnlyConfirmed((EndDeviceMessage) message1);
        this.verifyOnlyConfirmed((EndDeviceMessage) message2);
    }

    private void verifyOnlyConfirmed (EndDeviceMessage message) throws BusinessException, SQLException {
        verify(message).moveTo(DeviceMessageStatus.CONFIRMED);
        verify(message, never()).moveTo(DeviceMessageStatus.SENT);
        verify(message, never()).moveTo(DeviceMessageStatus.FAILED);
        verify(message, never()).moveTo(DeviceMessageStatus.INDOUBT);
        verify(message, never()).moveTo(DeviceMessageStatus.PENDING);
    }

    /**
     * Tests the situation where the Device the expected number > actual message count.
     */
    @Test
    public void testConfirmSentMessagesAndGetPendingWithInsufficientSentMessages () throws BusinessException, SQLException {
        DeviceMessage message1 = mock(DeviceMessage.class, withSettings().extraInterfaces(EndDeviceMessage.class));
        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        when(message1.goOffline()).thenReturn(offlineDeviceMessage1);
        DeviceMessage message2 = mock(DeviceMessage.class, withSettings().extraInterfaces(EndDeviceMessage.class));
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        when(message2.goOffline()).thenReturn(offlineDeviceMessage2);
        when(this.device.getMessagesByState(DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(message1, message2));

        // Business method
        comServerDAO.confirmSentMessagesAndGetPending(this.deviceIdentifier, 1);

        // Asserts
        verify(this.device).getMessagesByState(DeviceMessageStatus.SENT);
        verify(this.device).getMessagesByState(DeviceMessageStatus.PENDING);
        this.verifyOnlyInDoubt((EndDeviceMessage) message1);
        this.verifyOnlyInDoubt((EndDeviceMessage) message2);
    }

    private void verifyOnlyInDoubt(EndDeviceMessage message) throws BusinessException, SQLException {
        verify(message).moveTo(DeviceMessageStatus.INDOUBT);
        verify(message, never()).moveTo(DeviceMessageStatus.SENT);
        verify(message, never()).moveTo(DeviceMessageStatus.FAILED);
        verify(message, never()).moveTo(DeviceMessageStatus.CONFIRMED);
        verify(message, never()).moveTo(DeviceMessageStatus.PENDING);
    }

    /**
     * Tests the situation where the Device the expected number < actual message count.
     */
    @Test
    public void testConfirmSentMessagesAndGetPendingWithToManyConfirmedMessages () throws BusinessException, SQLException {
        DeviceMessage message1 = mock(DeviceMessage.class, withSettings().extraInterfaces(EndDeviceMessage.class));
        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        when(message1.goOffline()).thenReturn(offlineDeviceMessage1);
        DeviceMessage message2 = mock(DeviceMessage.class, withSettings().extraInterfaces(EndDeviceMessage.class));
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        when(message2.goOffline()).thenReturn(offlineDeviceMessage2);
        when(this.device.getMessagesByState(DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(message1, message2));

        // Business method
        comServerDAO.confirmSentMessagesAndGetPending(this.deviceIdentifier, 10);

        // Asserts
        verify(this.device).getMessagesByState(DeviceMessageStatus.SENT);
        verify(this.device).getMessagesByState(DeviceMessageStatus.PENDING);
        this.verifyOnlyInDoubt((EndDeviceMessage) message1);
        this.verifyOnlyInDoubt((EndDeviceMessage) message2);
    }

    /**
     * Tests the situation where the Device the actual message count is zero
     * but there are effectively sent messages waiting to be confirmed.
     */
    @Test
    public void testConfirmSentMessagesAndGetPendingWithZeroSentMessages () throws BusinessException, SQLException {
        DeviceMessage message1 = mock(DeviceMessage.class, withSettings().extraInterfaces(EndDeviceMessage.class));
        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        when(message1.goOffline()).thenReturn(offlineDeviceMessage1);
        DeviceMessage message2 = mock(DeviceMessage.class, withSettings().extraInterfaces(EndDeviceMessage.class));
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        when(message2.goOffline()).thenReturn(offlineDeviceMessage2);
        when(this.device.getMessagesByState(DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(message1, message2));

        // Business method
        comServerDAO.confirmSentMessagesAndGetPending(this.deviceIdentifier, 0);

        // Asserts
        verify(this.device).getMessagesByState(DeviceMessageStatus.SENT);
        verify(this.device).getMessagesByState(DeviceMessageStatus.PENDING);
        this.verifyOnlyFailed((EndDeviceMessage) message1);
        this.verifyOnlyFailed((EndDeviceMessage) message2);
    }

    private void verifyOnlyFailed(EndDeviceMessage message) throws BusinessException, SQLException {
        verify(message).moveTo(DeviceMessageStatus.FAILED);
        verify(message, never()).moveTo(DeviceMessageStatus.SENT);
        verify(message, never()).moveTo(DeviceMessageStatus.INDOUBT);
        verify(message, never()).moveTo(DeviceMessageStatus.CONFIRMED);
        verify(message, never()).moveTo(DeviceMessageStatus.PENDING);
    }

    @Test
    public void updateDeviceMessageInformationTest(){
        final int dmId1 = 132;
        final int dmId2 = 6514;
        String protocolMessage = "Someone told me make sure this message failed, that is all I know.";
        EndDeviceMessage deviceMessage1 = mock(EndDeviceMessage.class);
        when(deviceMessage1.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        EndDeviceMessage deviceMessage2 = mock(EndDeviceMessage.class);
        when(deviceMessage2.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(deviceMessageFactory.find(dmId1)).thenReturn(deviceMessage1);
        when(deviceMessageFactory.find(dmId2)).thenReturn(deviceMessage2);

        // business method
        comServerDAO.updateDeviceMessageInformation(new DeviceMessageIdentifierById(dmId1), DeviceMessageStatus.CONFIRMED, null);
        comServerDAO.updateDeviceMessageInformation(new DeviceMessageIdentifierById(dmId2), DeviceMessageStatus.FAILED, protocolMessage);

        // asserts
        verify(deviceMessage1).moveTo(DeviceMessageStatus.CONFIRMED);
        verify(deviceMessage1).updateProtocolInfo(null);
        verify(deviceMessage2).moveTo(DeviceMessageStatus.FAILED);
        verify(deviceMessage2).updateProtocolInfo(protocolMessage);
    }

    @Test(expected = NotFoundException.class)
    public void testGetDeviceProtocolSecurityPropertiesWhenDeviceDoesNotExist () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doThrow(NotFoundException.class).when(deviceIdentifier).findDevice();

        // Business method
        this.comServerDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, mock(InboundComPort.class));

        // Asserts: expect a NotFoundException do be thrown
    }

    @Test
    public void testGetDeviceProtocolSecurityPropertiesWithoutConnectionTasks () {
        CommunicationDevice device = mock(CommunicationDevice.class);
        when(device.getInboundConnectionTasks()).thenReturn(new ArrayList<InboundConnectionTask>(0));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(mock(InboundComPortPool.class));

        // Business method
        List<SecurityProperty> securityProperties = this.comServerDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, comPort);

        // Asserts
        assertThat(securityProperties).isNull();
    }

    @Test
    public void testGetDeviceProtocolSecurityPropertiesWithoutMatchingConnectionTask () {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        InboundComPortPool otherComPortPool = mock(InboundComPortPool.class);
        CommunicationDevice device = mock(CommunicationDevice.class);
        when(device.getInboundConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(otherComPortPool);

        // Business method
        List<SecurityProperty> securityProperties = this.comServerDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, comPort);

        // Asserts
        assertThat(securityProperties).isNull();
    }

    @Test
    public void testGetDeviceProtocolSecurityPropertiesWithoutComTasks () {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        when(this.comTaskExecutionFactory.findByConnectionTask(connectionTask)).thenReturn(new ArrayList<ComTaskExecution>(0));
        CommunicationDevice device = mock(CommunicationDevice.class);
        when(device.getInboundConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);

        // Business method
        List<SecurityProperty> securityProperties = this.comServerDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, comPort);

        // Asserts
        assertThat(securityProperties).isNull();
    }

    @Test
    public void testGetDeviceProtocolSecurityProperties () {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(this.comTaskExecutionFactory.findByConnectionTask(connectionTask)).thenReturn(Arrays.asList(comTaskExecution));
        DeviceCommunicationConfiguration deviceCommunicationConfiguration = mock(DeviceCommunicationConfiguration.class);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(deviceCommunicationConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getCommunicationConfiguration()).thenReturn(deviceCommunicationConfiguration);
        SecurityProperty expectedSecurityProperty = mock(SecurityProperty.class);
        List<SecurityProperty> expectedSecurityProperties = Arrays.asList(expectedSecurityProperty);
        CommunicationDevice device = mock(CommunicationDevice.class);
        when(device.getInboundConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getProtocolSecurityProperties(securityPropertySet)).thenReturn(expectedSecurityProperties);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);

        // Business method
        this.comServerDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, comPort);

        // Asserts
        verify(device).getProtocolSecurityProperties(securityPropertySet);
    }

    @Test(expected = NotFoundException.class)
    public void testGetDeviceConnectionTypePropertiesWhenDeviceDoesNotExist () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doThrow(NotFoundException.class).when(deviceIdentifier).findDevice();

        // Business method
        this.comServerDAO.getDeviceConnectionTypeProperties(deviceIdentifier, mock(InboundComPort.class));

        // Asserts: expect a NotFoundException do be thrown
    }

    @Test
    public void testGetDeviceConnectionTypePropertiesWithoutConnectionTasks () {
        CommunicationDevice device = mock(CommunicationDevice.class);
        when(device.getInboundConnectionTasks()).thenReturn(new ArrayList<InboundConnectionTask>(0));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(mock(InboundComPortPool.class));

        // Business method
        TypedProperties connectionTypeProperties = this.comServerDAO.getDeviceConnectionTypeProperties(deviceIdentifier, comPort);

        // Asserts
        assertThat(connectionTypeProperties).isNull();
    }

    @Test
    public void testGetDeviceConnectionTypePropertiesWithoutMatchingConnectionTask () {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        InboundComPortPool otherComPortPool = mock(InboundComPortPool.class);
        CommunicationDevice device = mock(CommunicationDevice.class);
        when(device.getInboundConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(otherComPortPool);

        // Business method
        TypedProperties connectionTypeProperties = this.comServerDAO.getDeviceConnectionTypeProperties(deviceIdentifier, comPort);

        // Asserts
        assertThat(connectionTypeProperties).isNull();
    }

    @Test
    public void testGetDeviceConnectionTypeProperties () {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        when(this.comTaskExecutionFactory.findByConnectionTask(connectionTask)).thenReturn(new ArrayList<ComTaskExecution>(0));
        CommunicationDevice device = mock(CommunicationDevice.class);
        when(device.getInboundConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);

        // Business method
        this.comServerDAO.getDeviceConnectionTypeProperties(deviceIdentifier, comPort);

        // Asserts
        verify(connectionTask).getTypedProperties();
    }

    @Test
    public void testGetDeviceProtocolPropertiesForDeviceIdentifierThatReturnsNullWhenDeviceDoesNotExist () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(null);

        // Business method
        TypedProperties protocolProperties = this.comServerDAO.getDeviceProtocolProperties(deviceIdentifier);

        // Asserts
        assertThat(protocolProperties).isNull();
    }

    @Test
    public void testGetDeviceProtocolPropertiesForDeviceIdentifierThatThrowsNotFoundExceptionWhenDeviceDoesNotExist () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doThrow(NotFoundException.class).when(deviceIdentifier).findDevice();

        // Business method
        TypedProperties protocolProperties = this.comServerDAO.getDeviceProtocolProperties(deviceIdentifier);

        // Asserts
        assertThat(protocolProperties).isNull();
    }

    @Test
    public void testGetDeviceProtocolProperties () {
        CommunicationDevice device = mock(CommunicationDevice.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);

        // Business method
        this.comServerDAO.getDeviceProtocolProperties(deviceIdentifier);

        // Asserts
        verify(device).getDeviceProtocolProperties();
    }

    private void mockMdwInterfaceTransactionExecutor () throws BusinessException, SQLException {
        when(this.mdwInterface.execute(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer (InvocationOnMock invocation) throws Throwable {
                Transaction transaction = (Transaction) invocation.getArguments()[0];
                return transaction.doExecute();
            }
        });
        when(this.mdwInterface.isInTransaction()).thenReturn(true);
    }

}