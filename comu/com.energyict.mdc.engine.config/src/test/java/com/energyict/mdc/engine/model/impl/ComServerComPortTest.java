package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.energyict.mdc.Transactional;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.channels.serial.BaudrateValue;
import com.energyict.mdc.protocol.api.channels.serial.FlowControl;
import com.energyict.mdc.protocol.api.channels.serial.NrOfDataBits;
import com.energyict.mdc.protocol.api.channels.serial.NrOfStopBits;
import com.energyict.mdc.protocol.api.channels.serial.Parities;
import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Fail.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    private static final int PLUGGABLE_CLASS_ID = 1;

    private InboundComPortPool tcpBasedInboundComPortPool;
    private InboundComPortPool udpBasedInboundComPortPool;
    private InboundComPortPool serialBasedInboundComPortPool;

    @Before
    public void setUp () {
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
        assertTrue("Was expecting the new com server to be active", comServer.isActive());
        assertEquals(SERVER_LOG_LEVEL, comServer.getServerLogLevel());
        assertEquals(COMMUNICATION_LOG_LEVEL, comServer.getCommunicationLogLevel());
        assertEquals(CHANGES_INTER_POLL_DELAY, comServer.getChangesInterPollDelay());
        assertEquals(SCHEDULING_INTER_POLL_DELAY, comServer.getSchedulingInterPollDelay());
        assertEquals("The number of com ports does not match.", numberOfComPorts, comServer.getOutboundComPorts().size());
    }

    @Test
    @Transactional
    public void loadWithComPortsTest() {
        OnlineComServer shadow = createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(shadow, numberOfComPorts);

        // Business method
        OnlineComServer loadedOnlineServer = (OnlineComServer) getEngineModelService().findComServer(shadow.getId());

        // Asserts
        assertTrue("Was expecting the new com server to be active", loadedOnlineServer.isActive());
        assertEquals(SERVER_LOG_LEVEL, loadedOnlineServer.getServerLogLevel());
        assertEquals(COMMUNICATION_LOG_LEVEL, loadedOnlineServer.getCommunicationLogLevel());
        assertEquals(CHANGES_INTER_POLL_DELAY, loadedOnlineServer.getChangesInterPollDelay());
        assertEquals(SCHEDULING_INTER_POLL_DELAY, loadedOnlineServer.getSchedulingInterPollDelay());
        assertEquals("The number of com ports does not match.", numberOfComPorts, loadedOnlineServer.getOutboundComPorts().size());
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
        assertEquals("The number of com ports does not match.", numberOfComPorts, onlineComServer.getOutboundComPorts().size());

        // Reload to make sure to work with an empty ComPort cache.
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(onlineComServer.getId());
        assertEquals("The number of com ports does not match.", numberOfComPorts, reloaded.getOutboundComPorts().size());
    }

    @Test
    @Transactional
    public void testUpdateAddOutboundComPorts () {
        OnlineComServer onlineComServer = createOnlineComServer();

        // Business method
        onlineComServer.newOutboundComPort()
                .name("Outbound-" + uniqueComPortId++)
                .comPortType(ComPortType.TCP)
                .numberOfSimultaneousConnections(1)
                .active(true).add();

        // Asserts
        assertEquals("The number of com ports does not match.", 1, onlineComServer.getOutboundComPorts().size());

        // Reload to make sure to work with an empty ComPort cache.
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(onlineComServer.getId());
        assertEquals("The number of com ports does not match.", 1, reloaded.getOutboundComPorts().size());
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
        assertEquals("The number of com ports does not match.", 3, comServer.getInboundComPorts().size());

        // Reload to make sure to work with an empty ComPort cache.
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(comServer.getId());
        assertEquals("The number of com ports does not match.", 3, reloaded.getInboundComPorts().size());
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
        assertEquals("The number of com ports does not match.", numberOfComPorts, onlineComServer.getOutboundComPorts().size());

        // Reload to make sure to have emptied the cache of ComPorts;
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(onlineComServer.getId());
        assertEquals("The number of com ports does not match.", numberOfComPorts, reloaded.getOutboundComPorts().size());
        for (OutboundComPort comPort : reloaded.getOutboundComPorts()) {
            assertTrue("Was expecting the name has changed", comPort.getName().startsWith("Updated"));
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
        assertEquals("The number of com ports does not match.", numberOfComPorts - 1, comServer.getOutboundComPorts().size());

        // Reload to make sure to work with empty ComPort cache
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(comServer.getId());
        assertEquals("The number of com ports does not match.", numberOfComPorts - 1, reloaded.getOutboundComPorts().size());
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
        assertNull(getEngineModelService().findComServer(id));
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
        assertNotNull(getEngineModelService().findComServer(id));
        for (Long comPortId : comPortIds) {
            ComPort obsoleteComPort = getEngineModelService().findComPort(comPortId);
            assertNotNull(obsoleteComPort);
            assertTrue(obsoleteComPort.isObsolete());
        }
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{MDC.DuplicateComPortPerComServer}", property = "portNumber")
    public void duplicateComPortsTest() throws SQLException, BusinessException {
        int duplicatePortNumber = 2222;
        OnlineComServer shadow = createOnlineComServer();

        shadow.newTCPBasedInboundComPort()
                .name("TCP1")
                .active(true)
                .comPortPool(tcpBasedInboundComPortPool)
                .numberOfSimultaneousConnections(1)
                .portNumber(duplicatePortNumber).add();

        shadow.newTCPBasedInboundComPort()
                .name("TCP2")
                .active(true)
                .comPortPool(tcpBasedInboundComPortPool)
                .numberOfSimultaneousConnections(1)
                .portNumber(duplicatePortNumber).add();
    }

    private int uniqueComPortId=1;
    private void addComPorts(OnlineComServer comServerShadow, int numberOfComPorts) {
        for (int i = 0; i < numberOfComPorts; i++) {
            comServerShadow.newOutboundComPort().name("Outbound-" + uniqueComPortId++).comPortType(ComPortType.TCP).numberOfSimultaneousConnections(1).active(true).add();
        }
    }

    private ModemBasedInboundComPort modemBasedComPort(ComServer comServer) {
        String name = "Inbound-" + uniqueComPortId++;
        return comServer.newModemBasedInboundComport()
                .name(name)
                .active(true)
                .comPortPool(serialBasedInboundComPortPool)
                .maximumDialErrors(2).ringCount(3)
                .connectTimeout(new TimeDuration(30))
                .atCommandTimeout(new TimeDuration(5))
                .atCommandTry(new BigDecimal(3))
                .serialPortConfiguration(new SerialPortConfiguration(name,
                        BaudrateValue.BAUDRATE_9600,
                        NrOfDataBits.EIGHT,
                        NrOfStopBits.ONE,
                        Parities.NONE,
                        FlowControl.NONE))
                .add();
    }

    private TCPBasedInboundComPort tcpComPort(ComServer comServer) {
        String name = "Inbound-" + uniqueComPortId++;
        return comServer.newTCPBasedInboundComPort()
                .name(name)
                .active(true)
                .comPortPool(tcpBasedInboundComPortPool)
                .portNumber(9000)
                .numberOfSimultaneousConnections(1)
                .add();
    }

    private UDPBasedInboundComPort udpComPort(ComServer comServer) {
        String name = "Inbound-" + uniqueComPortId++;
        return comServer.newUDPBasedInboundComPort()
                .name(name)
                .active(true)
                .comPortPool(udpBasedInboundComPortPool)
                .portNumber(9001)
                .bufferSize(1024)
                .numberOfSimultaneousConnections(1)
                .add();
    }
    
    private int comPortPoolIndex=1;
    private InboundComPortPool newInboundComPortPool(ComPortType comPortType) {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setName("Unique comPortPool "+comPortPoolIndex++);
        inboundComPortPool.setDescription("description");
        inboundComPortPool.setComPortType(comPortType);
        inboundComPortPool.setDiscoveryProtocolPluggableClassId(PLUGGABLE_CLASS_ID);
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
    

}