package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.channels.serial.BaudrateValue;
import com.energyict.mdc.protocol.api.channels.serial.FlowControl;
import com.energyict.mdc.protocol.api.channels.serial.NrOfDataBits;
import com.energyict.mdc.protocol.api.channels.serial.NrOfStopBits;
import com.energyict.mdc.protocol.api.channels.serial.Parities;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
* Tests the integration between the {@link ComServerImpl} and
* the ComPortImpl components.
* Since the InboundOutboundComServerImpl is an abstract class,
* it actually uses the {@link OnlineComServerImpl} class but that
* was just a random choice, there is no reason why it could not
* have been another member of the class hierarchy.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-04-19 (10:00)
*/
@RunWith(MockitoJUnitRunner.class)
public class ComServerComPortTest extends PersistenceTest {

    private static final String QUERY_API_POST_URL = "http://comserver.energyict.com/queryAPI";
    private static final String EVENT_REGISTRATION_URL = "http://comserver.energyict.com/events/registration";

    private static final ComServer.LogLevel SERVER_LOG_LEVEL = ComServer.LogLevel.ERROR;
    private static final ComServer.LogLevel COMMUNICATION_LOG_LEVEL = ComServer.LogLevel.TRACE;
    private static final TimeDuration CHANGES_INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.HOURS);
    private static final TimeDuration SCHEDULING_INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.MINUTES);

    private InboundComPortPool tcpBasedInboundComPortPool;
    private InboundComPortPool udpBasedInboundComPortPool;
    private InboundComPortPool serialBasedInboundComPortPool;

    @Before
    public void setUp () {
        super.setUp();
        tcpBasedInboundComPortPool = newInboundComPortPool(ComPortType.TCP);
        udpBasedInboundComPortPool = newInboundComPortPool(ComPortType.UDP);
        serialBasedInboundComPortPool = newInboundComPortPool(ComPortType.SERIAL);
    }

    @Test
    @Transactional
    public void testCreateWithComPortsWithoutViolations () {
        OnlineComServer comServer = createOnlineComServer();

        int numberOfComPorts = 3;
        addComPorts(comServer, numberOfComPorts);
        // Asserts
        assertThat(comServer.isActive()).as("Was expecting the new com server to be active").isTrue();
        assertThat(comServer.getServerLogLevel()).isEqualTo(SERVER_LOG_LEVEL);
        assertThat(comServer.getCommunicationLogLevel()).isEqualTo(COMMUNICATION_LOG_LEVEL);
        assertThat(comServer.getChangesInterPollDelay()).isEqualTo(CHANGES_INTER_POLL_DELAY);
        assertThat(comServer.getSchedulingInterPollDelay()).isEqualTo(SCHEDULING_INTER_POLL_DELAY);
        assertThat(comServer.getOutboundComPorts().size()).isEqualTo(numberOfComPorts);
    }

    @Test
    @Transactional
    public void loadWithComPortsTest() {
        OnlineComServer shadow = createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(shadow, numberOfComPorts);

        // Business method
        OnlineComServer loadedOnlineServer = (OnlineComServer) getEngineModelService().findComServer(shadow.getId()).get();

        // Asserts
        assertThat(loadedOnlineServer.isActive()).as("Was expecting the new com server to be active").isTrue();
        assertThat(loadedOnlineServer.getServerLogLevel()).isEqualTo(SERVER_LOG_LEVEL);
        assertThat(loadedOnlineServer.getCommunicationLogLevel()).isEqualTo(COMMUNICATION_LOG_LEVEL);
        assertThat(loadedOnlineServer.getChangesInterPollDelay()).isEqualTo(CHANGES_INTER_POLL_DELAY);
        assertThat(loadedOnlineServer.getSchedulingInterPollDelay()).isEqualTo(SCHEDULING_INTER_POLL_DELAY);
        assertThat(loadedOnlineServer.getOutboundComPorts().size()).isEqualTo(numberOfComPorts);
    }

    @Test
    @Transactional
    public void testUpdateAddComPortsViaShadow () {
        OnlineComServer onlineComServer = createOnlineComServer();

        onlineComServer.setActive(false);

        int numberOfComPorts = 3;
        this.addComPorts(onlineComServer, numberOfComPorts);

        onlineComServer.save();

        // Asserts
        assertThat(onlineComServer.getOutboundComPorts().size()).isEqualTo(numberOfComPorts);

        // Reload to make sure to work with an empty ComPort cache.
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(onlineComServer.getId()).get();
        assertThat(reloaded.getOutboundComPorts().size()).isEqualTo(numberOfComPorts);
    }

    @Test
    @Transactional
    public void testUpdateAddOutboundComPorts () {
        OnlineComServer onlineComServer = createOnlineComServer();

        // Business method
        onlineComServer.newOutboundComPort("Outbound-" + uniqueComPortId++, 1)
                .comPortType(ComPortType.TCP)
                .active(true).add();

        // Asserts
        assertThat(onlineComServer.getOutboundComPorts().size()).isEqualTo(1);

        // Reload to make sure to work with an empty ComPort cache.
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(onlineComServer.getId()).get();
        assertThat(reloaded.getOutboundComPorts().size()).isEqualTo(1);
    }

    @Test
    @Transactional
    public void testUpdateAddInboundComPorts () {
        OnlineComServer comServer = createOnlineComServer();

        // Business method
        modemBasedComPort(comServer);
        tcpComPort(comServer);
        udpComPort(comServer);

        // Asserts
        assertThat(comServer.getInboundComPorts().size()).isEqualTo(3);

        // Reload to make sure to work with an empty ComPort cache.
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(comServer.getId()).get();
        assertThat(reloaded.getInboundComPorts().size()).isEqualTo(3);
    }

    @Test
    @Transactional
    public void testUpdateWithUpdatesToComPorts () {
        OnlineComServer onlineComServer = createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(onlineComServer, numberOfComPorts);

        // Business method
        onlineComServer.setActive(false);
        OutboundComPort outboundComPort = onlineComServer.getOutboundComPorts().get(0);
        outboundComPort.setName("Updated-1");
        outboundComPort.save();
        outboundComPort = onlineComServer.getOutboundComPorts().get(1);
        outboundComPort.setName("Updated-2");
        outboundComPort.save();
        outboundComPort = onlineComServer.getOutboundComPorts().get(2);
        outboundComPort.setName("Updated-3");
        outboundComPort.save();

        // Asserts
        assertThat(onlineComServer.getOutboundComPorts().size()).isEqualTo(numberOfComPorts);

        // Reload to make sure to have emptied the cache of ComPorts;
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(onlineComServer.getId()).get();
        assertThat(reloaded.getOutboundComPorts().size()).isEqualTo(numberOfComPorts);
        for (OutboundComPort comPort : reloaded.getOutboundComPorts()) {
            assertThat(comPort.getName().startsWith("Updated")).as("Was expecting the name has changed").isTrue();
        }
    }

    @Test
    @Transactional
    public void testUpdateWithDeletedComPortsViaShadow () {
        OnlineComServer comServer = createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(comServer, numberOfComPorts);

        comServer.setActive(false);
        List<OutboundComPort> outboundComPorts = comServer.getOutboundComPorts();
        Set<Long> comPortIds = new HashSet<>();
        comPortIds.add(outboundComPorts.get(0).getId());
        comPortIds.add(outboundComPorts.get(2).getId());
        // Business method
        comServer.removeComPort(outboundComPorts.get(1).getId());   // Removes the second ComPort.

        // Asserts
        assertThat(comServer.getOutboundComPorts().size()).isEqualTo(numberOfComPorts - 1);

        // Reload to make sure to work with empty ComPort cache
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(comServer.getId()).get();
        assertThat(reloaded.getOutboundComPorts().size()).isEqualTo(numberOfComPorts - 1);
        for (OutboundComPort comPort : reloaded.getOutboundComPorts()) {
            comPortIds.remove(comPort.getId());
        }
        if (!comPortIds.isEmpty()) {
            for (Long comPortId : comPortIds) {
                System.err.println("Unexpected comport id" + comPortId);
            }
            fail("Removal of ComPorts failed because ComPorts that were not removed, were not returned after the update: "+comPortIds);
        }
    }

    @Test
    @Transactional
    public void testDeleteWithComPorts () {
        OnlineComServer comServer = createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(comServer, numberOfComPorts);
        long id = comServer.getId();

        // Business method
        comServer.delete();

        // Asserts
        assertThat(getEngineModelService().findComServer(id).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithComPorts () {
        OnlineComServer comServer = createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(comServer, numberOfComPorts);
        long id = comServer.getId();
        Set<Long> comPortIds = new HashSet<>();
        for (OutboundComPort comPort : comServer.getOutboundComPorts()) {
            comPortIds.add(comPort.getId());
        }

        // Business method
        comServer.makeObsolete();

        // Asserts
        assertThat(getEngineModelService().findComServer(id)).isNotNull();
        for (Long comPortId : comPortIds) {
            ComPort obsoleteComPort = getEngineModelService().findComPort(comPortId);
            assertThat(obsoleteComPort).isNotNull();
            assertThat(obsoleteComPort.isObsolete()).isTrue();
        }
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithComPortsInPool () {
        OutboundComPortPool comPortPool = this.createOutboundComPortPool();
        OnlineComServer comServer = createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(comServer, numberOfComPorts);
        long id = comServer.getId();
        Set<Long> comPortIds = new HashSet<>();
        for (OutboundComPort comPort : comServer.getOutboundComPorts()) {
            comPortIds.add(comPort.getId());
            comPortPool.addOutboundComPort(comPort);
        }

        // Business method
        comServer.makeObsolete();

        // Asserts
        assertThat(getEngineModelService().findComServer(id)).isNotNull();
        for (Long comPortId : comPortIds) {
            ComPort obsoleteComPort = getEngineModelService().findComPort(comPortId);
            assertThat(obsoleteComPort).isNotNull();
            assertThat(obsoleteComPort.isObsolete()).isTrue();
            assertThat(obsoleteComPort).isInstanceOf(OutboundComPort.class);
            List<OutboundComPortPool> containingComPortPools = this.getEngineModelService().findContainingComPortPoolsForComPort((OutboundComPort) obsoleteComPort);
            assertThat(containingComPortPools).isEmpty();
        }
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_PORT_PER_COM_SERVER+"}", property = "portNumber")
    public void duplicateComPortsTest() throws SQLException, BusinessException {
        int duplicatePortNumber = 2222;
        OnlineComServer shadow = createOnlineComServer();

        shadow.newTCPBasedInboundComPort("TCP1", 1, duplicatePortNumber)
                .active(true)
                .comPortPool(tcpBasedInboundComPortPool)
                .add();

        shadow.newTCPBasedInboundComPort("TCP2",1, duplicatePortNumber)
                .active(true)
                .comPortPool(tcpBasedInboundComPortPool)
                .add();
    }

    private int uniqueComPortId=1;
    private void addComPorts(OnlineComServer comServer, int numberOfComPorts) {
        for (int i = 0; i < numberOfComPorts; i++) {
            comServer.newOutboundComPort("Outbound-" + uniqueComPortId++, 1).comPortType(ComPortType.TCP).active(true).add();
        }
    }

    private ModemBasedInboundComPort modemBasedComPort(ComServer comServer) {
        String name = "Inbound-" + uniqueComPortId++;
        return comServer.newModemBasedInboundComport(name, 3, 2, new TimeDuration(30), new TimeDuration(5), new SerialPortConfiguration(name,
                                BaudrateValue.BAUDRATE_9600,
                                NrOfDataBits.EIGHT,
                                NrOfStopBits.ONE,
                                Parities.NONE,
                                FlowControl.NONE))
                .active(true)
                .comPortPool(serialBasedInboundComPortPool)
                .atCommandTry(new BigDecimal(3))
                .add();
    }

    private TCPBasedInboundComPort tcpComPort(ComServer comServer) {
        String name = "Inbound-" + uniqueComPortId++;
        return comServer.newTCPBasedInboundComPort(name, 1, 9000)
                .active(true)
                .comPortPool(tcpBasedInboundComPortPool)
                .add();
    }

    private UDPBasedInboundComPort udpComPort(ComServer comServer) {
        String name = "Inbound-" + uniqueComPortId++;
        return comServer.newUDPBasedInboundComPort(name, 1, 9001)
                .active(true)
                .comPortPool(udpBasedInboundComPortPool)
                .bufferSize(1024)
                .add();
    }

    private int comPortPoolIndex=1;
    private InboundComPortPool newInboundComPortPool(ComPortType comPortType) {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setName("Unique comPortPool "+comPortPoolIndex++);
        inboundComPortPool.setDescription("description");
        inboundComPortPool.setComPortType(comPortType);
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
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setStoreTaskQueueSize(1);
        onlineComServer.setStoreTaskThreadPriority(1);
        onlineComServer.setNumberOfStoreTaskThreads(1);
        onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URL);
        onlineComServer.setEventRegistrationUri(EVENT_REGISTRATION_URL);
        onlineComServer.save();
        return onlineComServer;
    }

    private OutboundComPortPool createOutboundComPortPool () {
        OutboundComPortPool comPortPool = getEngineModelService().newOutboundComPortPool();
        comPortPool.setName("ComServerComPortTest");
        comPortPool.setComPortType(ComPortType.TCP);
        comPortPool.setActive(true);
        comPortPool.setTaskExecutionTimeout(TimeDuration.minutes(1));
        comPortPool.save();
        return comPortPool;
    }

}