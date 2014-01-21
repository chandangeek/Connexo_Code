package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.Expected;
import com.energyict.mdc.ExpectedErrorRule;
import com.energyict.mdc.Transactional;
import com.energyict.mdc.TransactionalRule;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;

import org.junit.*;

import java.sql.SQLException;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.junit.Assert.*;

/**
 * Test for the {@link UDPBasedInboundComPortImpl} object.
 *
 * @author gna
 * @since 3/04/12 - 12:06
 */
public class UDPBasedInboundComPortImplTest extends PersistenceTest {

    private static final String COMPORT_NAME = "UDPBasedComPort";
    private static final String DESCRIPTION = "Description for new UDP Based Inbound ComPort";
    private static final boolean ACTIVE = true;
    private static final int NUMBER_OF_SIMULTANEOUS_CONNECTIONS = 11;
    private static final int PORT_NUMBER = 8080;
    private static final int DATAGRAM_BUFFER_SIZE = 4096;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedErrorRule();

    @Mock
    DataModel dataModel;

    @Test
    @Transactional
    public void testInbound() {
        UDPBasedInboundComPort comPort = createSimpleComPort();
        assertTrue(comPort.isInbound());
    }

    @Test
    @Transactional
    public void testIsUDPBased () {
        UDPBasedInboundComPort comPort = createSimpleComPort();
        assertTrue(comPort.isUDPBased());
    }

    @Test
    @Transactional
    public void testIsNotTCPBased () {
        UDPBasedInboundComPort comPort = createSimpleComPort();
        assertFalse(comPort.isTCPBased());
    }

    @Test
    @Transactional
    public void testIsNotModemBased () {
        UDPBasedInboundComPort comPort = createSimpleComPort();
        assertFalse(comPort.isModemBased());
    }

    @Test
    @Transactional
    public void testIsNotServletBased () {
        UDPBasedInboundComPort comPort = createSimpleComPort();
        assertFalse(comPort.isServletBased());
    }

