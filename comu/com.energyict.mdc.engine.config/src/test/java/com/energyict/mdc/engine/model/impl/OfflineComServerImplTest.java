//package com.energyict.mdc.engine.model.impl;
//
//import com.elster.jupiter.orm.DataModel;
//import com.energyict.mdc.common.BusinessException;
//import com.energyict.mdc.common.InvalidValueException;
//import com.energyict.mdc.common.TimeDuration;
//import com.energyict.mdc.engine.model.ComServer;
//import com.energyict.mdc.engine.model.EngineModelService;
//import com.energyict.mdc.engine.model.OfflineComServer;
//import com.energyict.mdc.engine.model.OutboundComPort;
//import com.energyict.mdc.engine.model.ComPort;
//import com.energyict.mdc.engine.model.PersistenceTest;
//import com.energyict.mdc.protocol.api.ComPortType;
//import com.google.inject.Provider;
//import org.junit.Test;
//import org.mockito.Mock;
//
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
///**
// * Tests the {@link OfflineComServerImpl} component.
// *
// * @author Rudi Vankeirsbilck (rudi)
// * @since 2012-03-30 (08:41)
// */
//public class OfflineComServerImplTest extends PersistenceTest {
//
//    private static int nextComPortId = 1;
//
//    private static final ComServer.LogLevel SERVER_LOG_LEVEL = ComServer.LogLevel.ERROR;
//    private static final ComServer.LogLevel COMMUNICATION_LOG_LEVEL = ComServer.LogLevel.TRACE;
//    private static final TimeDuration CHANGES_INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.HOURS);
//    private static final TimeDuration SCHEDULING_INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.MINUTES);
//    private static final String NO_VIOLATIONS_NAME = "Offline-No-Violations";
//
//    @Mock
//    EngineModelService engineModelService;
//    @Mock
//    DataModel dataModel;
//    @Mock
//    Provider<OutboundComPortImpl> outboundComPortProvider;
//
//    @Test
//    public void testGetTypeDoesNotReturnServerBasedClassName () {
//        OfflineComServer onlineComServer = new OfflineComServerImpl(dataModel, engineModelService, outboundComPortProvider);
//
//        // Business method
//        String type = onlineComServer.getType();
//
//        // Asserts
//        assertThat(type).doesNotContain(".Server");
//    }
//
//    @Test
//    public void testCreateWithoutComPortsWithoutViolations () throws BusinessException, SQLException {
//        String name = NO_VIOLATIONS_NAME;
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        OfflineComServer comServer = new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//
//        // Asserts
//        assertThat(name).isEqualTo(comServer.getName());
//        assertThat(comServer.isActive()).isTrue();
//        assertThat(SERVER_LOG_LEVEL).isEqualTo(comServer.getServerLogLevel());
//        assertThat(COMMUNICATION_LOG_LEVEL).isEqualTo(comServer.getCommunicationLogLevel());
//        assertThat(CHANGES_INTER_POLL_DELAY).isEqualTo(comServer.getChangesInterPollDelay());
//        assertThat(SCHEDULING_INTER_POLL_DELAY).isEqualTo(comServer.getSchedulingInterPollDelay());
//    }
//
//    @Test(expected = BusinessException.class)
//    public void testTooSmallChangesInterPollDelay () throws BusinessException, SQLException {
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        shadow.setName("testTooSmallChangesInterPollDelay");
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(new TimeDuration(1, TimeDuration.SECONDS));
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        try {
//            // Business method
//            new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//        }
//        catch (BusinessException e) {
//            // Asserts
//            assertThat("XshouldBeAtLeast").isEqualTo(e.getMessageId());
//            throw e;
//        }
//    }
//
//    @Test(expected = BusinessException.class)
//    public void testTooSmallSchedulingInterPollDelay () throws BusinessException, SQLException {
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        shadow.setName("testTooSmallSchedulingInterPollDelay");
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(new TimeDuration(1, TimeDuration.SECONDS));
//
//        // Business method
//        try {
//            // Business method
//            new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//        }
//        catch (BusinessException e) {
//            // Asserts
//            assertThat("XshouldBeAtLeast").isEqualTo(e.getMessageId());
//            throw e;
//        }
//    }
//
//    @Test
//    public void loadTest() throws BusinessException, SQLException {
//        String name = NO_VIOLATIONS_NAME;
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        OfflineComServer createdComServer = new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//        OfflineComServer loadedOfflineServer = (OfflineComServer) new ComServerFactoryImpl(engineModelService).find((int) createdComServer.getId());
//
//        // asserts
//        assertThat(name).isEqualTo(loadedOfflineServer.getName());
//        assertThat(loadedOfflineServer.isActive()).isTrue();
//        assertThat(SERVER_LOG_LEVEL).isEqualTo(loadedOfflineServer.getServerLogLevel());
//        assertThat(COMMUNICATION_LOG_LEVEL).isEqualTo(loadedOfflineServer.getCommunicationLogLevel());
//        assertThat(CHANGES_INTER_POLL_DELAY).isEqualTo(loadedOfflineServer.getChangesInterPollDelay());
//        assertThat(SCHEDULING_INTER_POLL_DELAY).isEqualTo(loadedOfflineServer.getSchedulingInterPollDelay());
//    }
//
//    @Test
//    public void testCreateWithComPortWithoutViolations () throws BusinessException, SQLException {
//        this.injectFactories();
//
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        String name = "With-ComPorts";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.addOutboundComPort(this.outboundComPortShadow());
//
//        // Business method
//        OfflineComServer comServer = new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//
//        // Asserts
//        assertThat(comServer.getOutboundComPorts()).isNotEmpty();
//    }
//
//    @Test(expected = BusinessException.class)
//    public void testCreateWithoutName () throws BusinessException, SQLException {
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//
//        // Expecting a BusinessException to be thrown because the name is not set
//    }
//
//    @Test(expected = InvalidValueException.class)
//    public void testCreateWithoutServerLogLevel () throws BusinessException, SQLException {
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        String name = "No-Server-LogLevel";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(null);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//
//        // Expecting a BusinessException to be thrown because the server log level is not set
//    }
//
//    @Test(expected = InvalidValueException.class)
//    public void testCreateWithoutCommunicationLogLevel () throws BusinessException, SQLException {
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        String name = "No-Communication-LogLevel";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(null);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//
//        // Expecting a BusinessException to be thrown because the communication log level is not set
//    }
//
//    @Test(expected = InvalidValueException.class)
//    public void testCreateWithoutChangesInterPollDelay () throws BusinessException, SQLException {
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        String name = "No-Changes-InterpollDelay";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(null);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//
//        // Expecting a BusinessException to be thrown because the changes interpoll delay is not set
//    }
//
//    @Test(expected = InvalidValueException.class)
//    public void testCreateWithoutSchedulingInterPollDelay () throws BusinessException, SQLException {
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        String name = "No-Scheduling-InterpollDelay";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(null);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//
//        // Expecting a BusinessException to be thrown because the scheduling interpoll delay is not set
//    }
//
//    @Test(expected = BusinessException.class)
//    public void testCreateWithExistingName () throws BusinessException, SQLException {
//        String serverName = "Candidate-for-duplicate";
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        shadow.setName(serverName);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//
//        OfflineComServerShadow shadow2 = new OfflineComServerShadow();
//        shadow2.setName(serverName);
//        shadow2.setActive(false);
//        shadow2.setServerLogLevel(ComServer.LogLevel.TRACE);
//        shadow2.setCommunicationLogLevel(ComServer.LogLevel.TRACE);
//        shadow2.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow2.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow2.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//
//        // Expecting a BusinessException to be thrown because a ComServer with the same name already exists
//    }
//
//    @Test
//    public void testCreateWithExistingButDeletedName () throws BusinessException, SQLException {
//        String serverName = "Candidate-for-duplication";
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        shadow.setName(serverName);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        OfflineComServer offlineComServer1 = new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//        offlineComServer1.delete();
//
//        OfflineComServerShadow shadow2 = new OfflineComServerShadow();
//        shadow2.setName(serverName);
//        shadow2.setActive(false);
//        shadow2.setServerLogLevel(ComServer.LogLevel.TRACE);
//        shadow2.setCommunicationLogLevel(ComServer.LogLevel.TRACE);
//        shadow2.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow2.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow2.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        OfflineComServer offlineComServer2 = new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//
//        // No BusinessException expected, because a new ComServer can have the same name as a deleted one.
//    }
//
//    @Test
//    public void testUpdateWithoutComPort () throws BusinessException, SQLException {
//        this.injectFactories();
//
//        OfflineComServerShadow creationShadow = new OfflineComServerShadow();
//        String name = "Update-Candidate";
//        creationShadow.setName(name);
//        creationShadow.setActive(true);
//        creationShadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        creationShadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        creationShadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        creationShadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        OfflineComServer comServer = new ComServerFactoryImpl(engineModelService).createOffline(creationShadow);
//
//        // Business method
//        String changedName = "Name-Updated";
//        ComServer.LogLevel changedServerLogLevel = ComServer.LogLevel.WARN;
//        ComServer.LogLevel changedComLogLevel = ComServer.LogLevel.INFO;
//        TimeDuration changedChangesInterPollDelay = SCHEDULING_INTER_POLL_DELAY;
//        TimeDuration changedSchedulingInterPollDelay = CHANGES_INTER_POLL_DELAY;
//        comServer.setName(changedName);
//        comServer.setActive(false);
//        comServer.setServerLogLevel(changedServerLogLevel);
//        comServer.setCommunicationLogLevel(changedComLogLevel);
//        comServer.setChangesInterPollDelay(changedChangesInterPollDelay);
//        comServer.setSchedulingInterPollDelay(changedSchedulingInterPollDelay);
//        comServer.save();
//
//        // Asserts
//        assertThat(changedName).isEqualTo(comServer.getName());
//        assertThat(comServer.isActive()).isFalse();
//        assertThat(changedServerLogLevel).isEqualTo(comServer.getServerLogLevel());
//        assertThat(changedComLogLevel).isEqualTo(comServer.getCommunicationLogLevel());
//        assertThat(changedChangesInterPollDelay).isEqualTo(comServer.getChangesInterPollDelay());
//        assertThat(changedSchedulingInterPollDelay).isEqualTo(comServer.getSchedulingInterPollDelay());
//    }
//
//    @Test
//    public void testUpdateWithUpdatesToComPort () throws BusinessException, SQLException {
//        ServerOutboundComPort comPort = this.injectFactories();
//
//        OfflineComServerShadow creationShadow = new OfflineComServerShadow();
//        String name = "Update-Candidate2";
//        creationShadow.setName(name);
//        creationShadow.setActive(true);
//        creationShadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        creationShadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        creationShadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        creationShadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        creationShadow.addOutboundComPort(this.outboundComPortShadow());
//        OfflineComServer comServer = new ComServerFactoryImpl(engineModelService).createOffline(creationShadow);
//
//        // Business method
//        String changedName = "Name-Updated2";
//        ComServer.LogLevel changedServerLogLevel = ComServer.LogLevel.WARN;
//        ComServer.LogLevel changedComLogLevel = ComServer.LogLevel.INFO;
//        TimeDuration changedChangesInterPollDelay = SCHEDULING_INTER_POLL_DELAY;
//        TimeDuration changedSchedulingInterPollDelay = CHANGES_INTER_POLL_DELAY;
//        comServer.setName(changedName);
//        comServer.setActive(false);
//        comServer.setServerLogLevel(changedServerLogLevel);
//        comServer.setCommunicationLogLevel(changedComLogLevel);
//        comServer.setChangesInterPollDelay(changedChangesInterPollDelay);
//        comServer.setSchedulingInterPollDelay(changedSchedulingInterPollDelay);
//        OutboundComPortShadow comPortShadow = comServer.getOutboundComPortShadow().get(0);
//        comPortShadow.setName("Updated");
//        comServer.save();
//
//        // Asserts
//        assertThat(changedName).isEqualTo(comServer.getName());
//        assertThat(comServer.isActive()).isFalse();
//        assertThat(changedServerLogLevel).isEqualTo(comServer.getServerLogLevel());
//        assertThat(changedComLogLevel).isEqualTo(comServer.getCommunicationLogLevel());
//        assertThat(changedChangesInterPollDelay).isEqualTo(comServer.getChangesInterPollDelay());
//        assertThat(changedSchedulingInterPollDelay).isEqualTo(comServer.getSchedulingInterPollDelay());
//        verify(comPort).updateFrom(comPortShadow);  // Verify that the related comport was also updated
//    }
//
//    @Test
//    public void testUpdateWithoutUpdatesToComPort () throws BusinessException, SQLException {
//        ServerOutboundComPort comPort = this.injectFactories();
//
//        OfflineComServerShadow creationShadow = new OfflineComServerShadow();
//        String name = "Update-Candidate3";
//        creationShadow.setName(name);
//        creationShadow.setActive(true);
//        creationShadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        creationShadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        creationShadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        creationShadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        creationShadow.addOutboundComPort(this.outboundComPortShadow());
//        OfflineComServer comServer = new ComServerFactoryImpl(engineModelService).createOffline(creationShadow);
//
//        // Business method
//        String changedName = "Name-Updated3";
//        ComServer.LogLevel changedServerLogLevel = ComServer.LogLevel.WARN;
//        ComServer.LogLevel changedComLogLevel = ComServer.LogLevel.INFO;
//        TimeDuration changedChangesInterPollDelay = SCHEDULING_INTER_POLL_DELAY;
//        TimeDuration changedSchedulingInterPollDelay = CHANGES_INTER_POLL_DELAY;
//        comServer.setName(changedName);
//        comServer.setActive(false);
//        comServer.setServerLogLevel(changedServerLogLevel);
//        comServer.setCommunicationLogLevel(changedComLogLevel);
//        comServer.setChangesInterPollDelay(changedChangesInterPollDelay);
//        comServer.setSchedulingInterPollDelay(changedSchedulingInterPollDelay);
//        comServer.save();
//
//        // Asserts
//        assertThat(changedName).isEqualTo(comServer.getName());
//        assertThat(comServer.isActive()).isFalse();
//        assertThat(changedServerLogLevel).isEqualTo(comServer.getServerLogLevel());
//        assertThat(changedComLogLevel).isEqualTo(comServer.getCommunicationLogLevel());
//        assertThat(changedChangesInterPollDelay).isEqualTo(comServer.getChangesInterPollDelay());
//        assertThat(changedSchedulingInterPollDelay).isEqualTo(comServer.getSchedulingInterPollDelay());
//        verify(comPort, times(0)).updateFrom(any(ComPortShadow.class));  // Verify that the related comport was NOT updated
//    }
//
//    @Test
//    public void testMakeObsoleteWithComPortsWithoutViolations () throws BusinessException, SQLException {
//        this.injectFactories();
//
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        String name = "testMakeObsoleteWithComPortsWithoutViolations";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.addOutboundComPort(this.outboundComPortShadow());
//        OfflineComServer comServer = new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//        long id = comServer.getId();
//        List<OutboundComPort> comPorts = comServer.getOutboundComPorts();
//
//        // Business method
//        comServer.makeObsolete();
//
//        // Asserts
//        assertThat(new ComServerFactoryImpl(engineModelService).find((int) id)).isNotNull();
//        for (OutboundComPort outbound : comPorts) {
//            ServerComPort comPort = (ServerComPort) outbound;
//            verify(comPort).makeObsolete();
//        }
//    }
//
//    @Test
//    public void testMakeObsolete () throws BusinessException, SQLException {
//        this.injectFactories();
//
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        String name = "testMakeObsolete";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.addOutboundComPort(this.outboundComPortShadow());
//        OfflineComServer comServer = new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//
//        // Business method
//        comServer.makeObsolete();
//        ComServer deletedComServer = new ComServerFactoryImpl(engineModelService).find((int) comServer.getId());
//
//        // Asserts
//        assertTrue("DeleteDate should be filled in", comServer.getObsoleteDate() != null);
//        assertTrue("Should be marked for delete", comServer.isObsolete());
//
//        assertNotNull("DeleteDate should be filled in", deletedComServer.getObsoleteDate());
//        assertTrue("Should be marked for delete", deletedComServer.isObsolete());
//        assertTrue("toString() representation should contain '(Deleted on ...)'", deletedComServer.toString().contains("delete"));
//    }
//
//    @Test(expected = BusinessException.class)
//    public void testUpdateAfterMakeObsolete() throws BusinessException, SQLException {
//        this.injectFactories();
//
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        String name = "testUpdateAfterMakeObsolete";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.addOutboundComPort(this.outboundComPortShadow());
//
//        // Business method
//        OfflineComServer comServer = new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//
//        // Business method
//        comServer.makeObsolete();
//        comServer.save();
//    }
//
//    @Test(expected = BusinessException.class)
//    public void testMakeObsoleteTwice () throws BusinessException, SQLException {
//        this.injectFactories();
//
//        OfflineComServerShadow shadow = new OfflineComServerShadow();
//        String name = "testMakeObsoleteTwice";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.addOutboundComPort(this.outboundComPortShadow());
//
//        // Business method
//        OfflineComServer comServer = new ComServerFactoryImpl(engineModelService).createOffline(shadow);
//        comServer.makeObsolete();
//
//        // Business method
//        comServer.makeObsolete();
//
//        // Expected a BusinessException because a ComServer cannot be made obsolete twice
//    }
//
//    private OutboundComPortShadow outboundComPortShadow () {
//        OutboundComPortShadow shadow = new OutboundComPortShadow();
//        shadow.setType(ComPortType.TCP);
//        shadow.setActive(true);
//        shadow.setName("Outbound");
//        shadow.setNumberOfSimultaneousConnections(1);
//        return shadow;
//    }
//
//    private ServerOutboundComPort injectFactories () {
//        ServerComPortFactory comPortFactory = this.mockComPortFactory();
//        when(ManagerFactory.getCurrent().getComPortFactory()).thenReturn(comPortFactory);
//        return this.injectComPorts(comPortFactory);
//    }
//
//    private ServerComPortFactory mockComPortFactory () {
//        return mock(ServerComPortFactory.class);
//    }
//
//    private ServerOutboundComPort injectComPorts (ServerComPortFactory factory) {
//        ServerOutboundComPort comPort = mock(ServerOutboundComPort.class);
//        int comPortId = nextComPortId++;
//        when(comPort.getId()).thenReturn(comPortId);
//        OutboundComPortShadow shadow = new OutboundComPortShadow();
//        shadow.setId(comPortId);
//        shadow.markClean();
//        when(comPort.getShadow()).thenReturn(shadow);
//        try {
//            when(factory.createOutbound(any(ComServer.class), any(OutboundComPortShadow.class))).thenReturn(comPort);
//        }
//        catch (BusinessException | SQLException e) {
//            // When.thenReturn does not actually call the method so this exception is never going to happen
//        }
//        when(factory.find(comPortId)).thenReturn(comPort);
//        List<ComPort> comPorts = new ArrayList<>(1);
//        comPorts.add(comPort);
//        when(factory.findByComServer(any(ComServer.class))).thenReturn(comPorts);
//        return comPort;
//    }
//
//}