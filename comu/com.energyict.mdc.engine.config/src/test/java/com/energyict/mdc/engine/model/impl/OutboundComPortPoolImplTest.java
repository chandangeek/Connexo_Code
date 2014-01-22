package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.Expected;
import com.energyict.mdc.ExpectedErrorRule;
import com.energyict.mdc.Transactional;
import com.energyict.mdc.TransactionalRule;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.DuplicateException;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.energyict.mdc.protocol.api.ComPortType;
import org.junit.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.rules.TestRule;
import org.mockito.Matchers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
* Tests the {@link OutboundComPortPoolImpl} component.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-05-02 (15:59)
*/
public class OutboundComPortPoolImplTest extends PersistenceTest {

    private static final String NAME_BASIS = "ComPortPool-";
    protected static final String DESCRIPTION = "Description";

    private static final ComPortType COM_PORT_TYPE = ComPortType.TCP;
    private static final TimeDuration EXECUTION_TIMEOUT = new TimeDuration(120);

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedErrorRule();

    @Test
    @Transactional
    public void testCreateWithoutPortsWithoutViolations() throws TranslatableApplicationException, SQLException {
        // Business method
        OutboundComPortPool comPortPool = this.newOutboundComPortPoolWithoutViolations();

        // Asserts
        assertFalse("Was NOT expecting an OutboundComPortPool to be inbound", comPortPool.isInbound());
        assertTrue("Was not expecting any ComPorts", comPortPool.getComPorts().isEmpty());
    }

    @Test
    @Transactional
    public void testCreateWithPortsWithoutViolations() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();
        OnlineComServer onlineComServer = createOnlineComServer();
        OutboundComPort portA = onlineComServer.newOutboundComPort()
                .name("portA").numberOfSimultaneousConnections(1).description("hello world")
                .active(true).comPortType(ComPortType.TCP)
                .add();
        OutboundComPort portB = onlineComServer.newOutboundComPort()
                .name("portB").numberOfSimultaneousConnections(1).description("hello world")
                .active(true).comPortType(ComPortType.TCP)
                .add();
        OutboundComPort portC = onlineComServer.newOutboundComPort()
                .name("portC").numberOfSimultaneousConnections(1).description("hello world")
                .active(true).comPortType(ComPortType.TCP)
                .add();
        comPortPool.addOutboundComPort(portA);
        comPortPool.addOutboundComPort(portB);
        comPortPool.addOutboundComPort(portC);

        assertThat(comPortPool.getComPorts()).containsOnly(portA, portB, portC);
    }

    @Test
    @Transactional
