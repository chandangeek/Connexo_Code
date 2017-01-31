/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.Expected;
import com.energyict.mdc.ExpectedErrorRule;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.protocol.api.ComPortType;

import java.sql.SQLException;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

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
        OutboundComPort portA = onlineComServer.newOutboundComPort("portA", 1)
                .description("hello world")
                .active(true).comPortType(ComPortType.TCP)
                .add();
        OutboundComPort portB = onlineComServer.newOutboundComPort("portB", 1)
                .description("hello world")
                .active(true).comPortType(ComPortType.TCP)
                .add();
        OutboundComPort portC = onlineComServer.newOutboundComPort("portC", 1)
                .description("hello world")
                .active(true).comPortType(ComPortType.TCP)
                .add();
        comPortPool.addOutboundComPort(portA);
        comPortPool.addOutboundComPort(portB);
        comPortPool.addOutboundComPort(portC);

        assertThat(comPortPool.getComPorts()).containsOnly(portA, portB, portC);
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "comPortTypeOfComPortXDoesNotMatchWithComPortPoolY")
    public void testAddComPortWithNonMatchingComPortType() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();
        OnlineComServer onlineComServer = createOnlineComServer();

        OutboundComPort portA = onlineComServer.newOutboundComPort("portA", 1)
                .description("hello world")
                .active(true).comPortType(ComPortType.SERIAL)
                .add();

        // Business method
        comPortPool.addOutboundComPort(portA);

        // Expecting TranslatableApplicationException because the ComPortType of the ComPorts does not match
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCreateWithoutName() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool outboundComPortPool = getEngineModelService().newOutboundComPortPool(null, COM_PORT_TYPE, EXECUTION_TIMEOUT);
        outboundComPortPool.setDescription(DESCRIPTION);

        // Expecting TranslatableApplicationException because the name is not set
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "comPortType")
    public void testCreateWithoutComPortType() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool outboundComPortPool = getEngineModelService().newOutboundComPortPool(NAME_BASIS + outboundComPortPoolIndex++, null, EXECUTION_TIMEOUT);
        outboundComPortPool.setDescription(DESCRIPTION);

        // Expecting TranslatableApplicationException because the ComPortType is not set
    }

    @Test
    @Transactional
    public void testLoad() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();

        // Business method
        OutboundComPortPool loaded = getEngineModelService().findOutboundComPortPool(comPortPool.getId()).get();

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
        comPortPool.update();

        OutboundComPortPool retrievedComPortPool = getEngineModelService().findOutboundComPortPool(comPortPool.getId()).get();

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
        OutboundComPort portA = onlineComServer.newOutboundComPort("portA", 1)
                .description("hello world")
                .active(true).comPortType(ComPortType.TCP)
                .add();
        OutboundComPort portB = onlineComServer.newOutboundComPort("portB", 1)
                .description("hello world")
                .active(true).comPortType(ComPortType.TCP)
                .add();
        OutboundComPort portC = onlineComServer.newOutboundComPort("portC", 1)
                .description("hello world")
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
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_PORT_POOL+"}", property = "name")
    public void testUpdateWithSameName() throws TranslatableApplicationException, SQLException {
        String name = NAME_BASIS + outboundComPortPoolIndex++;
        OutboundComPortPool outboundComPortPool = getEngineModelService().newOutboundComPortPool(name, COM_PORT_TYPE, EXECUTION_TIMEOUT);
        outboundComPortPool.setDescription(DESCRIPTION);
        outboundComPortPool.update();

        OutboundComPortPool duplicateComPortPool = getEngineModelService().newOutboundComPortPool(name, COM_PORT_TYPE, EXECUTION_TIMEOUT);
        duplicateComPortPool.setDescription(DESCRIPTION);
        duplicateComPortPool.update();

        // Expecting a DuplicateException
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testUpdateWithoutName() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();
        comPortPool.setName(null);

        // Business method
        comPortPool.update();

        // Expecting TranslatableApplicationException because the name is not set
    }

    @Test
    @Transactional
    public void testDelete() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();

        // Business method
        comPortPool.delete();

        assertThat(this.getEngineModelService().findOutboundComPortPool(comPortPool.getId()).isPresent()).isFalse();
    }

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

        ComPortPool reloaded = getEngineModelService().findOutboundComPortPool(comPortPoolId).get();

        // Asserts
        assertThat(reloaded.isObsolete()).isTrue();
        assertThat(reloaded.getObsoleteDate()).isNotNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_COMPORTPOOL_NO_UPDATE_ALLOWED+"}", property = "obsoleteDate")
    public void testUpdateAfterMakeObsolete() throws TranslatableApplicationException, SQLException {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();
        comPortPool.makeObsolete();

        // Business method
        comPortPool.update();

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

    /**
     * Created test for JP-5753
     */
    @Test
    @Transactional
    public void findUniqueComPortPoolByNameAfterDeleteTest() {
        OutboundComPortPool comPortPool = newOutboundComPortPoolWithoutViolations();
        String comPortPoolName = comPortPool.getName();

        comPortPool.makeObsolete();

        Optional<? extends ComPortPool> noComPortPoolForThisName = getEngineModelService().findComPortPoolByName(comPortPoolName);
        assertThat(noComPortPoolForThisName.isPresent()).isFalse();

        // creating the exact same comportpool
        OutboundComPortPool outboundComPortPool = getEngineModelService().newOutboundComPortPool(comPortPoolName, COM_PORT_TYPE, EXECUTION_TIMEOUT);
        outboundComPortPool.setDescription(DESCRIPTION);
        outboundComPortPool.update();

        Optional<? extends ComPortPool> newComPortPoolForName = getEngineModelService().findComPortPoolByName(comPortPoolName);
        assertThat(newComPortPoolForName.isPresent()).isTrue();
    }

    private int outboundComPortPoolIndex =1;
    private OutboundComPortPool newOutboundComPortPoolWithoutViolations() {
        OutboundComPortPool outboundComPortPool = getEngineModelService().newOutboundComPortPool(NAME_BASIS+outboundComPortPoolIndex++, COM_PORT_TYPE, EXECUTION_TIMEOUT);
        outboundComPortPool.setDescription(DESCRIPTION);
        outboundComPortPool.update();
        return outboundComPortPool;
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