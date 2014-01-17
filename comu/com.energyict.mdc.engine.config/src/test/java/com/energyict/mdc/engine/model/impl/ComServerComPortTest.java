//package com.energyict.mdc.engine.model.impl;
//
//import com.energyict.mdc.InMemoryPersistence;
//import com.energyict.mdc.ManagerFactory;
//import com.energyict.mdc.MdwInterface;
//import com.energyict.mdc.PersistenceTest;
//import com.energyict.mdc.ServerManager;
//import com.energyict.mdc.channels.serial.BaudrateValue;
//import com.energyict.mdc.channels.serial.FlowControl;
//import com.energyict.mdc.channels.serial.NrOfDataBits;
//import com.energyict.mdc.channels.serial.NrOfStopBits;
//import com.energyict.mdc.channels.serial.Parities;
//import com.energyict.mdc.channels.serial.SerialPortConfiguration;
//import com.energyict.mdc.common.BusinessException;
//import com.energyict.mdc.common.Environment;
//import com.energyict.mdc.common.InvalidValueException;
//import com.energyict.mdc.common.ShadowList;
//import com.energyict.mdc.common.TimeDuration;
//import com.energyict.mdc.engine.model.ComPort;
//import com.energyict.mdc.engine.model.ComServer;
//import com.energyict.mdc.engine.model.EngineModelService;
//import com.energyict.mdc.engine.model.InboundComPortPool;
//import com.energyict.mdc.engine.model.OnlineComServer;
//import com.energyict.mdc.engine.model.PersistenceTest;
//import com.energyict.mdc.journal.ComSessionFactoryImpl;
//import com.energyict.mdc.meta.persistence.ComPortPoolMemberTableDescription;
//import com.energyict.mdc.meta.persistence.ComPortPoolTableDescription;
//import com.energyict.mdc.meta.persistence.ComPortTableDescription;
//import com.energyict.mdc.meta.persistence.ComSessionTableDescription;
//import com.energyict.mdc.meta.persistence.model.DatabaseModel;
//import com.energyict.mdc.meta.persistence.model.DatabaseModelImpl;
//import com.energyict.mdc.ports.ComPortFactoryImpl;
//import com.energyict.mdc.ports.ComPortPoolFactoryImpl;
//import com.energyict.mdc.protocol.api.ComPortType;
//import com.energyict.mdc.shadow.ports.IPBasedInboundComPortShadow;
//import com.energyict.mdc.shadow.ports.InboundComPortPoolShadow;
//import com.energyict.mdc.shadow.ports.InboundComPortShadow;
//import com.energyict.mdc.shadow.ports.ModemBasedInboundComPortShadow;
//import com.energyict.mdc.shadow.ports.OutboundComPortShadow;
//import com.energyict.mdc.shadow.ports.TCPBasedInboundComPortShadow;
//import com.energyict.mdc.shadow.ports.UDPBasedInboundComPortShadow;
//import com.energyict.mdc.shadow.servers.OnlineComServerShadow;
//import com.energyict.mdw.core.PluggableClass;
//import com.energyict.mdw.core.PluggableClassFactory;
//import com.energyict.mdw.event.RegisteredEventHandlerFactory;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.runners.MockitoJUnitRunner;
//
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.sql.SQLException;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import static org.fest.assertions.api.Fail.fail;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Mockito.when;
//
///**
// * Tests the integration between the {@link ComServerImpl} and
// * the ComPortImpl components.
// * Since the InboundOutboundComServerImpl is an abstract class,
// * it actually uses the {@link OnlineComServerImpl} class but that
// * was just a random choice, there is no reason why it could not
// * have been another member of the class hierarchy.
// *
// * @author Rudi Vankeirsbilck (rudi)
// * @since 2012-04-19 (10:00)
// */
//@RunWith(MockitoJUnitRunner.class)
//public class ComServerComPortTest extends PersistenceTest {
//
//    private static int nextComPortNumber = 1;
//
//    private static final String QUERY_API_POST_URL = "http://comserver.energyict.com/queryAPI";
//    private static final String EVENT_REGISTRATION_URL = "http://comserver.energyict.com/events/registration";
//
//    private static final ComServer.LogLevel SERVER_LOG_LEVEL = ComServer.LogLevel.ERROR;
//    private static final ComServer.LogLevel COMMUNICATION_LOG_LEVEL = ComServer.LogLevel.TRACE;
//    private static final TimeDuration CHANGES_INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.HOURS);
//    private static final TimeDuration SCHEDULING_INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.MINUTES);
//    private static final int PLUGGABLE_CLASS_ID = 1;
//
//    @Mock
//    private ServerManager manager;
//    @Mock
//    private MdwInterface mdwInterface;
//    @Mock
//    private RegisteredEventHandlerFactory eventHandlerFactory;
//    @Mock
//    private PluggableClassFactory pluggableClassFactory;
//    @Mock
//    private PluggableClass pluggableClass;
//    @Mock
//    private EngineModelService engineModelService;
//
//    private InboundComPortPool tcpBasedInboundComPortPool;
//    private InboundComPortPool udpBasedInboundComPortPool;
//    private InboundComPortPool serialBasedInboundComPortPool;
//
//    @BeforeClass
//    public static void initializeDatabase () throws SQLException, IOException {
//        InMemoryPersistence.initializeDatabase(ComServerComPortTest.class);
//        createTables();
//    }
//
//    private static void createTables () throws SQLException {
//        getDatabaseModel().create(Environment.DEFAULT.get().getConnection());
//    }
//
//    private static DatabaseModel getDatabaseModel () {
//        DatabaseModel databaseModel = new DatabaseModelImpl();
//        databaseModel.add(new ComPortTableDescription());
//        databaseModel.add(new ComPortPoolTableDescription());
//        databaseModel.add(new ComPortPoolMemberTableDescription());
//        databaseModel.add(new ComSessionTableDescription());
//        return databaseModel;
//    }
//
//    @Before
//    public void setUp () throws BusinessException, SQLException {
//        this.initializeMocksAndFactories();
//        this.findOrCreateInboundComPortPool(ComPortType.TCP);
//        this.findOrCreateInboundComPortPool(ComPortType.UDP);
//        this.findOrCreateInboundComPortPool(ComPortType.SERIAL);
//    }
//
//    @After
//    public void cleanUpDatabase() throws BusinessException, SQLException {
//        final List<ComPort> comPorts = new ComPortFactoryImpl(engineModelService).findAllWithDeleted();
//        for (ComPort comPort : comPorts) {
//            comPort.delete();
//        }
//        final List<ComServer> comServers = new ComServerFactoryImpl(engineModelService).findAllIncludingObsoletes();
//        for (ComServer comServer : comServers) {
//            comServer.delete();
//        }
//    }
//
//    private void initializeMocksAndFactories () {
//        when(this.pluggableClass.getId()).thenReturn(PLUGGABLE_CLASS_ID);
//        when(this.pluggableClassFactory.find(PLUGGABLE_CLASS_ID)).thenReturn(this.pluggableClass);
//        when(this.manager.getComPortPoolFactory()).thenReturn(new ComPortPoolFactoryImpl(engineModelService));
//        when(this.manager.getComPortFactory()).thenReturn(new ComPortFactoryImpl(engineModelService));
//        when(this.manager.getComServerFactory()).thenReturn(new ComServerFactoryImpl(engineModelService));
//        when(this.manager.getComSessionFactory()).thenReturn(new ComSessionFactoryImpl());
//        when(this.manager.getMdwInterface()).thenReturn(this.mdwInterface);
//        ManagerFactory.setCurrent(this.manager);
//    }
//
//    public void findOrCreateInboundComPortPool (ComPortType comPortType) throws BusinessException, SQLException {
//        ComPortPoolFactoryImpl factory = new ComPortPoolFactoryImpl(engineModelService);
//        InboundComPortPoolShadow shadow = new InboundComPortPoolShadow();
//        shadow.setActive(true);
//        shadow.setName(comPortType + "Inbound");
//        shadow.setType(comPortType);
//        shadow.setDiscoveryProtocolPluggableClassId(PLUGGABLE_CLASS_ID);
//        switch(comPortType){
//            case TCP:{
//                this.tcpBasedInboundComPortPool = (InboundComPortPool) factory.find(comPortType + "Inbound");
//                if (this.tcpBasedInboundComPortPool == null) {
//                    this.tcpBasedInboundComPortPool = factory.createInbound(shadow);
//                }
//            }break;
//            case UDP:{
//                this.udpBasedInboundComPortPool = (InboundComPortPool) factory.find(comPortType + "Inbound");
//                if (this.udpBasedInboundComPortPool == null) {
//                    this.udpBasedInboundComPortPool = factory.createInbound(shadow);
//                }
//            }break;
//            case SERIAL:{
//                this.serialBasedInboundComPortPool = (InboundComPortPool) factory.find(comPortType + "Inbound");
//                if (this.serialBasedInboundComPortPool == null) {
//                    this.serialBasedInboundComPortPool = factory.createInbound(shadow);
//                }
//            }
//
//        }
//    }
//
//    @Test
//    public void testCreateWithComPortsWithoutViolations () throws BusinessException, SQLException {
//        OnlineComServerShadow shadow = new OnlineComServerShadow();
//        String name = "Online-With-ComPorts";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.setQueryAPIPostUri(QUERY_API_POST_URL);
//        int numberOfComPorts = 3;
//        this.addComPortShadows(shadow, numberOfComPorts);
//
//        // Business method
//        OnlineComServer comServer = new ComServerFactoryImpl(engineModelService).createOnline(shadow);
//
//        // Asserts
//        assertEquals(name, comServer.getName());
//        assertTrue("Was expecting the new com server to be active", comServer.isActive());
//        assertEquals(SERVER_LOG_LEVEL, comServer.getServerLogLevel());
//        assertEquals(COMMUNICATION_LOG_LEVEL, comServer.getCommunicationLogLevel());
//        assertEquals(CHANGES_INTER_POLL_DELAY, comServer.getChangesInterPollDelay());
//        assertEquals(SCHEDULING_INTER_POLL_DELAY, comServer.getSchedulingInterPollDelay());
//        assertEquals("The number of com ports does not match.", numberOfComPorts, comServer.getOutboundComPorts().size());
//    }
//
//    @Test
//    public void loadWithComPortsTest() throws BusinessException, SQLException {
//        OnlineComServerShadow shadow = new OnlineComServerShadow();
//        String name = "Online-With-ComPorts";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.setQueryAPIPostUri(QUERY_API_POST_URL);
//        int numberOfComPorts = 3;
//        this.addComPortShadows(shadow, numberOfComPorts);
//
//        // Business method
//        OnlineComServer createdOnlineServer = new ComServerFactoryImpl(engineModelService).createOnline(shadow);
//        OnlineComServer loadedOnlineServer = (OnlineComServer) new ComServerFactoryImpl(engineModelService).find((int) createdOnlineServer.getId());
//
//        // Asserts
//        assertEquals(name, loadedOnlineServer.getName());
//        assertTrue("Was expecting the new com server to be active", loadedOnlineServer.isActive());
//        assertEquals(SERVER_LOG_LEVEL, loadedOnlineServer.getServerLogLevel());
//        assertEquals(COMMUNICATION_LOG_LEVEL, loadedOnlineServer.getCommunicationLogLevel());
//        assertEquals(CHANGES_INTER_POLL_DELAY, loadedOnlineServer.getChangesInterPollDelay());
//        assertEquals(SCHEDULING_INTER_POLL_DELAY, loadedOnlineServer.getSchedulingInterPollDelay());
//        assertEquals("The number of com ports does not match.", numberOfComPorts, loadedOnlineServer.getOutboundComPorts().size());
//    }
//
//    @Test
//    public void testUpdateAddComPortsViaShadow () throws BusinessException, SQLException {
//        OnlineComServerShadow creationShadow = new OnlineComServerShadow();
//        String name = "Add-ComPorts-Via-Shadow";
//        creationShadow.setName(name);
//        creationShadow.setActive(true);
//        creationShadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        creationShadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        creationShadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        creationShadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        creationShadow.setQueryAPIPostUri(QUERY_API_POST_URL);
//        creationShadow.setEventRegistrationUri(EVENT_REGISTRATION_URL);
//        ComServerFactoryImpl comServerFactory = new ComServerFactoryImpl(engineModelService);
//        OnlineComServer comServer = comServerFactory.createOnline(creationShadow);
//
//        OnlineComServerShadow shadow = comServer.getShadow();
//        comServer.setActive(false);
//
//        int numberOfComPorts = 3;
//        this.addComPortShadows(shadow, numberOfComPorts);
//
//        // Business method
//        comServer.update(shadow);
//
//        // Asserts
//        assertEquals("The number of com ports does not match.", numberOfComPorts, comServer.getOutboundComPorts().size());
//
//        // Reload to make sure to work with an empty ComPort cache.
//        OnlineComServer reloaded = (OnlineComServer) comServerFactory.find((int) comServer.getId());
//        assertEquals("The number of com ports does not match.", numberOfComPorts, reloaded.getOutboundComPorts().size());
//    }
//
//    @Test
//    public void testUpdateAddOutboundComPorts () throws BusinessException, SQLException {
//        OnlineComServerShadow creationShadow = new OnlineComServerShadow();
//        String name = "Add-Outbound-ComPorts";
//        creationShadow.setName(name);
//        creationShadow.setActive(true);
//        creationShadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        creationShadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        creationShadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        creationShadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        creationShadow.setQueryAPIPostUri(QUERY_API_POST_URL);
//        ComServerFactoryImpl comServerFactory = new ComServerFactoryImpl(engineModelService);
//        OnlineComServer comServer = comServerFactory.createOnline(creationShadow);
//
//        // Business method
//        comServer.createOutbound(this.outboundComPortShadow());
//
//        // Asserts
//        assertEquals("The number of com ports does not match.", 1, comServer.getOutboundComPorts().size());
//
//        // Reload to make sure to work with an empty ComPort cache.
//        OnlineComServer reloaded = (OnlineComServer) comServerFactory.find(comServer.getId());
//        assertEquals("The number of com ports does not match.", 1, reloaded.getOutboundComPorts().size());
//    }
//
//    @Test
//    public void testUpdateAddInboundComPorts () throws BusinessException, SQLException {
//        OnlineComServerShadow creationShadow = new OnlineComServerShadow();
//        String name = "Add-Inbound-ComPorts";
//        creationShadow.setName(name);
//        creationShadow.setActive(true);
//        creationShadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        creationShadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        creationShadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        creationShadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        creationShadow.setQueryAPIPostUri(QUERY_API_POST_URL);
//        ComServerFactoryImpl comServerFactory = new ComServerFactoryImpl(engineModelService);
//        OnlineComServer comServer = comServerFactory.createOnline(creationShadow);
//
//        // Business method
//        comServer.createModemBasedInbound(this.modemBasedComPortShadow());
//        comServer.createTCPBasedInbound(this.tcpComPortShadow());
//        comServer.createUDPBasedInbound(this.udpComPortShadow());
//
//        // Asserts
//        assertEquals("The number of com ports does not match.", 3, comServer.getInboundComPorts().size());
//
//        // Reload to make sure to work with an empty ComPort cache.
//        OnlineComServer reloaded = (OnlineComServer) comServerFactory.find(comServer.getId());
//        assertEquals("The number of com ports does not match.", 3, reloaded.getInboundComPorts().size());
//    }
//
//    @Test
//    public void testUpdateWithUpdatesToComPorts () throws BusinessException, SQLException {
//        ComServerFactoryImpl comServerFactory = new ComServerFactoryImpl(engineModelService);
//        OnlineComServerShadow creationShadow = new OnlineComServerShadow();
//        String name = "Update-ComPorts";
//        creationShadow.setName(name);
//        creationShadow.setActive(true);
//        creationShadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        creationShadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        creationShadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        creationShadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        creationShadow.setQueryAPIPostUri(QUERY_API_POST_URL);
//        creationShadow.setEventRegistrationUri(EVENT_REGISTRATION_URL);
//        int numberOfComPorts = 3;
//        this.addComPortShadows(creationShadow, numberOfComPorts);
//        OnlineComServer comServer = comServerFactory.createOnline(creationShadow);
//
//        OnlineComServerShadow shadow = comServer.getShadow();
//        shadow.setActive(false);
//        shadow.getOutboundComPortShadows().get(0).setName("Updated-1");
//        shadow.getOutboundComPortShadows().get(1).setName("Updated-2");
//        shadow.getOutboundComPortShadows().get(2).setName("Updated-3");
//
//        // Business method
//        comServer.update(shadow);
//
//        // Asserts
//        assertEquals("The number of com ports does not match.", numberOfComPorts, comServer.getOutboundComPorts().size());
//
//        // Reload to make sure to have emptied the cache of ComPorts;
//        OnlineComServer reloaded = (OnlineComServer) comServerFactory.find(comServer.getId());
//        assertEquals("The number of com ports does not match.", numberOfComPorts, reloaded.getOutboundComPorts().size());
//        for (OutboundComPort comPort : reloaded.getOutboundComPorts()) {
//            assertTrue("Was expecting the name has changed", comPort.getName().startsWith("Updated"));
//        }
//    }
//
//    @Test
//    public void testUpdateWithDeletedComPortsViaShadow () throws BusinessException, SQLException {
//        OnlineComServerShadow creationShadow = new OnlineComServerShadow();
//        String name = "Delete-ComPorts";
//        creationShadow.setName(name);
//        creationShadow.setActive(true);
//        creationShadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        creationShadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        creationShadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        creationShadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        creationShadow.setQueryAPIPostUri(QUERY_API_POST_URL);
//        creationShadow.setEventRegistrationUri(EVENT_REGISTRATION_URL);
//        int numberOfComPorts = 3;
//        this.addComPortShadows(creationShadow, numberOfComPorts);
//        ComServerFactoryImpl comServerFactory = new ComServerFactoryImpl(engineModelService);
//        OnlineComServer comServer = comServerFactory.createOnline(creationShadow);
//
//        OnlineComServerShadow shadow = comServer.getShadow();
//        shadow.setActive(false);
//        shadow.getOutboundComPortShadows().remove(1);   // Removes the second ComPort.
//        List<OutboundComPort> outboundComPorts = comServer.getOutboundComPorts();
//        Set<Integer> comPortIds = new HashSet<>();
//        comPortIds.add(outboundComPorts.get(0).getId());
//        comPortIds.add(outboundComPorts.get(2).getId());
//
//        // Business method
//        comServer.update(shadow);
//
//        // Asserts
//        assertEquals("The number of com ports does not match.", numberOfComPorts - 1, comServer.getOutboundComPorts().size());
//
//        // Reload to make sure to work with empty ComPort cache
//        OnlineComServer reloaded = (OnlineComServer) comServerFactory.find((int) comServer.getId());
//        assertEquals("The number of com ports does not match.", numberOfComPorts - 1, reloaded.getOutboundComPorts().size());
//        for (OutboundComPort comPort : reloaded.getOutboundComPorts()) {
//            comPortIds.remove(comPort.getId());
//        }
//        if (!comPortIds.isEmpty()) {
//            for (Integer comPortId : comPortIds) {
//                System.out.printf("Unexpected comport id" + comPortId);
//            }
//            Fail.fail("Removal of ComPorts failed because ComPorts that were not removed, were not returned after the udpate");
//        }
//    }
//
//    @Test
//    public void testDeleteWithComPorts () throws BusinessException, SQLException {
//        OnlineComServerShadow shadow = new OnlineComServerShadow();
//        String name = "Delete-Candidate-with-ComPorts";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.setQueryAPIPostUri(QUERY_API_POST_URL);
//        int numberOfComPorts = 3;
//        this.addComPortShadows(shadow, numberOfComPorts);
//        ComServerFactoryImpl comServerFactory = new ComServerFactoryImpl(engineModelService);
//        OnlineComServer comServer = comServerFactory.createOnline(shadow);
//        long id = comServer.getId();
//
//        // Business method
//        comServer.delete();
//
//        // Asserts
//        assertNull(new ComServerFactoryImpl(engineModelService).find((int) id));
//    }
//
//    @Test
//    public void testMakeObsoleteWithComPorts () throws BusinessException, SQLException {
//        OnlineComServerShadow shadow = new OnlineComServerShadow();
//        String name = "MakeObsolete-Candidate-with-ComPorts";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.setQueryAPIPostUri(QUERY_API_POST_URL);
//        int numberOfComPorts = 3;
//        this.addComPortShadows(shadow, numberOfComPorts);
//        ComServerFactoryImpl comServerFactory = new ComServerFactoryImpl(engineModelService);
//        OnlineComServer comServer = comServerFactory.createOnline(shadow);
//        long id = comServer.getId();
//        Set<Integer> comPortIds = new HashSet<>();
//        for (OutboundComPort comPort : comServer.getOutboundComPorts()) {
//            comPortIds.add(comPort.getId());
//        }
//
//        // Business method
//        comServer.makeObsolete();
//
//        // Asserts
//        assertNotNull(new ComServerFactoryImpl(engineModelService).find((int) id));
//        for (Integer comPortId : comPortIds) {
//            ServerComPort obsoleteComPort = (ServerComPort) new ComPortFactoryImpl(engineModelService).find(comPortId);
//            assertNotNull(obsoleteComPort);
//            assertTrue(obsoleteComPort.isObsolete());
//        }
//    }
//
//
//    @Test(expected = InvalidValueException.class)
//    public void duplicateComPortsTest() throws SQLException, BusinessException {
//        int duplicatePortNumber = 2222;
//        OnlineComServerShadow shadow = new OnlineComServerShadow();
//        shadow.setName("duplicateComPortsTest");
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        IPBasedInboundComPortShadow comPortShadow1 = new TCPBasedInboundComPortShadow();
//        comPortShadow1.setName("TCP1");
//        comPortShadow1.setPortNumber(duplicatePortNumber);
//
//        IPBasedInboundComPortShadow comPortShadow2 = new TCPBasedInboundComPortShadow();
//        comPortShadow2.setName("TCP2");
//        comPortShadow2.setPortNumber(duplicatePortNumber);
//
//        ShadowList<InboundComPortShadow> inboundComPortShadows = new ShadowList<>();
//        inboundComPortShadows.add(comPortShadow1);
//        inboundComPortShadows.add(comPortShadow2);
//        shadow.setInboundComPortShadows(inboundComPortShadows);
//
//        try {
//            new ComServerFactoryImpl(engineModelService).createOnline(shadow);
//        } catch (InvalidValueException e) {
//            if(!e.getMessageId().equals("duplicatecomportpercomserver")){
//                Fail.fail("Should have gotten exception indicating that the portnumber must be unique per ComServer, but was " + e.getMessage());
//            } else {
//                throw e;
//            }
//        }
//    }
//
//    private void addComPortShadows (OnlineComServerShadow comServerShadow, int numberOfComPorts) {
//        for (int i = 0; i < numberOfComPorts; i++) {
//            OutboundComPortShadow comPortShadow = outboundComPortShadow();
//            comServerShadow.addOutboundComPort(comPortShadow);
//        }
//    }
//
//    private OutboundComPortShadow outboundComPortShadow () {
//        OutboundComPortShadow comPortShadow = new OutboundComPortShadow();
//        int comPortId = nextComPortNumber++;
//        comPortShadow.setName("Outbound-" + comPortId);
//        comPortShadow.setType(ComPortType.TCP);
//        comPortShadow.setNumberOfSimultaneousConnections(1);
//        comPortShadow.setActive(true);
//        return comPortShadow;
//    }
//
//    private void initializeInboundComPortShadow(InboundComPortShadow shadow, InboundComPortPool inboundComPortPool) {
//        int comPortId = nextComPortNumber++;
//        shadow.setName("Inbound-" + comPortId);
//        shadow.setActive(true);
//        shadow.setInboundComPortPoolId(inboundComPortPool.getId());
//    }
//
//    private ModemBasedInboundComPortShadow modemBasedComPortShadow() {
//        ModemBasedInboundComPortShadow shadow = new ModemBasedInboundComPortShadow();
//        this.initializeInboundComPortShadow(shadow, serialBasedInboundComPortPool);
//        shadow.setMaximumNumberOfDialErrors(2);
//        shadow.setRingCount(3);
//        shadow.setConnectTimeout(new TimeDuration(30));
//        shadow.setAtCommandTimeout(new TimeDuration(5));
//        shadow.setAtCommandTry(new BigDecimal(3));
//        shadow.setSerialPortConfiguration(new SerialPortConfiguration(shadow.getName(),
//                BaudrateValue.BAUDRATE_9600,
//                NrOfDataBits.EIGHT,
//                NrOfStopBits.ONE,
//                Parities.NONE,
//                FlowControl.NONE));
//         shadow.setType(ComPortType.SERIAL);
//        return shadow;
//    }
//
//    private TCPBasedInboundComPortShadow tcpComPortShadow () {
//        TCPBasedInboundComPortShadow shadow = new TCPBasedInboundComPortShadow();
//        this.initializeInboundComPortShadow(shadow, tcpBasedInboundComPortPool);
//        shadow.setPortNumber(9000);
//        shadow.setNumberOfSimultaneousConnections(3);
//        shadow.setType(ComPortType.TCP);
//        return shadow;
//    }
//
//    private UDPBasedInboundComPortShadow udpComPortShadow () {
//        UDPBasedInboundComPortShadow shadow = new UDPBasedInboundComPortShadow();
//        this.initializeInboundComPortShadow(shadow, udpBasedInboundComPortPool);
//        shadow.setPortNumber(9001);
//        shadow.setNumberOfSimultaneousConnections(3);
//        shadow.setBufferSize(1024);
//        shadow.setType(ComPortType.UDP);
//        return shadow;
//    }
//
//}