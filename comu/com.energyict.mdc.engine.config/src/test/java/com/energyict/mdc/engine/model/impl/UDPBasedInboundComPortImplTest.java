package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import java.sql.SQLException;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}", property = "comPortPool")
    public void testCreateWithoutComPortPool() throws BusinessException, SQLException {
        createOnlineComServer().newUDPBasedInboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .portNumber(PORT_NUMBER)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .add();

        // Expecting an InvalidValueException to be thrown because the ComPortPool is not set
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}", property = "comPortPool")
    public void testCreateWithOutboundComPortPool() throws BusinessException, SQLException {
        createOnlineComServer().newUDPBasedInboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .portNumber(PORT_NUMBER)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .add();

        // Expecting an TranslatableApplicationException to be thrown because the ComPortPool is not an InboundComPortPool
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCreateWithoutName() throws BusinessException, SQLException {
        createOnlineComServer().newUDPBasedInboundComPort()
        .description(DESCRIPTION)
        .active(ACTIVE)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .portNumber(PORT_NUMBER)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortPool(createComPortPool())
        .add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_DUPLICATE_COM_PORT+"}", property = "name")
    public void testCreateWithExistingName() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        UDPBasedInboundComPort comPort = this.createSimpleComPort(onlineComServer);
        onlineComServer.newUDPBasedInboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .portNumber(PORT_NUMBER+1)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortPool(createComPortPool())
        .add();

        // Expecting a BusinessException to be thrown because a ComPort with the same name already exists
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_VALUE_TOO_SMALL+"}", property = "numberOfSimultaneousConnections")
    public void testCreateWithZeroSimultaneousConnections() throws BusinessException, SQLException {
        createOnlineComServer().newUDPBasedInboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .portNumber(PORT_NUMBER)
        .numberOfSimultaneousConnections(0)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortPool(createComPortPool())
        .add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_VALUE_TOO_SMALL+"}", property = "portNumber")
    public void testCreateWithZeroPortNumber() throws BusinessException, SQLException {
        createOnlineComServer().newUDPBasedInboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .portNumber(0)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortPool(createComPortPool())
        .add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MDC_VALUE_TOO_SMALL+"}", property = "bufferSize")
    public void testCreateWithZeroBufferSize() throws BusinessException, SQLException {
        createOnlineComServer().newUDPBasedInboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .portNumber(PORT_NUMBER)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .bufferSize(0)
        .comPortPool(createComPortPool())
        .add();

        // Expecting a BusinessException to be thrown because the name is not set
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
        inboundComPortPool.setDiscoveryProtocolPluggableClass(inboundDeviceProtocolPluggableClass);
        inboundComPortPool.save();
        return inboundComPortPool;
    }

    private UDPBasedInboundComPort createSimpleComPort() {
        return createSimpleComPort(createOnlineComServer());
    }

    private UDPBasedInboundComPort createSimpleComPort(ComServer comServer) {
        return comServer.newUDPBasedInboundComPort()
                .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .active(ACTIVE)
                
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