package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.Expected;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.inject.Provider;
import org.junit.Test;
import org.mockito.Mock;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

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
        OutboundComPort comPort = new OutboundComPortImpl(dataModel, getEngineModelService());
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

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutName() throws BusinessException, SQLException {
        createOnlineComServer().newOutboundComPort()
        .description(DESCRIPTION)
        .active(ACTIVE)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .comPortType(COM_PORT_TYPE).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Expected(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutComPortType() throws BusinessException, SQLException {
        createOnlineComServer().newOutboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .comPortType(null)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XnotInAcceptableRange")
    public void testCreateWithZeroSimultaneousConnections() throws BusinessException, SQLException {
        createOnlineComServer().newOutboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .comPortType(COM_PORT_TYPE)
        .numberOfSimultaneousConnections(0).add();
        failBecauseExceptionWasNotThrown(TranslatableApplicationException.class);
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class, messageId = "XnotInAcceptableRange")
    public void testCreateWithTooManySimultaneousConnections() throws BusinessException, SQLException {
        createOnlineComServer().newOutboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .comPortType(COM_PORT_TYPE)
        .numberOfSimultaneousConnections(OutboundComPort.MAXIMUM_NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1).add();
        failBecauseExceptionWasNotThrown(TranslatableApplicationException.class);
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithExistingName() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        createSimpleComPort(onlineComServer);

        onlineComServer.newOutboundComPort()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
        .comPortType(COM_PORT_TYPE).add();

        // Expecting a BusinessException to be thrown because a ComPort with the same name already exists
    }

    @Test
    @Transactional
    public void testLoad() throws SQLException, BusinessException {
        OutboundComPort comPort = this.createSimpleComPort();
        OutboundComPort createdComPort = (OutboundComPort) getEngineModelService().findComPort(comPort.getId());

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
    
    private OutboundComPort createSimpleComPort() throws SQLException {
        return createSimpleComPort(createOnlineComServer());
    }

    private OutboundComPort createSimpleComPort(OnlineComServer comServer) throws SQLException {
        return comServer.newOutboundComPort()
                .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortType(ComPortType.SERIAL)
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .add();
    }


    

}