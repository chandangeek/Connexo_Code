package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.Expected;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.channels.serial.BaudrateValue;
import com.energyict.mdc.protocol.api.channels.serial.FlowControl;
import com.energyict.mdc.protocol.api.channels.serial.NrOfDataBits;
import com.energyict.mdc.protocol.api.channels.serial.NrOfStopBits;
import com.energyict.mdc.protocol.api.channels.serial.Parities;
import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;
import java.math.BigDecimal;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the InboundComPortPoolImpl component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (15:59)
 */
public class InboundComPortPoolImplTest extends PersistenceTest {

    protected static final String DESCRIPTION = "Description";

    @Test
    @Transactional
    public void testCreateWithoutViolations() {
        // Business method
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();

        // Asserts
        assertThat(comPortPool.isObsolete()).isFalse();
        assertThat(comPortPool.getObsoleteDate()).isNull();
        assertThat(comPortPool.isInbound()).as("Was expecting an InboundComPortPool to be inbound").isTrue();
        assertThat(comPortPool.getComPorts()).isEmpty();
        assertThat(comPortPool.getDiscoveryProtocolPluggableClass().getId()).isEqualTo(DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_ID);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCreateWithoutName() {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setDescription(DESCRIPTION);
        inboundComPortPool.setComPortType(ComPortType.TCP);
        inboundComPortPool.setDiscoveryProtocolPluggableClass(inboundDeviceProtocolPluggableClass);
        inboundComPortPool.save();
        // Expecting BusinessException because the name is not set
    }

    @Test
    @Transactional
    public void testLoad() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();

        // Business method
        ComPortPool loadedComPortPool = getEngineModelService().findComPortPool(comPortPool.getId());

        // Asserts
        InboundComPortPool loaded = (InboundComPortPool) loadedComPortPool;
        assertThat(comPortPool.getName()).isEqualTo(loaded.getName());
        assertThat(comPortPool.isActive()).isEqualTo(loaded.isActive());
        assertThat(comPortPool.getDescription()).isEqualTo(loaded.getDescription());
        assertEquals(DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_ID, comPortPool.getDiscoveryProtocolPluggableClass().getId());
    }

    @Test
    @Transactional
    public void testUpdateWithoutViolations() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();

        comPortPool.setName(comPortPool.getName() + "Updated");
        comPortPool.setDescription(comPortPool.getDescription() + "Updated");
        comPortPool.setActive(false);

        // Business method
        comPortPool.save();

        // Asserts
        assertThat(comPortPool.getName()).isEqualTo(comPortPool.getName());
        assertThat(comPortPool.isActive()).isEqualTo(comPortPool.isActive());
        assertThat(comPortPool.getDescription()).isEqualTo(comPortPool.getDescription());
        assertThat(comPortPool.getComPorts()).isEmpty();
        assertThat(comPortPool.isInbound()).as("InboundComPortPools are expected to be inbound").isTrue();
        assertEquals(DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_ID, comPortPool.getDiscoveryProtocolPluggableClass().getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_DUPLICATE_COM_PORT_POOL+"}", property = "name")
    public void testUpdateWithSameName() {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setName("Test for duplication");
        inboundComPortPool.setDescription(DESCRIPTION);
        inboundComPortPool.setComPortType(ComPortType.TCP);
        inboundComPortPool.setDiscoveryProtocolPluggableClass(inboundDeviceProtocolPluggableClass);
        inboundComPortPool.save();
        // Business method
        inboundComPortPool = getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setName("Test for duplication");
        inboundComPortPool.setDescription(DESCRIPTION);
        inboundComPortPool.setComPortType(ComPortType.TCP);
        inboundComPortPool.setDiscoveryProtocolPluggableClass(inboundDeviceProtocolPluggableClass);
        inboundComPortPool.save();
        // Expecting a DuplicateException
    }

    @Test
    @Transactional
    public void testUpdateWithSameNameAsAnObsoletePool() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        comPortPool.makeObsolete();
        // Business method
        InboundComPortPool notADuplicate = getEngineModelService().newInboundComPortPool();
        notADuplicate.setName(comPortPool.getName());
        notADuplicate.setDescription(DESCRIPTION);
        notADuplicate.setComPortType(ComPortType.TCP);
        notADuplicate.setDiscoveryProtocolPluggableClass(inboundDeviceProtocolPluggableClass);
        notADuplicate.save();