//    @Expected(expected = TranslatableApplicationException.class)
    public void testCreateWithPortsWithNonMatchingComPortType() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();
        OnlineComServer onlineComServer = createOnlineComServer();

        OutboundComPort portA = onlineComServer.newOutboundComPort()
                .name("portA").numberOfSimultaneousConnections(1).description("hello world")
                .active(true).comPortType(ComPortType.SERIAL)
                .add();

        // Business method
        comPortPool.addOutboundComPort(portA);

        // Expecting TranslatableApplicationException because the ComPortType of the ComPorts does not match
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEmpty")
    public void testCreateWithoutName() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool outboundComPortPool = getEngineModelService().newOutboundComPortPool();
        outboundComPortPool.setDescription(DESCRIPTION);
        outboundComPortPool.setComPortType(COM_PORT_TYPE);
        outboundComPortPool.setTaskExecutionTimeout(EXECUTION_TIMEOUT);
        outboundComPortPool.save();

        // Expecting TranslatableApplicationException because the name is not set
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEmpty")
    public void testCreateWithoutComPortType() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool outboundComPortPool = getEngineModelService().newOutboundComPortPool();
        outboundComPortPool.setName(NAME_BASIS+outboundComPortPoolIndex++);
        outboundComPortPool.setDescription(DESCRIPTION);
        outboundComPortPool.setTaskExecutionTimeout(EXECUTION_TIMEOUT);
        outboundComPortPool.save();

        // Expecting TranslatableApplicationException because the ComPortType is not set
    }

    @Test
    @Transactional
    public void testLoad() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();

        // Business method
        OutboundComPortPool loaded = getEngineModelService().findOutboundComPortPool(comPortPool.getId());

        assertEquals("Name does not match", comPortPool.getName(), loaded.getName());
        assertEquals("Description does not match", comPortPool.getDescription(), loaded.getDescription());
        assertEquals("ComPortType does not match", COM_PORT_TYPE, loaded.getComPortType());
        assertFalse("Was NOT expecting an OutboundComPortPool to be inbound", loaded.isInbound());
    }

    @Test
    @Transactional
    public void testUpdateWithoutViolations() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();

        comPortPool.setName(comPortPool.getName() + "Updated");
        comPortPool.setDescription(comPortPool.getDescription() + "Updated");

        // Business method
        comPortPool.save();

        OutboundComPortPool retrievedComPortPool = getEngineModelService().findOutboundComPortPool(comPortPool.getId());

        // Asserts
        assertEquals("Name does not match", comPortPool.getName(), retrievedComPortPool.getName());
        assertEquals("Description does not match", comPortPool.getDescription(), retrievedComPortPool.getDescription());
        assertTrue("Was not expecting any ComPorts", retrievedComPortPool.getComPorts().isEmpty());
        assertFalse("Was NOT expecting an OutboundComPortPool to be inbound", retrievedComPortPool.isInbound());
    }

    @Test
    @Transactional
    public void testUpdateAndDeleteComPortMembers() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();
        OnlineComServer onlineComServer = createOnlineComServer();
        OutboundComPort portA = onlineComServer.newOutboundComPort()
                .name("portA").numberOfSimultaneousConnections(1).description("hello world")
                .active(true).comPortType(ComPortType.TCP)
                .add();
        OutboundComPort portB = onlineComServer.newOutboundComPort()
                .name("portB").numberOfSimultaneousConnections(1).description("hello world")
                .active(true).comPortType(ComPortType.TCP)
                .add();
        OutboundComPort portC = onlineComServer.newOutboundComPort()
                .name("portC").numberOfSimultaneousConnections(1).description("hello world")
                .active(true).comPortType(ComPortType.TCP)
                .add();
        comPortPool.addOutboundComPort(portA);
        comPortPool.addOutboundComPort(portB);
        comPortPool.addOutboundComPort(portC);

        assertThat(comPortPool.getComPorts()).containsOnly(portA, portB, portC);

        comPortPool.removeOutboundComPort(portB);

        assertThat(comPortPool.getComPorts()).containsOnly(portA, portC);
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEmpty")
    public void testUpdateWithSameName() throws TranslatableApplicationException, SQLException {
        String name = NAME_BASIS + outboundComPortPoolIndex++;
        OutboundComPortPool outboundComPortPool = getEngineModelService().newOutboundComPortPool();
        outboundComPortPool.setName(name);
        outboundComPortPool.setDescription(DESCRIPTION);
        outboundComPortPool.setComPortType(COM_PORT_TYPE);
        outboundComPortPool.setTaskExecutionTimeout(EXECUTION_TIMEOUT);
        outboundComPortPool.save();
        
        OutboundComPortPool duplicateComPortPool = getEngineModelService().newOutboundComPortPool();
        duplicateComPortPool.setName(name);
        duplicateComPortPool.setDescription(DESCRIPTION);
        duplicateComPortPool.setComPortType(COM_PORT_TYPE);
        outboundComPortPool.setTaskExecutionTimeout(EXECUTION_TIMEOUT);
        duplicateComPortPool.save();

        // Expecting a DuplicateException
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEmpty")
    public void testUpdateWithoutName() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();
        comPortPool.setName(null);

        // Business method
        comPortPool.save();

        // Expecting TranslatableApplicationException because the name is not set
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEmpty")
    public void testUpdateWithoutComPortType() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();
        comPortPool.setComPortType(null);

        // Business method
        comPortPool.save();

        // Expecting TranslatableApplicationException because the discovery protocol is not set
    }

    @Test
    @Transactional
    public void testDelete() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();

        // Business method
        comPortPool.delete();

        assertThat(this.getEngineModelService().findOutboundComPortPool(comPortPool.getId())).isNull();
    }

//    @Test
//    public void testDeleteWithComSession () throws TranslatableApplicationException, SQLException {
//        OutboundComPortPool shadow = getEngineModelService().newOutboundComPortPool();
//        OutboundComPortPool comPortPool = this.getComPortPoolFactory().createOutbound(shadow);
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

