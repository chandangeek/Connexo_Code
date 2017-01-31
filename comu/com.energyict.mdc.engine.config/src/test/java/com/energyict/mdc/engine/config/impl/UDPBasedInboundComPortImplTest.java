/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import java.sql.SQLException;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
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
    public void testCreateWithoutViolations() throws SQLException {
        UDPBasedInboundComPort comPort = this.createSimpleComPort();

        // Asserts
        assertEquals("Name does not match", COMPORT_NAME, comPort.getName());
        assertEquals("Description does not match", DESCRIPTION, comPort.getDescription());
        assertTrue("Was expecting the new com port to be active", comPort.isActive());
        assertEquals("Incorrect number of simultaneous connections", NUMBER_OF_SIMULTANEOUS_CONNECTIONS, comPort.getNumberOfSimultaneousConnections());
        assertEquals("Incorrect listening PortNumber", PORT_NUMBER, comPort.getPortNumber());
        assertEquals("Buffer size should be the same", Integer.valueOf(DATAGRAM_BUFFER_SIZE), comPort.getBufferSize());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_ACTIVE_INBOUND_COMPORT_MUST_HAVE_POOL+"}")
    public void testCreateWithoutComPortPool() throws SQLException {
        createOnlineComServer().newUDPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .add();

        // Expecting an InvalidValueException to be thrown because the ComPortPool is not set
    }

    @Test
    @Transactional
    public void testCreateInActivePortWithoutComPortPool() throws SQLException {
        UDPBasedInboundComPort udpBasedInboundComPort = createOnlineComServer().newUDPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(false)
                .bufferSize(DATAGRAM_BUFFER_SIZE)
                .add();

        assertThat(udpBasedInboundComPort.isActive()).isFalse();
        assertThat(udpBasedInboundComPort.getComPortPool()).isNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_ACTIVE_INBOUND_COMPORT_MUST_HAVE_POOL+"}")
    public void setPortActiveWithoutComPortPoolTest() {
        UDPBasedInboundComPort udpBasedInboundComPort = createOnlineComServer().newUDPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(false)
                .bufferSize(DATAGRAM_BUFFER_SIZE)
                .add();

        udpBasedInboundComPort.setActive(true);
        udpBasedInboundComPort.update();
    }

    @Test
    @Transactional
    public void setPortActiveWithComPortPoolTest() {
        UDPBasedInboundComPort udpBasedInboundComPort = createOnlineComServer().newUDPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(false)
                .bufferSize(DATAGRAM_BUFFER_SIZE)
                .add();

        InboundComPortPool comPortPool = createComPortPool();
        udpBasedInboundComPort.setComPortPool(comPortPool);
        udpBasedInboundComPort.setActive(true);
        udpBasedInboundComPort.update();

        assertThat(udpBasedInboundComPort.isActive()).isTrue();
        assertThat(udpBasedInboundComPort.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCreateWithoutName() throws SQLException {
        createOnlineComServer().newUDPBasedInboundComPort(null, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortPool(createComPortPool())
        .add();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_PORT+"}", property = "name")
    public void testCreateWithExistingName() throws SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        UDPBasedInboundComPort comPort = this.createSimpleComPort(onlineComServer);
        onlineComServer.newUDPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER+1)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortPool(createComPortPool())
        .add();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "numberOfSimultaneousConnections")
    public void testCreateWithZeroSimultaneousConnections() throws SQLException {
        createOnlineComServer().newUDPBasedInboundComPort(COMPORT_NAME, 0, PORT_NUMBER)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortPool(createComPortPool())
        .add();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "portNumber")
    public void testCreateWithZeroPortNumber() throws SQLException {
        createOnlineComServer().newUDPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS,0)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .bufferSize(DATAGRAM_BUFFER_SIZE)
        .comPortPool(createComPortPool())
        .add();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "bufferSize")
    public void testCreateWithZeroBufferSize() throws SQLException {
        createOnlineComServer().newUDPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS,PORT_NUMBER)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .bufferSize(0)
        .comPortPool(createComPortPool())
        .add();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    public void testLoad() throws SQLException {
        UDPBasedInboundComPort comPort = this.createSimpleComPort();
        UDPBasedInboundComPort reloaded = (UDPBasedInboundComPort) getEngineModelService().findComPort(comPort.getId()).get();

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
    public void updateWithoutViolations() throws SQLException {
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

        comPort.update();
        // Asserts
        assertEquals("Name does not match", newName, comPort.getName());
        assertEquals("Description does not match", newDescription, comPort.getDescription());
        assertEquals("Was NOT expecting the new com port to be active", newActive, comPort.isActive());
        assertEquals("Incorrect number of simultaneous connections", newNumberOfSimultaneousConnections, comPort.getNumberOfSimultaneousConnections());
        assertEquals("Incorrect listening PortNumber", newPortNumber, comPort.getPortNumber());
        assertEquals("Buffer size should be the same", Integer.valueOf(newBufferSize), comPort.getBufferSize());
    }

    private int comPortPoolIndex=1;
    private InboundComPortPool createComPortPool() {
        return getEngineModelService().newInboundComPortPool("comPortPool "+comPortPoolIndex++, ComPortType.UDP, inboundDeviceProtocolPluggableClass);
    }

    private UDPBasedInboundComPort createSimpleComPort() {
        return createSimpleComPort(createOnlineComServer());
    }

    private UDPBasedInboundComPort createSimpleComPort(ComServer comServer) {
        return comServer.newUDPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .bufferSize(DATAGRAM_BUFFER_SIZE)
                .add();

    }

    private int onlineNameNumber = 1;

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