        // No BusinessException expected, because a new ComPortPool can have the same name as a deleted one.
        // Asserts
        assertThat(notADuplicate.getName()).isEqualTo(comPortPool.getName());
        assertThat(notADuplicate.isObsolete()).isFalse();
        assertThat(notADuplicate.getObsoleteDate()).isNull();
        assertThat(notADuplicate.isInbound()).as("Was expecting an InboundComPortPool to be inbound").isTrue();
        assertThat(notADuplicate.getComPorts()).isEmpty();
        assertEquals(DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_ID, comPortPool.getDiscoveryProtocolPluggableClass().getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testUpdateWithoutName() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();

        comPortPool.setName(null);

        // Business method
        comPortPool.save();
        // Expecting BusinessException because the name is not set
    }

    @Test
    @Transactional
    public void testDelete() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        long comPortPoolId = comPortPool.getId();

        // Business method
        comPortPool.delete();

        ComPortPool shouldBeNull = getEngineModelService().findComPortPool(comPortPoolId);

        // Asserts
        assertThat(shouldBeNull).isNull();
    }

//    @Test
//    @Transactional
//    public void testDeleteWithComSession() {
//        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
//        List<ComSession> comSessions = new ArrayList<>();
//        ComSession comSession = mock(ComSession.class);
//        comSessions.add(comSession);
//        when(this.getComSessionFactory().findByPool(comPortPool)).thenReturn(comSessions);
//
//        // Business method
//        comPortPool.delete();
//
//        // Asserts
//        verify(comSession).delete();
//    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "inboundComPortPoolXStillInUseByComPortsY")
    public void testDeleteWithComPorts() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        OnlineComServer onlineComServer = createOnlineComServer();
        onlineComServer.newTCPBasedInboundComPort()
                .name("port").numberOfSimultaneousConnections(1).portNumber(8080).description("hello world")
                .active(true).comPortPool(comPortPool)
                .add();

        // Business method
        comPortPool.delete();
        // Expected BusinessException because a ComPortPool cannot be made obsolete if ComPorts are still using it
    }

//    @Test
//    @Transactional
//    @Expected(expected = BusinessException.class, messageId = "inboundComPortPoolXStillInUseByConnectionTasksY")
//    public void testDeleteWithConnectionTasks () {
//        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
//        List<InboundConnectionTask> inboundConnectionTasks = new ArrayList<>();
//        inboundConnectionTasks.add(mock(InboundConnectionTask.class));
//        when(this.getConnectionTaskFactory().findInboundUsingComPortPool(comPortPool)).thenReturn(inboundConnectionTasks);
//
//        // Business method
//        comPortPool.delete();
//
//        // Expected BusinessException because a ComPortPool cannot be made obsolete if ComPorts are still using it
//    }

    @Test
    @Transactional
    public void testMakeObsolete() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();

        // Business method
        comPortPool.makeObsolete();

        // Asserts
        assertThat(comPortPool.isObsolete()).isTrue();
        assertThat(comPortPool.getObsoleteDate()).isNotNull();
    }

    @Test
    @Transactional
    public void testIsObsoleteAfterReload() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        long comPortPoolId = comPortPool.getId();

        // Business method
        comPortPool.makeObsolete();

        ComPortPool reloaded = getEngineModelService().findComPortPool(comPortPoolId);

        // Asserts
        assertThat(reloaded.isObsolete()).isTrue();
        assertThat(reloaded.getObsoleteDate()).isNotNull();
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "inboundComPortPoolXStillInUseByComPortsY")
    public void testMakeObsoleteWithComPorts() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        OnlineComServer onlineComServer = createOnlineComServer();
        onlineComServer.newTCPBasedInboundComPort()
                .name("port").numberOfSimultaneousConnections(1).portNumber(8080).description("hello world")
                .active(true).comPortPool(comPortPool)
                .add();

        // Business method
        comPortPool.makeObsolete();

        // Expected BusinessException because a ComPortPool cannot be made obsolete if ComPorts are still using it
    }

