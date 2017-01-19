package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.Expected;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OfflineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.ports.ComPortType;
import com.google.inject.Provider;
import org.junit.Test;
import org.mockito.Mock;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
* Tests the {@link OfflineComServerImpl} component.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-03-30 (08:41)
*/
public class OfflineComServerImplTest extends PersistenceTest {

    private static final ComServer.LogLevel SERVER_LOG_LEVEL = ComServer.LogLevel.ERROR;
    private static final ComServer.LogLevel COMMUNICATION_LOG_LEVEL = ComServer.LogLevel.TRACE;
    private static final TimeDuration CHANGES_INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.TimeUnit.HOURS);
    private static final TimeDuration SCHEDULING_INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.TimeUnit.MINUTES);
    private static final String NO_VIOLATIONS_NAME = "Offline-No-Violations";

    @Mock
    DataModel dataModel;
    @Mock
    Provider<OutboundComPortImpl> outboundComPortProvider;


    @Test
    @Transactional
    public void testCreateWithoutComPortsWithoutViolations () throws SQLException {
        String name = NO_VIOLATIONS_NAME;
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> comServer = getEngineModelService().newOfflineComServerBuilder();
        comServer.name(name);
        comServer.active(true);
        comServer.serverLogLevel(SERVER_LOG_LEVEL);
        comServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        comServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        // Business method
        final OfflineComServer offlineComServer = comServer.create();

        // Asserts
        assertThat(name).isEqualTo(offlineComServer.getName());
        assertThat(offlineComServer.isActive()).isTrue();
        assertThat(SERVER_LOG_LEVEL).isEqualTo(offlineComServer.getServerLogLevel());
        assertThat(COMMUNICATION_LOG_LEVEL).isEqualTo(offlineComServer.getCommunicationLogLevel());
        assertThat(CHANGES_INTER_POLL_DELAY).isEqualTo(offlineComServer.getChangesInterPollDelay());
        assertThat(SCHEDULING_INTER_POLL_DELAY).isEqualTo(offlineComServer.getSchedulingInterPollDelay());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation( messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "changesInterPollDelay")
    public void testTooSmallChangesInterPollDelay () throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> comServer = getEngineModelService().newOfflineComServerBuilder();
        comServer.name("testTooSmallChangesInterPollDelay");
        comServer.active(true);
        comServer.serverLogLevel(SERVER_LOG_LEVEL);
        comServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.changesInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        comServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        comServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation( messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "schedulingInterPollDelay")
    public void testTooSmallSchedulingInterPollDelay () throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> comServer = getEngineModelService().newOfflineComServerBuilder();
        comServer.name("testTooSmallSchedulingInterPollDelay");
        comServer.active(true);
        comServer.serverLogLevel(SERVER_LOG_LEVEL);
        comServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        comServer.schedulingInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));

        comServer.create();
    }

    @Test
    @Transactional
    public void loadTest() throws SQLException {
        String name = NO_VIOLATIONS_NAME;
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> comServer = getEngineModelService().newOfflineComServerBuilder();
        comServer.name(name);
        comServer.active(true);
        comServer.serverLogLevel(SERVER_LOG_LEVEL);
        comServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        comServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        // Business method
        final OfflineComServer offlineComServer = comServer.create();
        ComServer loadedOfflineServer = getEngineModelService().findComServer(offlineComServer.getId()).get();

        // asserts
        assertThat(name).isEqualTo(loadedOfflineServer.getName());
        assertThat(loadedOfflineServer.isActive()).isTrue();
        assertThat(SERVER_LOG_LEVEL).isEqualTo(loadedOfflineServer.getServerLogLevel());
        assertThat(COMMUNICATION_LOG_LEVEL).isEqualTo(loadedOfflineServer.getCommunicationLogLevel());
        assertThat(CHANGES_INTER_POLL_DELAY).isEqualTo(loadedOfflineServer.getChangesInterPollDelay());
        assertThat(SCHEDULING_INTER_POLL_DELAY).isEqualTo(loadedOfflineServer.getSchedulingInterPollDelay());
    }

    @Test
    @Transactional
    public void testCreateWithComPortWithoutViolations () throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> offlineComServer = getEngineModelService().newOfflineComServerBuilder();
        String name = "With-ComPorts";
        offlineComServer.name(name);
        offlineComServer.active(true);
        offlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        final OfflineComServer comServer = offlineComServer.create();
        addComPort(comServer);

        ComServer reloadedComServer = getEngineModelService().findComServer(name).get();
        assertThat(reloadedComServer.getComPorts()).isNotEmpty();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCreateWithoutName () throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> offlineComServer = getEngineModelService().newOfflineComServerBuilder();
        offlineComServer.active(true);
        offlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        offlineComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "serverLogLevel")
    public void testCreateWithoutServerLogLevel () throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> offlineComServer = getEngineModelService().newOfflineComServerBuilder();
        String name = "No-Server-LogLevel";
        offlineComServer.name(name);
        offlineComServer.active(true);
        offlineComServer.serverLogLevel(null);
        offlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        offlineComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "communicationLogLevel")
    public void testCreateWithoutCommunicationLogLevel () throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> offlineComServer = getEngineModelService().newOfflineComServerBuilder();
        String name = "No-Communication-LogLevel";
        offlineComServer.name(name);
        offlineComServer.active(true);
        offlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.communicationLogLevel(null);
        offlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        offlineComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "changesInterPollDelay")
    public void testCreateWithoutChangesInterPollDelay () throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> offlineComServer = getEngineModelService().newOfflineComServerBuilder();
        String name = "No-Changes-InterpollDelay";
        offlineComServer.name(name);
        offlineComServer.active(true);
        offlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.changesInterPollDelay(null);
        offlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        offlineComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation( messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "schedulingInterPollDelay")
    public void testCreateWithoutSchedulingInterPollDelay () throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> offlineComServer = getEngineModelService().newOfflineComServerBuilder();
        String name = "No-Scheduling-InterpollDelay";
        offlineComServer.name(name);
        offlineComServer.active(true);
        offlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.schedulingInterPollDelay(null);

        offlineComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_SERVER+"}", property = "name")
    public void testCreateWithExistingName () throws SQLException {
        String serverName = "Candidate-for-duplicate";
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> offlineComServer = getEngineModelService().newOfflineComServerBuilder();
        offlineComServer.name(serverName);
        offlineComServer.active(true);
        offlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        offlineComServer.create();

        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> duplicateComServer = getEngineModelService().newOfflineComServerBuilder();
        duplicateComServer.name(serverName);
        duplicateComServer.active(false);
        duplicateComServer.serverLogLevel(SERVER_LOG_LEVEL);
        duplicateComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        duplicateComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        duplicateComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        duplicateComServer.create();
    }

    @Test
    @Transactional
    public void testCreateWithExistingButDeletedName () throws SQLException {
        String serverName = "Candidate-for-duplication";
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> offlineComServerBuilder = getEngineModelService().newOfflineComServerBuilder();
        offlineComServerBuilder.name(serverName);
        offlineComServerBuilder.active(true);
        offlineComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        offlineComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        final OfflineComServer offlineComServer1 = offlineComServerBuilder.create();
        offlineComServer1.delete();

        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> recreatedComServerBuilder = getEngineModelService().newOfflineComServerBuilder();
        recreatedComServerBuilder.name(serverName);
        recreatedComServerBuilder.active(true);
        recreatedComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        recreatedComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        recreatedComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        recreatedComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        recreatedComServerBuilder.create();
    }

    @Test
    @Transactional
    public void testUpdateWithoutComPort () throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> offlineComServer = getEngineModelService().newOfflineComServerBuilder();
        String name = "Update-Candidate";
        offlineComServer.name(name);
        offlineComServer.active(true);
        offlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        offlineComServer.create();

        OfflineComServer retrievedComServer = (OfflineComServer) getEngineModelService().findComServer(name).get();

        // Business method
        String changedName = "Name-Updated";
        ComServer.LogLevel changedServerLogLevel = ComServer.LogLevel.WARN;
        ComServer.LogLevel changedComLogLevel = ComServer.LogLevel.INFO;
        TimeDuration changedChangesInterPollDelay = SCHEDULING_INTER_POLL_DELAY;
        TimeDuration changedSchedulingInterPollDelay = CHANGES_INTER_POLL_DELAY;
        retrievedComServer.setName(changedName);
        retrievedComServer.setActive(false);
        retrievedComServer.setServerLogLevel(changedServerLogLevel);
        retrievedComServer.setCommunicationLogLevel(changedComLogLevel);
        retrievedComServer.setChangesInterPollDelay(changedChangesInterPollDelay);
        retrievedComServer.setSchedulingInterPollDelay(changedSchedulingInterPollDelay);
        retrievedComServer.update();

        // Asserts
        assertThat(changedName).isEqualTo(retrievedComServer.getName());
        assertThat(retrievedComServer.isActive()).isFalse();
        assertThat(changedServerLogLevel).isEqualTo(retrievedComServer.getServerLogLevel());
        assertThat(changedComLogLevel).isEqualTo(retrievedComServer.getCommunicationLogLevel());
        assertThat(changedChangesInterPollDelay).isEqualTo(retrievedComServer.getChangesInterPollDelay());
        assertThat(changedSchedulingInterPollDelay).isEqualTo(retrievedComServer.getSchedulingInterPollDelay());
    }

    @Test
    @Transactional
    public void testUpdateWithUpdatesToComPort () throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> creationShadow = getEngineModelService().newOfflineComServerBuilder();
        String name = "Update-Candidate2";
        creationShadow.name(name);
        creationShadow.active(true);
        creationShadow.serverLogLevel(SERVER_LOG_LEVEL);
        creationShadow.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        creationShadow.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        creationShadow.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        final OfflineComServer comServer = creationShadow.create();
        this.addComPort(comServer);

        OfflineComServer retrievedComServer = (OfflineComServer) getEngineModelService().findComServer(name).get();

        // Business method
        String changedName = "Name-Updated2";
        ComServer.LogLevel changedServerLogLevel = ComServer.LogLevel.WARN;
        ComServer.LogLevel changedComLogLevel = ComServer.LogLevel.INFO;
        TimeDuration changedChangesInterPollDelay = SCHEDULING_INTER_POLL_DELAY;
        TimeDuration changedSchedulingInterPollDelay = CHANGES_INTER_POLL_DELAY;
        retrievedComServer.setName(changedName);
        retrievedComServer.setActive(false);
        retrievedComServer.setServerLogLevel(changedServerLogLevel);
        retrievedComServer.setCommunicationLogLevel(changedComLogLevel);
        retrievedComServer.setChangesInterPollDelay(changedChangesInterPollDelay);
        retrievedComServer.setSchedulingInterPollDelay(changedSchedulingInterPollDelay);
        ComPort comPort = retrievedComServer.getComPorts().get(0);
        comPort.setName("Updated");
        retrievedComServer.update();

        // Asserts
        assertThat(changedName).isEqualTo(retrievedComServer.getName());
        assertThat(retrievedComServer.isActive()).isFalse();
        assertThat(changedServerLogLevel).isEqualTo(retrievedComServer.getServerLogLevel());
        assertThat(changedComLogLevel).isEqualTo(retrievedComServer.getCommunicationLogLevel());
        assertThat(changedChangesInterPollDelay).isEqualTo(retrievedComServer.getChangesInterPollDelay());
        assertThat(changedSchedulingInterPollDelay).isEqualTo(retrievedComServer.getSchedulingInterPollDelay());
    }

    @Test
    @Transactional
    public void testUpdateWithoutUpdatesToComPort () throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> offlineComServer = getEngineModelService().newOfflineComServerBuilder();
        String name = "Update-Candidate3";
        offlineComServer.name(name);
        offlineComServer.active(true);
        offlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        final OfflineComServer comServer = offlineComServer.create();
        addComPort(comServer);
        OfflineComServer reloadedComServer = (OfflineComServer) getEngineModelService().findComServer(name).get();

        // Business method
        String changedName = "Name-Updated3";
        ComServer.LogLevel changedServerLogLevel = ComServer.LogLevel.WARN;
        ComServer.LogLevel changedComLogLevel = ComServer.LogLevel.INFO;
        TimeDuration changedChangesInterPollDelay = SCHEDULING_INTER_POLL_DELAY;
        TimeDuration changedSchedulingInterPollDelay = CHANGES_INTER_POLL_DELAY;
        reloadedComServer.setName(changedName);
        reloadedComServer.setActive(false);
        reloadedComServer.setServerLogLevel(changedServerLogLevel);
        reloadedComServer.setCommunicationLogLevel(changedComLogLevel);
        reloadedComServer.setChangesInterPollDelay(changedChangesInterPollDelay);
        reloadedComServer.setSchedulingInterPollDelay(changedSchedulingInterPollDelay);
        reloadedComServer.update();

        // Asserts
        assertThat(changedName).isEqualTo(reloadedComServer.getName());
        assertThat(reloadedComServer.isActive()).isFalse();
        assertThat(changedServerLogLevel).isEqualTo(reloadedComServer.getServerLogLevel());
        assertThat(changedComLogLevel).isEqualTo(reloadedComServer.getCommunicationLogLevel());
        assertThat(changedChangesInterPollDelay).isEqualTo(reloadedComServer.getChangesInterPollDelay());
        assertThat(changedSchedulingInterPollDelay).isEqualTo(reloadedComServer.getSchedulingInterPollDelay());
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithComPortsWithoutViolations () throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> comServer = getEngineModelService().newOfflineComServerBuilder();
        String name = "testMakeObsoleteWithComPortsWithoutViolations";
        comServer.name(name);
        comServer.active(true);
        comServer.serverLogLevel(SERVER_LOG_LEVEL);
        comServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        comServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        final OfflineComServer offlineComServer = comServer.create();
        addComPort(offlineComServer);
        long id = offlineComServer.getId();
        List<OutboundComPort> comPorts = offlineComServer.getOutboundComPorts();

        // Business method
        offlineComServer.makeObsolete();

        // Asserts
        assertThat(getEngineModelService().findComServer(id).isPresent()).isTrue();
        for (OutboundComPort outbound : comPorts) {
            assertThat(outbound.isObsolete());
        }
    }

    @Test
    @Transactional
    public void testMakeObsolete () throws SQLException {
        String name = "testMakeObsolete";
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> comServer = getEngineModelService().newOfflineComServerBuilder();
        comServer.name(name);
        comServer.active(true);
        comServer.serverLogLevel(SERVER_LOG_LEVEL);
        comServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        comServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        final OfflineComServer offlineComServer = comServer.create();
        addComPort(offlineComServer);
        offlineComServer.makeObsolete();

        // Business method
        ComServer deletedComServer = getEngineModelService().findComServer(offlineComServer.getId()).get();

        // Asserts
        assertTrue("DeleteDate should be filled in", offlineComServer.getObsoleteDate() != null);
        assertTrue("Should be marked for delete", offlineComServer.isObsolete());

        assertNotNull("DeleteDate should be filled in", deletedComServer.getObsoleteDate());
        assertTrue("Should be marked for delete", deletedComServer.isObsolete());
        assertTrue("toString() representation should contain '(Deleted on ...)'", deletedComServer.toString().contains("delete"));
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_COMSERVER_NO_UPDATE_ALLOWED+"}")
    public void testUpdateAfterMakeObsolete() throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> comServer = getEngineModelService().newOfflineComServerBuilder();
        String name = "testUpdateAfterMakeObsolete";
        comServer.name(name);
        comServer.active(true);
        comServer.serverLogLevel(SERVER_LOG_LEVEL);
        comServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        comServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        final OfflineComServer offlineComServer = comServer.create();
        addComPort(offlineComServer);

        // Business method
        offlineComServer.update();

        // Business method
        offlineComServer.makeObsolete();
        offlineComServer.update();
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class)
    public void testMakeObsoleteTwice () throws SQLException {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> comServer = getEngineModelService().newOfflineComServerBuilder();
        String name = "testMakeObsoleteTwice";
        comServer.name(name);
        comServer.active(true);
        comServer.serverLogLevel(SERVER_LOG_LEVEL);
        comServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        comServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        final OfflineComServer offlineComServer = comServer.create();
        addComPort(offlineComServer);

        offlineComServer.update();
        offlineComServer.makeObsolete();

        offlineComServer.makeObsolete();
    }

    private void addComPort(ComServer comServer) {
        comServer.newOutboundComPort("Outbound", 1).active(true).comPortType(ComPortType.TCP).add();
    }

}