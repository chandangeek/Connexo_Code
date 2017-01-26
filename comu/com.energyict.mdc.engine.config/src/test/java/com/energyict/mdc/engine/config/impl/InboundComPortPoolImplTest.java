package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.Expected;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.io.BaudrateValue;
import com.energyict.mdc.io.FlowControl;
import com.energyict.mdc.io.NrOfDataBits;
import com.energyict.mdc.io.NrOfStopBits;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.protocol.api.ComPortType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

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
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCreateWithoutName() {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool(null, ComPortType.TCP, inboundDeviceProtocolPluggableClass, Collections.emptyMap());
        inboundComPortPool.setDescription(DESCRIPTION);
        inboundComPortPool.update();
        // See expected constraint violation rule
    }

    @Test
    @Transactional
    public void testLoad() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();

        // Business method
        ComPortPool loadedComPortPool = getEngineModelService().findComPortPool(comPortPool.getId()).get();

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
        comPortPool.setDescription(comPortPool.getDescription() + "Updated");
        comPortPool.setActive(false);

        // Business method
        comPortPool.update();

        // Asserts
        assertThat(comPortPool.getDescription()).isEqualTo(comPortPool.getDescription());
        assertThat(comPortPool.getName()).isEqualTo(comPortPool.getName());
        assertThat(comPortPool.isActive()).isEqualTo(comPortPool.isActive());
        assertThat(comPortPool.getComPorts()).isEmpty();
        assertThat(comPortPool.isInbound()).as("InboundComPortPools are expected to be inbound").isTrue();
        assertEquals(DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_ID, comPortPool.getDiscoveryProtocolPluggableClass().getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_PORT_POOL+"}", property = "name")
    public void testUpdateWithSameName() {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool("Test for duplication", ComPortType.TCP, inboundDeviceProtocolPluggableClass, Collections.emptyMap());
        inboundComPortPool.setDescription(DESCRIPTION);
        inboundComPortPool.update();
        // Business method
        inboundComPortPool = getEngineModelService().newInboundComPortPool("Test for duplication", ComPortType.TCP, inboundDeviceProtocolPluggableClass, Collections.emptyMap());
        inboundComPortPool.setDescription(DESCRIPTION);
        inboundComPortPool.update();
        // Expecting a DuplicateException
    }

    @Test
    @Transactional
    public void testUpdateWithSameNameAsAnObsoletePool() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        comPortPool.makeObsolete();
        // Business method
        InboundComPortPool notADuplicate = getEngineModelService().newInboundComPortPool(comPortPool.getName(), ComPortType.TCP, inboundDeviceProtocolPluggableClass, Collections.emptyMap());
        notADuplicate.setDescription(DESCRIPTION);
        notADuplicate.update();

        // No ConstraintViolation expected, because a new ComPortPool can have the same name as a deleted one.
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
    public void testDelete() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        long comPortPoolId = comPortPool.getId();

        // Business method
        comPortPool.delete();

        Optional<? extends ComPortPool> shouldNotBePresent = getEngineModelService().findComPortPool(comPortPoolId);

        // Asserts
        assertThat(shouldNotBePresent.isPresent()).isFalse();
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
        onlineComServer.newTCPBasedInboundComPort("port", 1, 8080).description("hello world")
                .active(true).comPortPool(comPortPool)
                .add();

        // Business method
        comPortPool.delete();
        // See expected constraint violation rule
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
//        // See expected constraint violation rule
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

        ComPortPool reloaded = getEngineModelService().findComPortPool(comPortPoolId).get();

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
        onlineComServer.newTCPBasedInboundComPort("port", 1, 8080).description("hello world")
                .active(true).comPortPool(comPortPool)
                .add();

        // Business method
        comPortPool.makeObsolete();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithObsoleteComPorts() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        long comPortPoolId = comPortPool.getId();
        OnlineComServer onlineComServer = createOnlineComServer();
        TCPBasedInboundComPort inboundComPort = onlineComServer.newTCPBasedInboundComPort("port", 1, 8080).description("hello world")
                .active(true).comPortPool(comPortPool)
                .add();
        inboundComPort.makeObsolete();

        // Business method
        comPortPool.makeObsolete();

        Optional<? extends ComPortPool> shouldNotBePresent = getEngineModelService().findComPortPool(comPortPoolId);

        // Asserts
        assertThat(shouldNotBePresent.isPresent()).isTrue();
        assertThat(shouldNotBePresent.get().isObsolete()).isTrue();
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
//        // See expected constraint violation rule
//    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_COMPORTPOOL_NO_UPDATE_ALLOWED+"}", property = "obsoleteDate")
    public void testUpdateAfterMakeObsolete() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        comPortPool.makeObsolete();

        comPortPool.update();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    public void testMakeObsoleteTwice() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        comPortPool.makeObsolete();

        // Business method
        comPortPool.delete();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "comPortType")
    public void testCreateWithoutComPortType() {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool("Unique comPortPool "+comPortPoolIndex++, null, inboundDeviceProtocolPluggableClass, Collections.emptyMap());
        inboundComPortPool.setDescription(DESCRIPTION);
        inboundComPortPool.update();
        // Expecting InvalidValueException because the ComPortType is not set
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL+"}", property = "comPortPool")
    public void testUpdateAddComPortsWithNonMatchingComPortType() {
        InboundComPortPool comPortPool = this.newInboundComPortPoolWithoutViolations();
        OnlineComServer onlineComServer = createOnlineComServer();
        TCPBasedInboundComPort portA = onlineComServer.newTCPBasedInboundComPort("portA",1, 8080).description("hello world")
                .active(true).comPortPool(comPortPool)
                .add();
        TCPBasedInboundComPort portB = onlineComServer.newTCPBasedInboundComPort("portB", 1, 8081).description("hello world")
                .active(true).comPortPool(comPortPool)
                .add();
        ModemBasedInboundComPort portC = onlineComServer.newModemBasedInboundComport("portC", 10, 3, new TimeDuration(60), new TimeDuration(60),
                new SerialPortConfiguration("portC", BaudrateValue.BAUDRATE_115200, NrOfDataBits.EIGHT, NrOfStopBits.ONE, Parities.EVEN, FlowControl.RTSCTS))
                .description(DESCRIPTION)
                .active(true)
                .comPortPool(comPortPool)
                .atCommandTry(BigDecimal.ONE)
                .delayAfterConnect(new TimeDuration(60))
                .delayBeforeSend(new TimeDuration(60))
                .addressSelector("?")
                .add();


        // See expected constraint violation rule
    }

    private int comPortPoolIndex=1;
    private InboundComPortPool newInboundComPortPoolWithoutViolations() {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool("Unique comPortPool "+comPortPoolIndex++, ComPortType.TCP, inboundDeviceProtocolPluggableClass, Collections
                .emptyMap());
        inboundComPortPool.setDescription(DESCRIPTION);
        inboundComPortPool.update();
        return inboundComPortPool;
    }

    int onlineNameNumber=1;
    private OnlineComServer createOnlineComServer() {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();
        String name = "Online-" + onlineNameNumber++;
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.active(true);
        onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.ERROR);
        onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.TRACE);
        onlineComServerBuilder.changesInterPollDelay(new TimeDuration(60));
        onlineComServerBuilder.schedulingInterPollDelay(new TimeDuration(90));
        onlineComServerBuilder.storeTaskQueueSize(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.numberOfStoreTaskThreads(1);
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        return onlineComServerBuilder.create();
    }

}