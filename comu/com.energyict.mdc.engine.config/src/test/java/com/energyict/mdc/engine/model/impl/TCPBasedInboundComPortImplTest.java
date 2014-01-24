package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.Expected;
import com.energyict.mdc.ExpectedErrorRule;
import com.energyict.mdc.Transactional;
import com.energyict.mdc.TransactionalRule;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.google.inject.Provider;
import org.junit.*;

import java.sql.SQLException;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.junit.Assert.*;

/**
 * Tests for the {@link TCPBasedInboundComPortImpl} object
 *
 * @author gna
 * @since 3/04/12 - 11:03
 */
public class TCPBasedInboundComPortImplTest extends PersistenceTest {

    private static final String COMPORT_NAME = "TCPBasedComPort";
    private static final String DESCRIPTION = "Description for new TCP Based Inbound ComPort";
    private static final boolean ACTIVE = true;
    private static final int NUMBER_OF_SIMULTANEOUS_CONNECTIONS = 11;
    private static final int PORT_NUMBER = 8080;

    @Mock
    DataModel dataModel;
    @Mock
    Provider<ComPortPoolMember> provider;

    @Test
    @Transactional
    public void testIsTCPBased() {
        TCPBasedInboundComPort comPort = createSimpleComPort(createOnlineComServer());
        assertTrue(comPort.isTCPBased());
        assertFalse(comPort.isUDPBased());
        assertFalse(comPort.isModemBased());
        assertFalse(comPort.isServletBased());
        assertTrue("TCP based inbound com ports are expected to be INBOUND", comPort.isInbound());
    }

