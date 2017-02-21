/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.sql.Fetcher;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImplIT;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import java.time.Instant;
import java.util.Calendar;
import java.util.Iterator;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ScheduleQueryTest extends ConnectionTaskImplIT {

    private ScheduledConnectionTaskImpl createAsapWithNoPropertiesWithoutViolations(String name) {
        this.partialScheduledConnectionTask.setName(name);
        this.partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl scheduledConnectionTask = (ScheduledConnectionTaskImpl) this.device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .add();
        return scheduledConnectionTask;
    }

    private ScheduledConnectionTaskImpl createOtherAsapWithNoPropertiesWithoutViolations(String name) {
        this.partialScheduledConnectionTask2.setName(name);
        this.partialScheduledConnectionTask2.save();
        ScheduledConnectionTaskImpl scheduledConnectionTask = (ScheduledConnectionTaskImpl) this.device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask2)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .add();
        return scheduledConnectionTask;
    }

    private ScheduledConnectionTaskImpl createAsapWithNoPropertiesWithoutViolationsUsingNonActiveComportPool(String name) {
        this.partialScheduledConnectionTask.setName(name);
        this.partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl scheduledConnectionTask = (ScheduledConnectionTaskImpl) this.device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                .setComPortPool(inactiveOutboundTcpipComportPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .add();
        return scheduledConnectionTask;
    }


    private OutboundComPort createOutboundComPort() {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = inMemoryPersistence.getEngineConfigurationService().newOnlineComServerBuilder();
        String name = "ComServer";
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.storeTaskQueueSize(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.changesInterPollDelay(TimeDuration.minutes(5));
        onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.schedulingInterPollDelay(TimeDuration.minutes(1));
        onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.numberOfStoreTaskThreads(2);
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();
        OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = onlineComServer.newOutboundComPort("ComPort", 1);
        outboundComPortBuilder.comPortType(ComPortType.TCP);
        OutboundComPort outboundComPort = outboundComPortBuilder.add();
        outboundTcpipComPortPool.addOutboundComPort(outboundComPort);
        return outboundComPort;
    }

    private OutboundComPort createComPortInOtherComPortPool() {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = inMemoryPersistence.getEngineConfigurationService().newOnlineComServerBuilder();
        String name = "ComServer";
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.storeTaskQueueSize(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.changesInterPollDelay(TimeDuration.minutes(5));
        onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.schedulingInterPollDelay(TimeDuration.minutes(1));
        onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.numberOfStoreTaskThreads(2);
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();
        OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = onlineComServer.newOutboundComPort("ComPort", 1);
        outboundComPortBuilder.comPortType(ComPortType.TCP);
        OutboundComPort outboundComPort = outboundComPortBuilder.add();
        outboundTcpipComPortPool2.addOutboundComPort(outboundComPort);
        return outboundComPort;
    }

    @Test
    @Transactional
    public void simpleSchedulingQueryTest() {
        Instant pastDate = freezeClock(2013, Calendar.MARCH, 13, 10, 12, 10, 0);
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("simpleSchedulingQueryTest");
//        connectionTask.activateAndSave();
        ComTaskExecution comTaskExecution = createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(connectionTask, pastDate);
        final Instant futureDate = freezeClock(2013, Calendar.AUGUST, 5); // make the task pending
        OutboundComPort outboundComPort = createOutboundComPort();
        Fetcher<ComTaskExecution> fetcher = inMemoryPersistence.getCommunicationTaskService().getPlannedComTaskExecutionsFor(outboundComPort);

        assertThat(fetcher).isNotNull();
        Iterator<ComTaskExecution> plannedComTaskExecutions = fetcher.iterator();
        assertThat(plannedComTaskExecutions.hasNext()).isTrue();
        assertThat(plannedComTaskExecutions.next().getId()).isEqualTo(comTaskExecution.getId());
    }

    @Test
    @Transactional
    public void scheduleQueryTestNoTaskWhenConnectionTaskIsPaused() {
        Instant pastDate = freezeClock(2013, Calendar.MARCH, 13, 10, 12, 10, 0);
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("scheduleQueryTestNoTaskWhenConnectionTaskIsPaused");

        ComTaskExecution comTaskExecution = createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(connectionTask, pastDate);
        final Instant futureDate = freezeClock(2013, Calendar.AUGUST, 5); // make the task pending
        OutboundComPort outboundComPort = createOutboundComPort();
        connectionTask.deactivate();
        Fetcher<ComTaskExecution> plannedComTaskExecutions = inMemoryPersistence.getCommunicationTaskService().getPlannedComTaskExecutionsFor(outboundComPort);

        assertThat(plannedComTaskExecutions.iterator().hasNext()).isFalse();
    }

    @Test
    @Transactional
    public void scheduleQueryTestNoTaskWhenConnectionTaskIsAlreadyBusyTest() {
        // we will make sure the ComServer field is filled in
        Instant pastDate = freezeClock(2013, Calendar.MARCH, 13, 10, 12, 10, 0);
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("scheduleQueryTestNoTaskWhenConnectionTaskIsPaused");

        ComTaskExecution comTaskExecution = createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(connectionTask, pastDate);
        final Instant futureDate = freezeClock(2013, Calendar.AUGUST, 5); // make the task pending
        OutboundComPort outboundComPort = createOutboundComPort();
        connectionTask.executionStarted(getOnlineComServer());
        Fetcher<ComTaskExecution> plannedComTaskExecutions = inMemoryPersistence.getCommunicationTaskService().getPlannedComTaskExecutionsFor(outboundComPort);

        assertThat(plannedComTaskExecutions.iterator().hasNext()).isFalse();
    }

    @Test
    @Transactional
    public void scheduleQueryTestNoTaskWhenComTaskIsObsoleteTest() {
        // we will make sure the ComServer field is filled in
        Instant pastDate = freezeClock(2013, Calendar.MARCH, 13, 10, 12, 10, 0);
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("scheduleQueryTestNoTaskWhenConnectionTaskIsPaused");

        ComTaskExecution comTaskExecution = createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(connectionTask, pastDate);
        final Instant futureDate = freezeClock(2013, Calendar.AUGUST, 5); // make the task pending
        OutboundComPort outboundComPort = createOutboundComPort();
        device.removeComTaskExecution(comTaskExecution);
        Fetcher<ComTaskExecution> plannedComTaskExecutions = inMemoryPersistence.getCommunicationTaskService().getPlannedComTaskExecutionsFor(outboundComPort);

        assertThat(plannedComTaskExecutions.iterator().hasNext()).isFalse();
    }

    @Test
    @Transactional
    public void scheduleQueryTestNoTaskWhenComPortPoolIsInactiveTest() {
        // we will make sure the ComServer field is filled in
        Instant pastDate = freezeClock(2013, Calendar.MARCH, 13, 10, 12, 10, 0);
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolationsUsingNonActiveComportPool("scheduleQueryTestNoTaskWhenConnectionTaskIsPaused");

        ComTaskExecution comTaskExecution = createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(connectionTask, pastDate);
        final Instant futureDate = freezeClock(2013, Calendar.AUGUST, 5); // make the task pending
        OutboundComPort outboundComPort = createOutboundComPort();

        Fetcher<ComTaskExecution> plannedComTaskExecutions = inMemoryPersistence.getCommunicationTaskService().getPlannedComTaskExecutionsFor(outboundComPort);

        assertThat(plannedComTaskExecutions.iterator().hasNext()).isFalse();
    }

    @Test
    @Transactional
    public void scheduleQueryTestNoTaskWhenNoPendingComTaskExecutionTest() {
        Instant pastDate = freezeClock(2013, Calendar.MARCH, 13, 10, 12, 10, 0);
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("simpleSchedulingQueryTest");

        ComTaskExecution comTaskExecution = createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(connectionTask, pastDate);
        final Instant futureDate = freezeClock(2012, Calendar.JULY, 5); // make the task waiting
        OutboundComPort outboundComPort = createOutboundComPort();
        Fetcher<ComTaskExecution> plannedComTaskExecutions = inMemoryPersistence.getCommunicationTaskService().getPlannedComTaskExecutionsFor(outboundComPort);

        assertThat(plannedComTaskExecutions.iterator().hasNext()).isFalse();
    }

    @Test
    @Transactional
    public void scheduleQueryTestNoTaskWhenComTaskExecutionAlreadyBusyTest() {
        Instant pastDate = freezeClock(2013, Calendar.MARCH, 13, 10, 12, 10, 0);
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("simpleSchedulingQueryTest");

        ComTaskExecution comTaskExecution = createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(connectionTask, pastDate);
        final Instant futureDate = freezeClock(2013, Calendar.AUGUST, 5); // make the task pending
        OutboundComPort outboundComPort = createOutboundComPort();
        ((ServerComTaskExecution) comTaskExecution).executionStarted(outboundComPort);
        Fetcher<ComTaskExecution> plannedComTaskExecutions = inMemoryPersistence.getCommunicationTaskService().getPlannedComTaskExecutionsFor(outboundComPort);

        assertThat(plannedComTaskExecutions.iterator().hasNext()).isFalse();
    }

    @Test
    @Transactional
    public void scheduleQueryTestNoTaskWhenComPortInOtherPoolTest() {
        Instant pastDate = freezeClock(2013, Calendar.MARCH, 13, 10, 12, 10, 0);
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("simpleSchedulingQueryTest");

        ComTaskExecution comTaskExecution = createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(connectionTask, pastDate);
        final Instant futureDate = freezeClock(2013, Calendar.AUGUST, 5); // make the task pending
        OutboundComPort outboundComPort = createComPortInOtherComPortPool();
        Fetcher<ComTaskExecution> plannedComTaskExecutions = inMemoryPersistence.getCommunicationTaskService().getPlannedComTaskExecutionsFor(outboundComPort);

        assertThat(plannedComTaskExecutions.iterator().hasNext()).isFalse();
    }

    @Test
    @Transactional
    public void orderByNextExecutionTimeStampTest() {
        Instant nextOne = freezeClock(2013, Calendar.MARCH, 13, 10, 12, 10, 0);
        Instant nextTwo = freezeClock(2013, Calendar.JANUARY, 30, 9, 1, 10, 0);
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("orderByNextExecutionTimeStampTest");
        ComTaskExecution comTaskExecution1 = createComTaskExecWithConnectionTaskNextDateAndComTaskEnablement(connectionTask, nextOne, comTaskEnablement1);
        ComTaskExecution comTaskExecution2 = createComTaskExecWithConnectionTaskNextDateAndComTaskEnablement(connectionTask, nextTwo, comTaskEnablement2);
        final Instant futureDate = freezeClock(2013, Calendar.AUGUST, 5); // make the task pending
        OutboundComPort outboundComPort = createOutboundComPort();
        Fetcher<ComTaskExecution> fetcher = inMemoryPersistence.getCommunicationTaskService().getPlannedComTaskExecutionsFor(outboundComPort);

        assertThat(fetcher).isNotNull();
        Iterator<ComTaskExecution> plannedComTaskExecutions = fetcher.iterator();
        assertThat(plannedComTaskExecutions.hasNext()).isTrue();
        assertThat(plannedComTaskExecutions.next().getId()).isEqualTo(comTaskExecution2.getId());
        assertThat(plannedComTaskExecutions.hasNext()).isTrue();
        assertThat(plannedComTaskExecutions.next().getId()).isEqualTo(comTaskExecution1.getId());
    }

    @Test
    @Transactional
    public void multipleTaskOneInFutureOneInPastTest() {
        Instant nextOne = freezeClock(2013, Calendar.MARCH, 13, 10, 12, 10, 0);
        Instant nextTwo = freezeClock(2025, Calendar.AUGUST, 23, 9, 1, 10, 0);
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("orderByNextExecutionTimeStampTest");
        ComTaskExecution comTaskExecution2 = createComTaskExecWithConnectionTaskNextDateAndComTaskEnablement(connectionTask, nextTwo, comTaskEnablement2);
        ComTaskExecution comTaskExecution1 = createComTaskExecWithConnectionTaskNextDateAndComTaskEnablement(connectionTask, nextOne, comTaskEnablement1);
        final Instant futureDate = freezeClock(2013, Calendar.AUGUST, 5); // make the task pending
        OutboundComPort outboundComPort = createOutboundComPort();
        Fetcher<ComTaskExecution> fetcher = inMemoryPersistence.getCommunicationTaskService().getPlannedComTaskExecutionsFor(outboundComPort);

        assertThat(fetcher).isNotNull();
        Iterator<ComTaskExecution> plannedComTaskExecutions = fetcher.iterator();
        assertThat(plannedComTaskExecutions.hasNext()).isTrue();
        assertThat(plannedComTaskExecutions.next().getId()).isEqualTo(comTaskExecution1.getId());
    }

    @Test
    @Transactional
    public void orderByConnectionTaskTest() {
        Instant nextOne = freezeClock(2013, Calendar.MARCH, 13, 10, 12, 10, 0);
        ScheduledConnectionTaskImpl connectionTask1 = this.createAsapWithNoPropertiesWithoutViolations("orderByConnectionTaskTest1");
        ScheduledConnectionTaskImpl connectionTask2 = this.createOtherAsapWithNoPropertiesWithoutViolations("orderByConnectionTaskTest2");
        ComTaskExecution comTaskExecution1 = createComTaskExecWithConnectionTaskNextDateAndComTaskEnablement(connectionTask1, nextOne, comTaskEnablement1);
        ComTaskExecution comTaskExecution2 = createComTaskExecWithConnectionTaskNextDateAndComTaskEnablement(connectionTask2, nextOne, comTaskEnablement2);
        ComTaskExecution comTaskExecution3 = createComTaskExecWithConnectionTaskNextDateAndComTaskEnablement(connectionTask1, nextOne, comTaskEnablement3);
        final Instant futureDate = freezeClock(2013, Calendar.AUGUST, 5); // make the tasks pending
        OutboundComPort outboundComPort = createOutboundComPort();
        Fetcher<ComTaskExecution> fetcher = inMemoryPersistence.getCommunicationTaskService().getPlannedComTaskExecutionsFor(outboundComPort);

        assertThat(fetcher).isNotNull();
        Iterator<ComTaskExecution> plannedComTaskExecutions = fetcher.iterator();
        assertThat(plannedComTaskExecutions.hasNext()).isTrue();
        assertThat(plannedComTaskExecutions.next().getId()).isEqualTo(comTaskExecution1.getId());
        assertThat(plannedComTaskExecutions.hasNext()).isTrue();
        assertThat(plannedComTaskExecutions.next().getId()).isEqualTo(comTaskExecution3.getId());
        assertThat(plannedComTaskExecutions.hasNext()).isTrue();
        assertThat(plannedComTaskExecutions.next().getId()).isEqualTo(comTaskExecution2.getId());
    }

}