    @Test
    @Transactional
    public void testCreateWithoutViolations() throws BusinessException, SQLException {
        UDPBasedInboundComPort comPort = this.createSimpleComPort();

        // Asserts
        assertEquals("Name does not match", COMPORT_NAME, comPort.getName());
        assertEquals("Description does not match", DESCRIPTION, comPort.getDescription());
        assertTrue("Was expecting the new com port to be active", comPort.isActive());
        assertEquals("Incorrect number of simultaneous connections", NUMBER_OF_SIMULTANEOUS_CONNECTIONS, comPort.getNumberOfSimultaneousConnections());
        assertEquals("Incorrect listening PortNumber", PORT_NUMBER, comPort.getPortNumber());
        assertEquals("Buffer size should be the same", DATAGRAM_BUFFER_SIZE, comPort.getBufferSize());
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEmpty")
    public void testCreateWithoutComPortPool() throws BusinessException, SQLException {
        createOnlineComServer().newUDPBasedInboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .portNumber(PORT_NUMBER)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortType(ComPortType.UDP).add();

        // Expecting an InvalidValueException to be thrown because the ComPortPool is not set
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEmpty")
    public void testCreateWithOutboundComPortPool() throws BusinessException, SQLException {
        createOnlineComServer().newUDPBasedInboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .portNumber(PORT_NUMBER)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortType(ComPortType.UDP).add();

        // Expecting an TranslatableApplicationException to be thrown because the ComPortPool is not an InboundComPortPool
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class)
    public void testCreateWithoutName() throws BusinessException, SQLException {
        createOnlineComServer().newUDPBasedInboundComPort()
        .description(DESCRIPTION)
        .active(ACTIVE)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .portNumber(PORT_NUMBER)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortType(ComPortType.UDP).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "duplicateComPortX")
    public void testCreateWithExistingName() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        UDPBasedInboundComPort comPort = this.createSimpleComPort(onlineComServer);
        onlineComServer.newUDPBasedInboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .portNumber(PORT_NUMBER)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortType(ComPortType.UDP).add();

        // Expecting a BusinessException to be thrown because a ComPort with the same name already exists
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEqualOrLessThanZero")
    public void testCreateWithZeroSimultaneousConnections() throws BusinessException, SQLException {
        createOnlineComServer().newUDPBasedInboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .portNumber(PORT_NUMBER)
        .numberOfSimultaneousConnections(0)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortType(ComPortType.UDP).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEqualOrLessThanZero")
    public void testCreateWithZeroPortNumber() throws BusinessException, SQLException {
        createOnlineComServer().newUDPBasedInboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .portNumber(0)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortType(ComPortType.UDP).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class)
    public void testCreateWithZeroBufferSize() throws BusinessException, SQLException {
        createOnlineComServer().newUDPBasedInboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .portNumber(PORT_NUMBER)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .bufferSize(0)
        .comPortType(ComPortType.UDP).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEmpty")
    public void testWithoutComPortType() throws BusinessException, SQLException {
        createOnlineComServer().newUDPBasedInboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .portNumber(PORT_NUMBER)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortType(null).add();
    }

    @Test
    @Transactional
    public void testLoad() throws SQLException, BusinessException {
        UDPBasedInboundComPort comPort = this.createSimpleComPort();
        UDPBasedInboundComPort reloaded = (UDPBasedInboundComPort) getEngineModelService().findComPort(comPort.getId());

        // Asserts
        assertNotNull("Was expecting to find the entity that was just created", reloaded);
        assertEquals("Name does not match", comPort.getName(), reloaded.getName());
        assertEquals("Description does not match", comPort.getDescription(), reloaded.getDescription());
        assertEquals("Was NOT expecting the new com port to be active", comPort.isActive(), reloaded.isActive());
        assertEquals("Incorrect number of simultaneous connections", comPort.getNumberOfSimultaneousConnections(), reloaded.getNumberOfSimultaneousConnections());
        assertEquals("Incorrect listening PortNumber", comPort.getPortNumber(), reloaded.getPortNumber());
        assertEquals("Buffer size should be the same", comPort.getBufferSize(), reloaded.getBufferSize());
    }

    @Test
    @Transactional
    public void updateWithoutViolations() throws BusinessException, SQLException {
        final int newNumberOfSimultaneousConnections = 99;
        final int newPortNumber = 512;
        final String newName = "NewComPortName";
        final String newDescription = "NewDescriptionForUpdatedModemBasedInboundComPortImpl";
        final boolean newActive = false;
        final int newBufferSize = 8192;

        UDPBasedInboundComPortImpl comPort = (UDPBasedInboundComPortImpl) this.createSimpleComPort();

        comPort.setName(newName);
        comPort.setDescription(newDescription);
        comPort.setActive(newActive);
        comPort.setPortNumber(newPortNumber);
        comPort.setNumberOfSimultaneousConnections(newNumberOfSimultaneousConnections);
        comPort.setBufferSize(newBufferSize);

        comPort.save();
        // Asserts
        assertEquals("Name does not match", newName, comPort.getName());
        assertEquals("Description does not match", newDescription, comPort.getDescription());
        assertEquals("Was NOT expecting the new com port to be active", newActive, comPort.isActive());
        assertEquals("Incorrect number of simultaneous connections", newNumberOfSimultaneousConnections, comPort.getNumberOfSimultaneousConnections());
        assertEquals("Incorrect listening PortNumber", newPortNumber, comPort.getPortNumber());
        assertEquals("Buffer size should be the same", newBufferSize, comPort.getBufferSize());
    }

    private int comPortPoolIndex=1;
    private InboundComPortPool createComPortPool() {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setComPortType(ComPortType.UDP);
        inboundComPortPool.setName("comPortPool "+comPortPoolIndex++);
        inboundComPortPool.setDiscoveryProtocolPluggableClassId(1);
        inboundComPortPool.save();
        return inboundComPortPool;
    }

    private ServerUDPBasedInboundComPort createSimpleComPort() {
        return createSimpleComPort(createOnlineComServer());
    }

    private ServerUDPBasedInboundComPort createSimpleComPort(ComServer comServer) {
        return comServer.newUDPBasedInboundComPort()
                .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortType(ComPortType.UDP)
                .comPortPool(createComPortPool())
                .portNumber(PORT_NUMBER)
                .bufferSize(DATAGRAM_BUFFER_SIZE)
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .add();

    }

    private int onlineNameNumber = 1;

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