    @Test
    @Transactional
    public void testCreateWithoutViolations() throws BusinessException, SQLException {
        TCPBasedInboundComPort comPort = this.createSimpleComPort();

        // Asserts
        assertEquals("Name does not match", COMPORT_NAME, comPort.getName());
        assertEquals("Description does not match", DESCRIPTION, comPort.getDescription());
        assertTrue("Was expecting the new com port to be active", comPort.isActive());
        assertEquals("Incorrect number of simultaneous connections", NUMBER_OF_SIMULTANEOUS_CONNECTIONS, comPort.getNumberOfSimultaneousConnections());
        assertEquals("Incorrect listening PortNumber", PORT_NUMBER, comPort.getPortNumber());
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEmpty")
    public void testCreateWithoutName() throws BusinessException, SQLException {
        createOnlineComServer().newTCPBasedInboundComPort()
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .portNumber(PORT_NUMBER).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEmpty")
    public void testCreateWithoutComPortPool() throws BusinessException, SQLException {
        createOnlineComServer().newTCPBasedInboundComPort()
                .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .portNumber(PORT_NUMBER)
                .comPortType(ComPortType.TCP).add();

        // Expecting an InvalidValueException to be thrown because the ComPortPool is not set
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "duplicateComPortX")
    public void testCreateWithExistingName() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        TCPBasedInboundComPort comPort = this.createSimpleComPort(onlineComServer);

        onlineComServer.newTCPBasedInboundComPort()
                .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .portNumber(PORT_NUMBER)
                .comPortType(ComPortType.TCP).add();

        // Expecting a BusinessException to be thrown because a ComPort with the same name already exists
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEqualOrLessThanZero")
    public void testCreateWithZeroSimultaneousConnections() throws BusinessException, SQLException {
        createOnlineComServer().newTCPBasedInboundComPort()
                .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .comPortPool(createComPortPool())
                .active(ACTIVE)
                .portNumber(PORT_NUMBER)
                .numberOfSimultaneousConnections(0)
                .comPortType(ComPortType.TCP).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEqualOrLessThanZero")
    public void testCreateWithZeroPortNumber() throws BusinessException, SQLException {
        createOnlineComServer().newTCPBasedInboundComPort()
                .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .comPortPool(createComPortPool())
                .active(ACTIVE)
                .portNumber(0)
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .comPortType(ComPortType.TCP).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "duplicatecomportpercomserver")
    public void createWithExistingPortNumberTest() throws SQLException, BusinessException {
        OnlineComServer onlineComServer = createOnlineComServer();
        onlineComServer.newTCPBasedInboundComPort()
                .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .portNumber(PORT_NUMBER)
                .comPortPool(createComPortPool())
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .comPortType(ComPortType.TCP).add();

        onlineComServer.newTCPBasedInboundComPort()
                .name("createWithExistingPortNumberTest")
                .description(DESCRIPTION)
                .comPortPool(createComPortPool())
                .active(ACTIVE)
                .portNumber(PORT_NUMBER)
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .comPortType(ComPortType.TCP).add();

        // Business method
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEmpty")
    public void testWithoutComPortType() throws BusinessException, SQLException {
        createOnlineComServer().newTCPBasedInboundComPort()
                .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .comPortPool(createComPortPool())
                .portNumber(PORT_NUMBER)
                .comPortType(null).add();
    }

    @Test
    @Transactional
    public void testLoad() throws SQLException, BusinessException {
        TCPBasedInboundComPort comPort = this.createSimpleComPort();
        TCPBasedInboundComPort reloaded = (TCPBasedInboundComPort) getEngineModelService().findComPort(comPort.getId());

        // Asserts
        assertNotNull("Was expecting to find the entity that was just created", reloaded);
        assertEquals("Name does not match", comPort.getName(), reloaded.getName());
        assertEquals("Description does not match", comPort.getDescription(), reloaded.getDescription());
        assertEquals("Was NOT expecting the new com port to be active", comPort.isActive(), reloaded.isActive());
        assertEquals("Incorrect number of simultaneous connections", comPort.getNumberOfSimultaneousConnections(), reloaded.getNumberOfSimultaneousConnections());
        assertEquals("Incorrect listening PortNumber", comPort.getPortNumber(), reloaded.getPortNumber());
    }

    @Test
    @Transactional
    public void updateWithoutViolations() throws BusinessException, SQLException {
        final int newNumberOfSimultaneousConnections = 99;
        final int newPortNumber = 512;
        final String newName = "NewComPortName";
        final String newDescription = "NewDescriptionForUpdatedModemBasedInboundComPortImpl";
        final boolean newActive = false;

        TCPBasedInboundComPortImpl comPort = (TCPBasedInboundComPortImpl) this.createSimpleComPort();

        comPort.setName(newName);
        comPort.setDescription(newDescription);
        comPort.setActive(newActive);
        comPort.setPortNumber(newPortNumber);
        comPort.setNumberOfSimultaneousConnections(newNumberOfSimultaneousConnections);

        comPort.save();

        // Asserts
        assertEquals("Name does not match", newName, comPort.getName());
        assertEquals("Description does not match", newDescription, comPort.getDescription());
        assertEquals("Was NOT expecting the new com port to be active", newActive, comPort.isActive());
        assertEquals("Incorrect number of simultaneous connections", newNumberOfSimultaneousConnections, comPort.getNumberOfSimultaneousConnections());
        assertEquals("Incorrect listening PortNumber", newPortNumber, comPort.getPortNumber());
    }

    private int comPortPoolIndex=1;
    private InboundComPortPool createComPortPool() {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setName("ComPortPool "+comPortPoolIndex++);
        inboundComPortPool.setComPortType(ComPortType.TCP);
        inboundComPortPool.setDiscoveryProtocolPluggableClassId(1);
        inboundComPortPool.save();
        return inboundComPortPool;
    }

    private TCPBasedInboundComPort createSimpleComPort() {
        return createSimpleComPort(createOnlineComServer());
    }

    private TCPBasedInboundComPort createSimpleComPort(ComServer comServer) {
        return comServer.newTCPBasedInboundComPort()
                .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortType(ComPortType.TCP)
                .comPortPool(createComPortPool())
                .portNumber(PORT_NUMBER)
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