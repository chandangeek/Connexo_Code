/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.channel.serial.BaudrateValue;
import com.energyict.mdc.channel.serial.FlowControl;
import com.energyict.mdc.channel.serial.NrOfDataBits;
import com.energyict.mdc.channel.serial.NrOfStopBits;
import com.energyict.mdc.channel.serial.Parities;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.comserver.ModemBasedInboundComPort;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.comserver.TCPBasedInboundComPort;
import com.energyict.mdc.common.comserver.UDPBasedInboundComPort;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.ports.ComPortType;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
* Tests the integration between the {@link ComServerImpl} and
* the ComPortImpl components.
* Since the InboundOutboundComServerImpl is an abstract class,
* it actually uses the {@link OnlineComServerImpl} class but that
* was just a random choice, there is no reason why it could not
* have been another member of the class hierarchy.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-04-19 (10:00)
*/
@RunWith(MockitoJUnitRunner.class)
public class ComServerComPortTest extends PersistenceTest {

    private static final String QUERY_API_POST_URL = "ws://comserver.energyict.com/queryAPI/";
    private static final String EVENT_REGISTRATION_URL = "ws://comserver.energyict.com/events/registration/";

    private static final ComServer.LogLevel SERVER_LOG_LEVEL = ComServer.LogLevel.ERROR;
    private static final ComServer.LogLevel COMMUNICATION_LOG_LEVEL = ComServer.LogLevel.TRACE;
    private static final TimeDuration CHANGES_INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.TimeUnit.HOURS);
    private static final TimeDuration SCHEDULING_INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.TimeUnit.MINUTES);
    private static final long PCTHIGHPRIOTASKS = 3;

    private InboundComPortPool tcpBasedInboundComPortPool;
    private InboundComPortPool udpBasedInboundComPortPool;
    private InboundComPortPool serialBasedInboundComPortPool;

    @Before
    public void setUp () {
        super.setUp();
        tcpBasedInboundComPortPool = newInboundComPortPool(ComPortType.TCP);
        udpBasedInboundComPortPool = newInboundComPortPool(ComPortType.UDP);
        serialBasedInboundComPortPool = newInboundComPortPool(ComPortType.SERIAL);
    }

    @Test
    @Transactional
    public void testCreateWithComPortsWithoutViolations () {
        OnlineComServer comServer = createOnlineComServer();

        int numberOfComPorts = 3;
        addComPorts(comServer, numberOfComPorts);
        // Asserts
        assertThat(comServer.isActive()).as("Was expecting the new com server to be active").isTrue();
        assertThat(comServer.getServerLogLevel()).isEqualTo(SERVER_LOG_LEVEL);
        assertThat(comServer.getCommunicationLogLevel()).isEqualTo(COMMUNICATION_LOG_LEVEL);
        assertThat(comServer.getChangesInterPollDelay()).isEqualTo(CHANGES_INTER_POLL_DELAY);
        assertThat(comServer.getSchedulingInterPollDelay()).isEqualTo(SCHEDULING_INTER_POLL_DELAY);
        assertThat(comServer.getOutboundComPorts().size()).isEqualTo(numberOfComPorts);
    }

    @Test
    @Transactional
    public void loadWithComPortsTest() {
        OnlineComServer shadow = createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(shadow, numberOfComPorts);

        // Business method
        OnlineComServer loadedOnlineServer = (OnlineComServer) getEngineModelService().findComServer(shadow.getId()).get();

        // Asserts
        assertThat(loadedOnlineServer.isActive()).as("Was expecting the new com server to be active").isTrue();
        assertThat(loadedOnlineServer.getServerLogLevel()).isEqualTo(SERVER_LOG_LEVEL);
        assertThat(loadedOnlineServer.getCommunicationLogLevel()).isEqualTo(COMMUNICATION_LOG_LEVEL);
        assertThat(loadedOnlineServer.getChangesInterPollDelay()).isEqualTo(CHANGES_INTER_POLL_DELAY);
        assertThat(loadedOnlineServer.getSchedulingInterPollDelay()).isEqualTo(SCHEDULING_INTER_POLL_DELAY);
        assertThat(loadedOnlineServer.getOutboundComPorts().size()).isEqualTo(numberOfComPorts);
    }

    @Test
    @Transactional
    public void testUpdateAddComPortsViaShadow () {
        OnlineComServer onlineComServer = createOnlineComServer();

        onlineComServer.setActive(false);

        int numberOfComPorts = 3;
        this.addComPorts(onlineComServer, numberOfComPorts);

        onlineComServer.update();

        // Asserts
        assertThat(onlineComServer.getOutboundComPorts().size()).isEqualTo(numberOfComPorts);

        // Reload to make sure to work with an empty ComPort cache.
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(onlineComServer.getId()).get();
        assertThat(reloaded.getOutboundComPorts().size()).isEqualTo(numberOfComPorts);
    }

    @Test
    @Transactional
    public void testUpdateAddOutboundComPorts () {
        OnlineComServer onlineComServer = createOnlineComServer();

        // Business method
        onlineComServer.newOutboundComPort("Outbound-" + uniqueComPortId++, 1)
                .comPortType(ComPortType.TCP)
                .active(true).add();

        // Asserts
        assertThat(onlineComServer.getOutboundComPorts().size()).isEqualTo(1);

        // Reload to make sure to work with an empty ComPort cache.
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(onlineComServer.getId()).get();
        assertThat(reloaded.getOutboundComPorts().size()).isEqualTo(1);
    }

    @Test
    @Transactional
    public void testUpdateAddInboundComPorts () {
        OnlineComServer comServer = createOnlineComServer();

        // Business method
        modemBasedComPort(comServer);
        tcpComPort(comServer);
        udpComPort(comServer);

        // Asserts
        assertThat(comServer.getInboundComPorts().size()).isEqualTo(3);

        // Reload to make sure to work with an empty ComPort cache.
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(comServer.getId()).get();
        assertThat(reloaded.getInboundComPorts().size()).isEqualTo(3);
    }

    @Test
    @Transactional
    public void testUpdateWithUpdatesToComPorts () {
        OnlineComServer onlineComServer = createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(onlineComServer, numberOfComPorts);
        Instant modificationDate = onlineComServer.getModTime();

        // Business method
        onlineComServer.setActive(false);
        OutboundComPort comPort1 = onlineComServer.getOutboundComPorts().get(0);
        comPort1.setName("Updated-1");
        comPort1.update();
        OutboundComPort comPort2 = onlineComServer.getOutboundComPorts().get(1);
        comPort2.setName("Updated-2");
        comPort2.update();
        OutboundComPort comPort3 = onlineComServer.getOutboundComPorts().get(2);
        comPort3.setName("Updated-3");
        comPort3.update();

        // Asserts
        assertThat(onlineComServer.getOutboundComPorts().size()).isEqualTo(numberOfComPorts);

        // Reload to make sure to have emptied the cache of ComPorts;
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(onlineComServer.getId()).get();
        Instant reloadedModificationDate = reloaded.getModTime();
        assertThat(reloaded.getOutboundComPorts().size()).isEqualTo(numberOfComPorts);
        for (OutboundComPort comPort : reloaded.getOutboundComPorts()) {
            assertThat(comPort.getName().startsWith("Updated")).as("Was expecting the name has changed").isTrue();
        }
        assertThat(reloadedModificationDate.isAfter(modificationDate)).isTrue();
    }

    @Test
    @Transactional
    public void testUpdateWithDeletedComPortsViaShadow () {
        OnlineComServer comServer = createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(comServer, numberOfComPorts);

        comServer.setActive(false);
        List<OutboundComPort> outboundComPorts = comServer.getOutboundComPorts();
        Set<Long> comPortIds = new HashSet<>();
        comPortIds.add(outboundComPorts.get(0).getId());
        comPortIds.add(outboundComPorts.get(2).getId());
        // Business method
        comServer.removeComPort(outboundComPorts.get(1).getId());   // Removes the second ComPort.

        // Asserts
        assertThat(comServer.getOutboundComPorts().size()).isEqualTo(numberOfComPorts - 1);

        // Reload to make sure to work with empty ComPort cache
        OnlineComServer reloaded = (OnlineComServer) getEngineModelService().findComServer(comServer.getId()).get();
        assertThat(reloaded.getOutboundComPorts().size()).isEqualTo(numberOfComPorts - 1);
        for (OutboundComPort comPort : reloaded.getOutboundComPorts()) {
            comPortIds.remove(comPort.getId());
        }
        if (!comPortIds.isEmpty()) {
            for (Long comPortId : comPortIds) {
                System.err.println("Unexpected comport id" + comPortId);
            }
            fail("Removal of ComPorts failed because ComPorts that were not removed, were not returned after the update: "+comPortIds);
        }
    }

    @Test
    @Transactional
    public void testDeleteWithComPorts () {
        OnlineComServer comServer = createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(comServer, numberOfComPorts);
        long id = comServer.getId();

        // Business method
        comServer.delete();

        // Asserts
        assertThat(getEngineModelService().findComServer(id).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithComPorts () {
        OnlineComServer comServer = createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(comServer, numberOfComPorts);
        long id = comServer.getId();
        Set<Long> comPortIds = new HashSet<>();
        for (OutboundComPort comPort : comServer.getOutboundComPorts()) {
            comPortIds.add(comPort.getId());
        }

        // Business method
        comServer.makeObsolete();

        // Asserts
        assertThat(getEngineModelService().findComServer(id)).isNotNull();
        for (Long comPortId : comPortIds) {
            Optional<? extends ComPort> obsoleteComPort = getEngineModelService().findComPort(comPortId);
            assertThat(obsoleteComPort).isNotNull();
            assertThat(obsoleteComPort.isPresent()).isTrue();
            assertThat(obsoleteComPort.get().isObsolete()).isTrue();
        }
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithComPortsInPool () {
        OutboundComPortPool comPortPool = this.createOutboundComPortPool();
        OnlineComServer comServer = createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(comServer, numberOfComPorts);
        long id = comServer.getId();
        Set<Long> comPortIds = new HashSet<>();
        for (OutboundComPort comPort : comServer.getOutboundComPorts()) {
            comPortIds.add(comPort.getId());
            comPortPool.addOutboundComPort(comPort);
        }

        // Business method
        comServer.makeObsolete();

        // Asserts
        assertThat(getEngineModelService().findComServer(id)).isNotNull();
        for (Long comPortId : comPortIds) {
            Optional<? extends ComPort> obsoleteComPort = getEngineModelService().findComPort(comPortId);
            assertThat(obsoleteComPort).isNotNull();
            assertThat(obsoleteComPort.isPresent()).isTrue();
            assertThat(obsoleteComPort.get().isObsolete()).isTrue();
            assertThat(obsoleteComPort.get()).isInstanceOf(OutboundComPort.class);
            List<OutboundComPortPool> containingComPortPools = this.getEngineModelService().findContainingComPortPoolsForComPort((OutboundComPort) obsoleteComPort.get());
            assertThat(containingComPortPools).isEmpty();
        }
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_PORT_PER_COM_SERVER+"}", property = "portNumber")
    public void duplicateComPortsTest() throws SQLException {
        int duplicatePortNumber = 2222;
        OnlineComServer shadow = createOnlineComServer();

        shadow.newTCPBasedInboundComPort("TCP1", 1, duplicatePortNumber)
                .active(true)
                .comPortPool(tcpBasedInboundComPortPool)
                .add();

        shadow.newTCPBasedInboundComPort("TCP2",1, duplicatePortNumber)
                .active(true)
                .comPortPool(tcpBasedInboundComPortPool)
                .add();
    }

    @Test
    @Transactional
    public void findContainingComPortPoolsForComServerWithoutPorts() {
        OnlineComServer comServer = this.createOnlineComServer();

        // Business method
        List<ComPortPool> comPortPools = this.getEngineModelService().findContainingComPortPoolsForComServer(comServer);

        // Asserts
        assertThat(comPortPools).isEmpty();
    }

    @Test
    @Transactional
    public void findContainingComPortPoolsForComServerWithAllPortInTheSamePool() {
        OutboundComPortPool comPortPool = this.createOutboundComPortPool();
        OnlineComServer comServer = this.createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(comServer, numberOfComPorts);
        comServer.getOutboundComPorts().forEach(comPortPool::addOutboundComPort);

        // Business method
        List<ComPortPool> comPortPools = this.getEngineModelService().findContainingComPortPoolsForComServer(comServer);

        // Asserts
        assertThat(comPortPools).hasSize(1);
        assertThat(comPortPools.get(0).getId()).isEqualTo(comPortPool.getId());
    }

    @Test
    @Transactional
    public void findContainingComPortPoolsForComServerWithAllPortInTheDifferentPools() {
        OutboundComPortPool comPortPool1 = this.createOutboundComPortPool("ComServerComPortTest-1");
        OutboundComPortPool comPortPool2 = this.createOutboundComPortPool("ComServerComPortTest-2");
        OutboundComPortPool comPortPool3 = this.createOutboundComPortPool("ComServerComPortTest-3");
        OnlineComServer comServer = this.createOnlineComServer();
        int numberOfComPorts = 3;
        this.addComPorts(comServer, numberOfComPorts);
        comPortPool1.addOutboundComPort(comServer.getOutboundComPorts().get(0));
        comPortPool2.addOutboundComPort(comServer.getOutboundComPorts().get(1));
        comPortPool3.addOutboundComPort(comServer.getOutboundComPorts().get(2));

        // Business method
        List<ComPortPool> comPortPools = this.getEngineModelService().findContainingComPortPoolsForComServer(comServer);
        Set<Long> comPortPoolIds = comPortPools.stream().map(ComPortPool::getId).collect(Collectors.toSet());

        // Asserts
        assertThat(comPortPools).hasSize(3);
        assertThat(comPortPoolIds).containsOnly(comPortPool1.getId(), comPortPool2.getId(), comPortPool3.getId());
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_PORT+"}", property = "name")
    @Transactional
    public void testCreateTwoComPortsWithTheSameNameOnOneComServer(){
        OnlineComServer comServer = this.createOnlineComServer();
        comServer.newOutboundComPort("Non-unique name", 2).comPortType(ComPortType.TCP).active(true).add();
        comServer.newOutboundComPort("Non-unique name", 4).comPortType(ComPortType.TCP).active(true).add();

        // Assert exception
    }

    @Test
    @Transactional
    public void testCreateTwoComPortsWithTheSameNameOnDifferentComServers(){
        OnlineComServer firstComServer = this.createOnlineComServer();
        OutboundComPort firstComPort = firstComServer.newOutboundComPort("Unique name per comserver", 2).comPortType(ComPortType.TCP).active(true).add();

        OnlineComServer secondComServer = this.createOnlineComServer();
        OutboundComPort secondComPort = secondComServer.newOutboundComPort("Unique name per comserver", 4).comPortType(ComPortType.TCP).active(true).add();

        assertThat(firstComPort.getName()).isEqualTo(secondComPort.getName());
    }

    @Test
    @Transactional
    public void testCreateDeleteCreateComPortsWithTheSameNameOnOneComServer(){
        OnlineComServer comServer = this.createOnlineComServer();
        OutboundComPort comPort = comServer.newOutboundComPort("Non-unique name", 2).comPortType(ComPortType.TCP).active(true).add();
        comPort.makeObsolete();
        comServer.newOutboundComPort("Non-unique name", 4).comPortType(ComPortType.TCP).active(true).add();

        // Assert no exception
    }

    private int uniqueComPortId=1;
    private void addComPorts(OnlineComServer comServer, int numberOfComPorts) {
        for (int i = 0; i < numberOfComPorts; i++) {
            comServer.newOutboundComPort("Outbound-" + uniqueComPortId++, 1).comPortType(ComPortType.TCP).active(true).add();
        }
    }

    private ModemBasedInboundComPort modemBasedComPort(ComServer comServer) {
        String name = "Inbound-" + uniqueComPortId++;
        return comServer.newModemBasedInboundComport(name, 3, 2, new TimeDuration(30), new TimeDuration(5), new SerialPortConfiguration(name,
                                BaudrateValue.BAUDRATE_9600,
                                NrOfDataBits.EIGHT,
                                NrOfStopBits.ONE,
                                Parities.NONE,
                                FlowControl.NONE))
                .active(true)
                .comPortPool(serialBasedInboundComPortPool)
                .atCommandTry(new BigDecimal(3))
                .add();
    }

    private TCPBasedInboundComPort tcpComPort(ComServer comServer) {
        String name = "Inbound-" + uniqueComPortId++;
        return comServer.newTCPBasedInboundComPort(name, 1, 9000)
                .active(true)
                .comPortPool(tcpBasedInboundComPortPool)
                .add();
    }

    private UDPBasedInboundComPort udpComPort(ComServer comServer) {
        String name = "Inbound-" + uniqueComPortId++;
        return comServer.newUDPBasedInboundComPort(name, 1, 9001)
                .active(true)
                .comPortPool(udpBasedInboundComPortPool)
                .bufferSize(1024)
                .add();
    }

    private int comPortPoolIndex=1;
    private InboundComPortPool newInboundComPortPool(ComPortType comPortType) {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool("Unique comPortPool "+comPortPoolIndex++, comPortType, inboundDeviceProtocolPluggableClass, Collections.emptyMap());
        inboundComPortPool.setDescription("description");
        inboundComPortPool.update();
        return inboundComPortPool;
    }

    int onlineNameNumber=1;

    private OnlineComServer createOnlineComServer() {
        final OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();
        String name = "Online-" + onlineNameNumber;
        onlineComServerBuilder.storeTaskQueueSize(1)
                .storeTaskThreadPriority(1)
                .numberOfStoreTaskThreads(1)
                .queryApiPort(onlineNameNumber)
                .eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER)
                .serverName(name)
                .name(name)
                .active(true)
                .serverLogLevel(SERVER_LOG_LEVEL)
                .communicationLogLevel(COMMUNICATION_LOG_LEVEL)
                .changesInterPollDelay(CHANGES_INTER_POLL_DELAY)
                .schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineNameNumber++;
        return onlineComServerBuilder.create();
    }

    private OutboundComPortPool createOutboundComPortPool () {
        return this.createOutboundComPortPool("ComServerComPortTest");
    }

    private OutboundComPortPool createOutboundComPortPool (String name) {
        OutboundComPortPool comPortPool = getEngineModelService().newOutboundComPortPool(name, ComPortType.TCP, TimeDuration.minutes(1), PCTHIGHPRIOTASKS);
        comPortPool.setActive(true);
        comPortPool.update();
        return comPortPool;
    }

}