//    @Test(expected = BusinessException.class)
//    public void testMakeObsoleteWithConnectionTasks () {
//        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
//        List<InboundConnectionTask> connectionTasks = new ArrayList<>();
//        connectionTasks.add(mock(InboundConnectionTask.class));
//        when(this.getConnectionTaskFactory().findInboundUsingComPortPool(comPortPool)).thenReturn(connectionTasks);
//
//        // Business method
//        try {
//            comPortPool.makeObsolete();
//        }
//        catch (BusinessException e) {
//            assertThat(e.getMessageId()).isEqualTo("inboundComPortPoolXStillInUseByConnectionTasksY");
//            throw e;
//        }
//
//        // Expected BusinessException because a ComPortPool cannot be made obsolete if ComPorts are still using it
//    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_COMPORTPOOL_NO_UPDATE_ALLOWED+"}", property = "obsoleteDate")
    public void testUpdateAfterMakeObsolete() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        comPortPool.makeObsolete();

        comPortPool.save();

        // Expected a BusinessException because an obsolete ComPortPool cannot be updated any longer
    }

    @Test
    @Transactional
    public void testMakeObsoleteTwice() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        comPortPool.makeObsolete();

        // Business method
        comPortPool.delete();

        // Expected a BusinessException because a ComPortPool cannot be made obsolete twice
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}", property = "comPortType")
    public void testCreateWithoutComPortType() {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setName("Unique comPortPool "+comPortPoolIndex++);
        inboundComPortPool.setDescription(DESCRIPTION);
        inboundComPortPool.setComPortType(null);
        inboundComPortPool.setDiscoveryProtocolPluggableClass(inboundDeviceProtocolPluggableClass);
        inboundComPortPool.save();
        // Expecting InvalidValueException because the ComPortType is not set
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}", property = "comPortType")
    public void updateWithoutComPortType() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();

        comPortPool.setComPortType(null);

        comPortPool.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL+"}", property = "comPortPool")
    public void testUpdateAddComPortsWithNonMatchingComPortType() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        OnlineComServer onlineComServer = createOnlineComServer();
        TCPBasedInboundComPort portA = onlineComServer.newTCPBasedInboundComPort()
                .name("portA").numberOfSimultaneousConnections(1).portNumber(8080).description("hello world")
                .active(true).comPortPool(comPortPool)
                .add();
        TCPBasedInboundComPort portB = onlineComServer.newTCPBasedInboundComPort()
                .name("portB").numberOfSimultaneousConnections(1).portNumber(8081).description("hello world")
                .active(true).comPortPool(comPortPool)
                .add();
        ModemBasedInboundComPort portC = onlineComServer.newModemBasedInboundComport()
                .name("portC")
                .description(DESCRIPTION)
                .active(true)
                .ringCount(10)
                .maximumDialErrors(3)
                .comPortPool(comPortPool)
                .connectTimeout(new TimeDuration(60))
                .atCommandTimeout(new TimeDuration(60))
                .atCommandTry(BigDecimal.ONE)
                .delayAfterConnect(new TimeDuration(60))
                .delayBeforeSend(new TimeDuration(60))
                .addressSelector("?")
                .serialPortConfiguration(new SerialPortConfiguration("portC", BaudrateValue.BAUDRATE_115200, NrOfDataBits.EIGHT, NrOfStopBits.ONE, Parities.EVEN, FlowControl.RTSCTS))
                .add();


        // Expecting BusinessException because one of the ComPorts is not of type TCP
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}", property = "discoveryProtocolPluggableClassId")
    public void updateWithoutDiscoveryProtocol() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();

        // Business method
        comPortPool.setDiscoveryProtocolPluggableClass(null);

        comPortPool.save();
        // was expecting a InvalidValueException because the discovery protocol is null
    }

//    @Test
//    @Transactional
//    @Expected(expected = TranslatableApplicationException.class)
//    public void updateWithNonExistingDiscoveryProtocol() {
//        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
//
//        // Business method
//        comPortPool.setDiscoveryProtocolPluggableClassId(NON_EXISTING_DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_ID);
//
//        comPortPool.save();
//
//        // was expecting a InvalidReferenceException because the discovery protocol does not exist
//    }

    private int comPortPoolIndex=1;
    private InboundComPortPool newInboundComPortPoolWithoutViolations() {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setName("Unique comPortPool "+comPortPoolIndex++);
        inboundComPortPool.setDescription(DESCRIPTION);
        inboundComPortPool.setComPortType(ComPortType.TCP);
        inboundComPortPool.setDiscoveryProtocolPluggableClass(inboundDeviceProtocolPluggableClass);
        inboundComPortPool.save();
        return inboundComPortPool;
    }

    int onlineNameNumber=1;
    private OnlineComServer createOnlineComServer() {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
        String name = "Online-" + onlineNameNumber++;
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
        onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.TRACE);
        onlineComServer.setChangesInterPollDelay(new TimeDuration(60));
        onlineComServer.setSchedulingInterPollDelay(new TimeDuration(90));
        onlineComServer.setStoreTaskQueueSize(1);
        onlineComServer.setStoreTaskThreadPriority(1);
        onlineComServer.setNumberOfStoreTaskThreads(1);
        onlineComServer.save();
        return onlineComServer;
    }

}