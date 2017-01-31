/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.ComPortPoolMember;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import com.google.inject.Provider;

import java.sql.SQLException;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    public void testCreateWithoutViolations() throws SQLException {
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
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCreateWithoutName() throws SQLException {
        createOnlineComServer().newTCPBasedInboundComPort(null, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .add();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_ACTIVE_INBOUND_COMPORT_MUST_HAVE_POOL+"}", property = "comPortPool")
    public void testCreateWithoutComPortPool() throws SQLException {
        createOnlineComServer().newTCPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .add();

        // Expecting an InvalidValueException to be thrown because the ComPortPool is not set
    }

    @Test
    @Transactional
    public void testCreateInActivePortWithoutComPortPool() throws SQLException {
        TCPBasedInboundComPort tcpBasedInboundComPort = createOnlineComServer().newTCPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(false)
                .add();

        assertThat(tcpBasedInboundComPort.isActive()).isFalse();
        assertThat(tcpBasedInboundComPort.getComPortPool()).isNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_ACTIVE_INBOUND_COMPORT_MUST_HAVE_POOL+"}")
    public void setPortActiveWithoutComPortPoolTest() {
        TCPBasedInboundComPort tcpBasedInboundComPort = createOnlineComServer().newTCPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(false)
                .add();

        tcpBasedInboundComPort.setActive(true);
        tcpBasedInboundComPort.update();
    }

    @Test
    @Transactional
    public void setPortActiveWithComPortPoolTest() {
        TCPBasedInboundComPort tcpBasedInboundComPort = createOnlineComServer().newTCPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(false)
                .add();

        InboundComPortPool comPortPool = createComPortPool();
        tcpBasedInboundComPort.setComPortPool(comPortPool);
        tcpBasedInboundComPort.setActive(true);
        tcpBasedInboundComPort.update();

        assertThat(tcpBasedInboundComPort.isActive()).isTrue();
        assertThat(tcpBasedInboundComPort.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_PORT+"}", property = "name")
    public void testCreateWithExistingName() throws SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        TCPBasedInboundComPort comPort = this.createSimpleComPort(onlineComServer);

        onlineComServer.newTCPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER+10)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .add();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "numberOfSimultaneousConnections")
    public void testCreateWithZeroSimultaneousConnections() throws SQLException {
        createOnlineComServer().newTCPBasedInboundComPort(COMPORT_NAME, 0, PORT_NUMBER)
                .description(DESCRIPTION)
                .comPortPool(createComPortPool())
                .active(ACTIVE)
                .add();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "portNumber")
    public void testCreateWithZeroPortNumber() throws SQLException {
        createOnlineComServer().newTCPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, 0)
                .description(DESCRIPTION)
                .comPortPool(createComPortPool())
                .active(ACTIVE)
                .add();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_PORT_PER_COM_SERVER+"}", property = "portNumber")
    public void createWithExistingPortNumberTest() throws SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        onlineComServer.newTCPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .add();

        onlineComServer.newTCPBasedInboundComPort("createWithExistingPortNumberTest", NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .comPortPool(createComPortPool())
                .active(ACTIVE)
                .add();

        // Business method
    }

    @Test
    @Transactional
    public void testLoad() throws SQLException {
        TCPBasedInboundComPort comPort = this.createSimpleComPort();
        TCPBasedInboundComPort reloaded = (TCPBasedInboundComPort) getEngineModelService().findComPort(comPort.getId()).get();

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
    public void updateWithoutViolations() throws SQLException {
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

        comPort.update();

        // Asserts
        assertEquals("Name does not match", newName, comPort.getName());
        assertEquals("Description does not match", newDescription, comPort.getDescription());
        assertEquals("Was NOT expecting the new com port to be active", newActive, comPort.isActive());
        assertEquals("Incorrect number of simultaneous connections", newNumberOfSimultaneousConnections, comPort.getNumberOfSimultaneousConnections());
        assertEquals("Incorrect listening PortNumber", newPortNumber, comPort.getPortNumber());
    }

    private int comPortPoolIndex=1;
    private InboundComPortPool createComPortPool() {
        return getEngineModelService().newInboundComPortPool("ComPortPool "+comPortPoolIndex++, ComPortType.TCP, inboundDeviceProtocolPluggableClass);
    }

    private TCPBasedInboundComPort createSimpleComPort() {
        return createSimpleComPort(createOnlineComServer());
    }

    private TCPBasedInboundComPort createSimpleComPort(ComServer comServer) {
        return comServer.newTCPBasedInboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
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