//    @Test(expected = TranslatableApplicationException.class)
//    public void testDeleteWithConnectionTasks () throws TranslatableApplicationException, SQLException {
//        OutboundComPortPool shadow = getEngineModelService().newOutboundComPortPool();
//        OutboundComPortPool comPortPool = this.getComPortPoolFactory().createOutbound(shadow);
//        List<ServerConnectionMethod> connectionMethods = new ArrayList<>();
//        ConnectionTask connectionTask = mock(ConnectionTask.class);
//        when(connectionTask.getName()).thenReturn("testDeleteWithConnectionTasks");
//        ServerConnectionMethod connectionMethod = mock(ServerConnectionMethod.class);
//        when(connectionMethod.getConnectionTask()).thenReturn(connectionTask);
//        connectionMethods.add(connectionMethod);
//        when(this.getConnectionMethodFactory().findByPool(comPortPool)).thenReturn(connectionMethods);
//
//        // Business method
//        comPortPool.delete();
//
//        // Expected TranslatableApplicationException because a ComPortPool that is still used by ConnectionTasks cannot be deleted
//    }

    @Test
    @Transactional
    public void testMakeObsolete() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();

        // Business method
        comPortPool.makeObsolete();

        // Asserts
        assertThat(comPortPool.isObsolete()).isTrue();
        assertThat(comPortPool.getObsoleteDate()).isNotNull();
    }

    @Test
    @Transactional
    public void testIsObsoleteAfterReload() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();
        long comPortPoolId = comPortPool.getId();

        // Business method
        comPortPool.makeObsolete();

        ComPortPool reloaded = getEngineModelService().findOutboundComPortPool(comPortPoolId);

        // Asserts
        assertThat(reloaded.isObsolete()).isTrue();
        assertThat(reloaded.getObsoleteDate()).isNotNull();
    }

//    @Test
//    @Transactional
//    @Expected(expected = TranslatableApplicationException.class)
//    public void testMakeObsoleteWithConnectionTasks () throws TranslatableApplicationException, SQLException {
//        OutboundComPortPool shadow = newOutboundComPortPoolWithoutViolations();
//        OutboundComPortPool comPortPool = this.getComPortPoolFactory().createOutbound(shadow);
//        List<ServerConnectionMethod> connectionMethods = new ArrayList<>();
//        ConnectionTask connectionTask = mock(ConnectionTask.class);
//        when(connectionTask.getName()).thenReturn("testMakeObsoleteWithConnectionTasks");
//        ServerConnectionMethod connectionMethod = mock(ServerConnectionMethod.class);
//        when(connectionMethod.getConnectionTask()).thenReturn(connectionTask);
//        connectionMethods.add(connectionMethod);
//        when(this.getConnectionMethodFactory().findByPool(comPortPool)).thenReturn(connectionMethods);
//
//        // Business method
//        comPortPool.makeObsolete();
//
//        // Expected TranslatableApplicationException because a ComPortPool that is still used by ConnectionTasks cannot be made obsolete
//    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "comportpool.noUpdateAllowed")
    public void testUpdateAfterMakeObsolete() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();
        comPortPool.makeObsolete();

        // Business method
        comPortPool.save();

        // Expected a TranslatableApplicationException because an obsolete ComPortPool cannot be updated any longer
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "comPortPoolIsAlreadyObsolete")
    public void testMakeObsoleteTwice() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();
        comPortPool.makeObsolete();

        // Business method
        comPortPool.makeObsolete();

        // Expected a TranslatableApplicationException because a ComPortPool cannot be made obsolete twice
    }

    private int outboundComPortPoolIndex =1;
    private OutboundComPortPool newOutboundComPortPoolWithoutViolations() {
        OutboundComPortPool outboundComPortPool = getEngineModelService().newOutboundComPortPool();
        outboundComPortPool.setName(NAME_BASIS+outboundComPortPoolIndex++);
        outboundComPortPool.setDescription(DESCRIPTION);
        outboundComPortPool.setComPortType(COM_PORT_TYPE);
        outboundComPortPool.setTaskExecutionTimeout(EXECUTION_TIMEOUT);
        outboundComPortPool.save();
        return outboundComPortPool;
    }

    private int inboundComPortPoolIndex =1;
    private InboundComPortPool newInboundComPortPoolWithoutViolations() {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setName("Unique comPortPool "+ inboundComPortPoolIndex++);
        inboundComPortPool.setDescription(DESCRIPTION);
        inboundComPortPool.setComPortType(ComPortType.TCP);
        inboundComPortPool.setDiscoveryProtocolPluggableClassId(1);
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