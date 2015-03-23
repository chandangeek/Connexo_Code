package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.Expected;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OfflineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.protocol.api.ComPortType;
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

    private static int nextComPortId = 1;

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
    public void testCreateWithoutComPortsWithoutViolations () throws BusinessException, SQLException {
        String name = NO_VIOLATIONS_NAME;
        OfflineComServer comServer = getEngineModelService().newOfflineComServerInstance();
        comServer.setName(name);
        comServer.setActive(true);
        comServer.setServerLogLevel(SERVER_LOG_LEVEL);
        comServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        comServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        // Business method
        comServer.save();

        // Asserts
        assertThat(name).isEqualTo(comServer.getName());
        assertThat(comServer.isActive()).isTrue();
        assertThat(SERVER_LOG_LEVEL).isEqualTo(comServer.getServerLogLevel());
        assertThat(COMMUNICATION_LOG_LEVEL).isEqualTo(comServer.getCommunicationLogLevel());
        assertThat(CHANGES_INTER_POLL_DELAY).isEqualTo(comServer.getChangesInterPollDelay());
        assertThat(SCHEDULING_INTER_POLL_DELAY).isEqualTo(comServer.getSchedulingInterPollDelay());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation( messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "changesInterPollDelay")
    public void testTooSmallChangesInterPollDelay () throws BusinessException, SQLException {
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        offlineComServer.setName("testTooSmallChangesInterPollDelay");
        offlineComServer.setActive(true);
        offlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.setChangesInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        offlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        offlineComServer.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation( messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "schedulingInterPollDelay")
    public void testTooSmallSchedulingInterPollDelay () throws BusinessException, SQLException {
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        offlineComServer.setName("testTooSmallSchedulingInterPollDelay");
        offlineComServer.setActive(true);
        offlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.setSchedulingInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));

        offlineComServer.save();
    }

    @Test
    @Transactional
    public void loadTest() throws BusinessException, SQLException {
        String name = NO_VIOLATIONS_NAME;
        OfflineComServer comServer = getEngineModelService().newOfflineComServerInstance();
        comServer.setName(name);
        comServer.setActive(true);
        comServer.setServerLogLevel(SERVER_LOG_LEVEL);
        comServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        comServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        // Business method
        comServer.save();
        ComServer loadedOfflineServer = getEngineModelService().findComServer(comServer.getId()).get();

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
    public void testCreateWithComPortWithoutViolations () throws BusinessException, SQLException {
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        String name = "With-ComPorts";
        offlineComServer.setName(name);
        offlineComServer.setActive(true);
        offlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        addComPort(offlineComServer);

        // Business method
        offlineComServer.save();

        ComServer comServer = getEngineModelService().findComServer(name).get();
        assertThat(comServer.getComPorts()).isNotEmpty();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCreateWithoutName () throws BusinessException, SQLException {
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        offlineComServer.setActive(true);
        offlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        offlineComServer.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "serverLogLevel")
    public void testCreateWithoutServerLogLevel () throws BusinessException, SQLException {
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        String name = "No-Server-LogLevel";
        offlineComServer.setName(name);
        offlineComServer.setActive(true);
        offlineComServer.setServerLogLevel(null);
        offlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        offlineComServer.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "communicationLogLevel")
    public void testCreateWithoutCommunicationLogLevel () throws BusinessException, SQLException {
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        String name = "No-Communication-LogLevel";
        offlineComServer.setName(name);
        offlineComServer.setActive(true);
        offlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.setCommunicationLogLevel(null);
        offlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        offlineComServer.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "changesInterPollDelay")
    public void testCreateWithoutChangesInterPollDelay () throws BusinessException, SQLException {
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        String name = "No-Changes-InterpollDelay";
        offlineComServer.setName(name);
        offlineComServer.setActive(true);
        offlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.setChangesInterPollDelay(null);
        offlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        offlineComServer.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation( messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "schedulingInterPollDelay")
    public void testCreateWithoutSchedulingInterPollDelay () throws BusinessException, SQLException {
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        String name = "No-Scheduling-InterpollDelay";
        offlineComServer.setName(name);
        offlineComServer.setActive(true);
        offlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.setSchedulingInterPollDelay(null);

        offlineComServer.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_SERVER+"}", property = "name")
    public void testCreateWithExistingName () throws BusinessException, SQLException {
        String serverName = "Candidate-for-duplicate";
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        offlineComServer.setName(serverName);
        offlineComServer.setActive(true);
        offlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        offlineComServer.save();

        OfflineComServer duplicateComServer = getEngineModelService().newOfflineComServerInstance();
        duplicateComServer.setName(serverName);
        duplicateComServer.setActive(false);
        duplicateComServer.setServerLogLevel(ComServer.LogLevel.TRACE);
        duplicateComServer.setCommunicationLogLevel(ComServer.LogLevel.TRACE);
        duplicateComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        duplicateComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        duplicateComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        duplicateComServer.save();
    }

    @Test
    @Transactional
    public void testCreateWithExistingButDeletedName () throws BusinessException, SQLException {
        String serverName = "Candidate-for-duplication";
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        offlineComServer.setName(serverName);
        offlineComServer.setActive(true);
        offlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        offlineComServer.save();
        offlineComServer.delete();

        OfflineComServer recreatedComServer = getEngineModelService().newOfflineComServerInstance();
        recreatedComServer.setName(serverName);
        recreatedComServer.setActive(false);
        recreatedComServer.setServerLogLevel(ComServer.LogLevel.TRACE);
        recreatedComServer.setCommunicationLogLevel(ComServer.LogLevel.TRACE);
        recreatedComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        recreatedComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        recreatedComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        recreatedComServer.save();
    }

    @Test
    @Transactional
    public void testUpdateWithoutComPort () throws BusinessException, SQLException {
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        String name = "Update-Candidate";
        offlineComServer.setName(name);
        offlineComServer.setActive(true);
        offlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        offlineComServer.save();

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
        retrievedComServer.save();

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
    public void testUpdateWithUpdatesToComPort () throws BusinessException, SQLException {
        OfflineComServer creationShadow = getEngineModelService().newOfflineComServerInstance();
        String name = "Update-Candidate2";
        creationShadow.setName(name);
        creationShadow.setActive(true);
        creationShadow.setServerLogLevel(SERVER_LOG_LEVEL);
        creationShadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        creationShadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        creationShadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        this.addComPort(creationShadow);
        creationShadow.save();
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
        retrievedComServer.save();

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
    public void testUpdateWithoutUpdatesToComPort () throws BusinessException, SQLException {
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        String name = "Update-Candidate3";
        offlineComServer.setName(name);
        offlineComServer.setActive(true);
        offlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        offlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        offlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        offlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        addComPort(offlineComServer);
        offlineComServer.save();
        OfflineComServer comServer = (OfflineComServer) getEngineModelService().findComServer(name).get();

        // Business method
        String changedName = "Name-Updated3";
        ComServer.LogLevel changedServerLogLevel = ComServer.LogLevel.WARN;
        ComServer.LogLevel changedComLogLevel = ComServer.LogLevel.INFO;
        TimeDuration changedChangesInterPollDelay = SCHEDULING_INTER_POLL_DELAY;
        TimeDuration changedSchedulingInterPollDelay = CHANGES_INTER_POLL_DELAY;
        comServer.setName(changedName);
        comServer.setActive(false);
        comServer.setServerLogLevel(changedServerLogLevel);
        comServer.setCommunicationLogLevel(changedComLogLevel);
        comServer.setChangesInterPollDelay(changedChangesInterPollDelay);
        comServer.setSchedulingInterPollDelay(changedSchedulingInterPollDelay);
        comServer.save();

        // Asserts
        assertThat(changedName).isEqualTo(comServer.getName());
        assertThat(comServer.isActive()).isFalse();
        assertThat(changedServerLogLevel).isEqualTo(comServer.getServerLogLevel());
        assertThat(changedComLogLevel).isEqualTo(comServer.getCommunicationLogLevel());
        assertThat(changedChangesInterPollDelay).isEqualTo(comServer.getChangesInterPollDelay());
        assertThat(changedSchedulingInterPollDelay).isEqualTo(comServer.getSchedulingInterPollDelay());
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithComPortsWithoutViolations () throws BusinessException, SQLException {
        OfflineComServer comServer = getEngineModelService().newOfflineComServerInstance();
        String name = "testMakeObsoleteWithComPortsWithoutViolations";
        comServer.setName(name);
        comServer.setActive(true);
        comServer.setServerLogLevel(SERVER_LOG_LEVEL);
        comServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        comServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        addComPort(comServer);
        comServer.save();
        long id = comServer.getId();
        List<OutboundComPort> comPorts = comServer.getOutboundComPorts();

        // Business method
        comServer.makeObsolete();

        // Asserts
        assertThat(getEngineModelService().findComServer(id).isPresent()).isTrue();
        for (OutboundComPort outbound : comPorts) {
            assertThat(outbound.isObsolete());
        }
    }

    @Test
    @Transactional
    public void testMakeObsolete () throws BusinessException, SQLException {
        String name = "testMakeObsolete";
        OfflineComServer comServer = null;
        comServer = getEngineModelService().newOfflineComServerInstance();
        comServer.setName(name);
        comServer.setActive(true);
        comServer.setServerLogLevel(SERVER_LOG_LEVEL);
        comServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        comServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        addComPort(comServer);
        comServer.save();
        comServer.makeObsolete();

        // Business method
        ComServer deletedComServer = getEngineModelService().findComServer(comServer.getId()).get();

        // Asserts
        assertTrue("DeleteDate should be filled in", comServer.getObsoleteDate() != null);
        assertTrue("Should be marked for delete", comServer.isObsolete());

        assertNotNull("DeleteDate should be filled in", deletedComServer.getObsoleteDate());
        assertTrue("Should be marked for delete", deletedComServer.isObsolete());
        assertTrue("toString() representation should contain '(Deleted on ...)'", deletedComServer.toString().contains("delete"));
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_COMSERVER_NO_UPDATE_ALLOWED+"}")
    public void testUpdateAfterMakeObsolete() throws BusinessException, SQLException {
        OfflineComServer comServer = getEngineModelService().newOfflineComServerInstance();
        String name = "testUpdateAfterMakeObsolete";
        comServer.setName(name);
        comServer.setActive(true);
        comServer.setServerLogLevel(SERVER_LOG_LEVEL);
        comServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        comServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        addComPort(comServer);

        // Business method
        comServer.save();

        // Business method
        comServer.makeObsolete();
        comServer.save();
    }

    @Test
    @Transactional
    @Expected(expected = TranslatableApplicationException.class)
    public void testMakeObsoleteTwice () throws BusinessException, SQLException {
        OfflineComServer comServer = getEngineModelService().newOfflineComServerInstance();
        String name = "testMakeObsoleteTwice";
        comServer.setName(name);
        comServer.setActive(true);
        comServer.setServerLogLevel(SERVER_LOG_LEVEL);
        comServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        comServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        comServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        addComPort(comServer);

        comServer.save();
        comServer.makeObsolete();

        comServer.makeObsolete();
    }

    private void addComPort(ComServer comServer) {
        comServer.newOutboundComPort("Outbound", 1).active(true).comPortType(ComPortType.TCP).add();
    }

}