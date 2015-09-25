package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.BusinessException;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.ComPortPoolMember;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.inject.Provider;
import java.sql.SQLException;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the OutboundComPortImpl object
 *
 * @author gna
 * @since 3/04/12 - 13:55
 */
public class OutboundComPortImplTest extends PersistenceTest {

    private static final String COMPORT_NAME = "OutBoundComPort";
    private static final String DESCRIPTION = "Description for a new OutBound ComPort";
    private static final boolean ACTIVE = true;
    private static final int NUMBER_OF_SIMULTANEOUS_CONNECTIONS = 11;
    private static final ComPortType COM_PORT_TYPE = ComPortType.SERIAL;


    @Mock
    DataModel dataModel;
    @Mock
    Provider<ComPortPoolMember> comPortPoolMemberProvider;

    @Test
    public void outBoundTest() {
        OutboundComPort comPort = new OutboundComPortImpl(dataModel, getEngineModelService(), null);
        assertThat(comPort.isInbound()).isFalse();
    }

    @Test
    @Transactional
    public void testCreateWithoutViolations() throws BusinessException, SQLException {
        OutboundComPort comPort = this.createSimpleComPort();

        // Asserts
        assertThat(comPort.getName()).isEqualTo(COMPORT_NAME);
        assertThat(comPort.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(comPort.isActive()).isTrue();
        assertThat(comPort.getNumberOfSimultaneousConnections()).isEqualTo(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        assertThat(comPort.getComPortType()).isEqualTo(COM_PORT_TYPE);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    @Transactional
    public void testCreateWithoutName() throws BusinessException, SQLException {
        createOnlineComServer().newOutboundComPort(null, NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .comPortType(COM_PORT_TYPE).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "type")
    @Transactional
    public void testCreateWithoutComPortType() throws BusinessException, SQLException {
        createOnlineComServer().newOutboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .comPortType(null)
        .add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE+"}", property = "numberOfSimultaneousConnections")
    public void testCreateWithZeroSimultaneousConnections() throws BusinessException, SQLException {
        createOnlineComServer().newOutboundComPort(COMPORT_NAME, 0)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .comPortType(COM_PORT_TYPE)
        .add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE+"}", property = "numberOfSimultaneousConnections")
    public void testCreateWithTooManySimultaneousConnections() throws BusinessException, SQLException {
        createOnlineComServer().newOutboundComPort(COMPORT_NAME, OutboundComPort.MAXIMUM_NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .comPortType(COM_PORT_TYPE)
        .add();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_PORT+"}", property="name")
    @Transactional
    public void testCreateWithExistingName() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        createSimpleComPort(onlineComServer);

        onlineComServer.newOutboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .comPortType(COM_PORT_TYPE).add();

        // Expecting a BusinessException to be thrown because a ComPort with the same name already exists
    }

    @Test
    @Transactional
    public void testLoad() throws SQLException, BusinessException {
        OutboundComPort comPort = this.createSimpleComPort();
        OutboundComPort createdComPort = (OutboundComPort) getEngineModelService().findComPort(comPort.getId()).get();

        // Asserts
        assertThat(createdComPort.getName()).isEqualTo(COMPORT_NAME);
        assertThat(createdComPort.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(createdComPort.isActive()).isTrue();
        assertThat(createdComPort.getNumberOfSimultaneousConnections()).isEqualTo(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        assertThat(createdComPort.getComPortType()).isEqualTo(COM_PORT_TYPE);
    }

    @Test
    @Transactional
    public void updateWithoutViolations() throws BusinessException, SQLException {
        final int newNumberOfSimultaneousConnections = 99;
        final ComPortType newType = ComPortType.UDP;
        final String newName = "NewComPortName";
        final String newDescription = "NewDescriptionForUpdatedModemBasedInboundComPortImpl";
        final boolean newActive = false;

        OutboundComPortImpl comPort = (OutboundComPortImpl) this.createSimpleComPort();

        comPort.setName(newName);
        comPort.setDescription(newDescription);
        comPort.setActive(newActive);
        comPort.setNumberOfSimultaneousConnections(newNumberOfSimultaneousConnections);

        comPort.save();

        // Asserts
        assertThat(comPort.getName()).isEqualTo(newName);
        assertThat(comPort.getDescription()).isEqualTo(newDescription);
        assertThat(comPort.isActive()).isEqualTo(newActive);
        assertThat(comPort.getNumberOfSimultaneousConnections()).isEqualTo(newNumberOfSimultaneousConnections);
    }

    private int onlineNameNumber=1;
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
        return onlineComServerBuilder.create();
    }

    private OutboundComPort createSimpleComPort() throws SQLException {
        return createSimpleComPort(createOnlineComServer());
    }

    private OutboundComPort createSimpleComPort(OnlineComServer comServer) throws SQLException {
        return comServer.newOutboundComPort(COMPORT_NAME, NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortType(ComPortType.SERIAL)
                .add();
    }




}