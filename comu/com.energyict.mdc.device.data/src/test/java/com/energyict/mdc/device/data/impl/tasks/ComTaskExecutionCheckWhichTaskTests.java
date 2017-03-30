/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.TopologyTask;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore("Please mock all dependencies of the ScheduledComTaskExecutionImpl ")
public class ComTaskExecutionCheckWhichTaskTests {

    private void initializeWithComTask(ComTaskExecutionImpl comTaskExecution, ComTask comTask) {
        Device device = mock(Device.class);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        comTaskExecution.initializeFrom(device, comTaskEnablement);
    }

    private ComTaskExecutionImpl createComTaskExecution() {
        return new ComTaskExecutionImpl(null, null, null, null, null, null);
    }

    @Test
    public void topologyTaskTest() {
        TopologyTask topologyTask = mock(TopologyTask.class);
        ComTask comTask = mock(ComTask.class);
        final List<? extends ProtocolTask> protocolTasks = Arrays.asList(topologyTask);
        when(comTask.getProtocolTasks()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return protocolTasks;
            }
        });

        ComTaskExecutionImpl comTaskExecution = createComTaskExecution();
        initializeWithComTask(comTaskExecution, comTask);

        assertThat(comTaskExecution.isConfiguredToUpdateTopology()).isTrue();
        assertThat(comTaskExecution.isConfiguredToCheckClock()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectEvents()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectLoadProfileData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectRegisterData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToReadStatusInformation()).isFalse();
        assertThat(comTaskExecution.isConfiguredToRunBasicChecks()).isFalse();
        assertThat(comTaskExecution.isConfiguredToSendMessages()).isFalse();
    }

    @Test
    public void checkClockTaskTest() {
        ClockTask checkClockTask = mock(ClockTask.class);
        ComTask comTask = mock(ComTask.class);
        final List<? extends ProtocolTask> protocolTasks = Arrays.asList(checkClockTask);
        when(comTask.getProtocolTasks()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return protocolTasks;
            }
        });

        ComTaskExecutionImpl comTaskExecution = createComTaskExecution();
        initializeWithComTask(comTaskExecution, comTask);

        assertThat(comTaskExecution.isConfiguredToUpdateTopology()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCheckClock()).isTrue();
        assertThat(comTaskExecution.isConfiguredToCollectEvents()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectLoadProfileData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectRegisterData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToReadStatusInformation()).isFalse();
        assertThat(comTaskExecution.isConfiguredToRunBasicChecks()).isFalse();
        assertThat(comTaskExecution.isConfiguredToSendMessages()).isFalse();
    }

    @Test
    public void collectEventsTaskTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        ComTask comTask = mock(ComTask.class);
        final List<? extends ProtocolTask> protocolTasks = Arrays.asList(logBooksTask);
        when(comTask.getProtocolTasks()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return protocolTasks;
            }
        });

        ComTaskExecutionImpl comTaskExecution = createComTaskExecution();
        initializeWithComTask(comTaskExecution, comTask);

        assertThat(comTaskExecution.isConfiguredToUpdateTopology()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCheckClock()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectEvents()).isTrue();
        assertThat(comTaskExecution.isConfiguredToCollectLoadProfileData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectRegisterData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToReadStatusInformation()).isFalse();
        assertThat(comTaskExecution.isConfiguredToRunBasicChecks()).isFalse();
        assertThat(comTaskExecution.isConfiguredToSendMessages()).isFalse();
    }

    @Test
    public void loadProfileTaskTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        ComTask comTask = mock(ComTask.class);
        final List<? extends ProtocolTask> protocolTasks = Arrays.asList(loadProfilesTask);
        when(comTask.getProtocolTasks()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return protocolTasks;
            }
        });

        ComTaskExecutionImpl comTaskExecution = createComTaskExecution();
        initializeWithComTask(comTaskExecution, comTask);

        assertThat(comTaskExecution.isConfiguredToUpdateTopology()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCheckClock()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectEvents()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectLoadProfileData()).isTrue();
        assertThat(comTaskExecution.isConfiguredToCollectRegisterData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToReadStatusInformation()).isFalse();
        assertThat(comTaskExecution.isConfiguredToRunBasicChecks()).isFalse();
        assertThat(comTaskExecution.isConfiguredToSendMessages()).isFalse();
    }

    @Test
    public void registerDataTest() {
        RegistersTask registersTask = mock(RegistersTask.class);
        ComTask comTask = mock(ComTask.class);
        final List<? extends ProtocolTask> protocolTasks = Arrays.asList(registersTask);
        when(comTask.getProtocolTasks()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return protocolTasks;
            }
        });

        ComTaskExecutionImpl comTaskExecution = createComTaskExecution();
        initializeWithComTask(comTaskExecution, comTask);

        assertThat(comTaskExecution.isConfiguredToUpdateTopology()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCheckClock()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectEvents()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectLoadProfileData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectRegisterData()).isTrue();
        assertThat(comTaskExecution.isConfiguredToReadStatusInformation()).isFalse();
        assertThat(comTaskExecution.isConfiguredToRunBasicChecks()).isFalse();
        assertThat(comTaskExecution.isConfiguredToSendMessages()).isFalse();
    }

    @Test
    public void statusInformationTaskTest() {
        StatusInformationTask statusInformationTask = mock(StatusInformationTask.class);
        ComTask comTask = mock(ComTask.class);
        final List<? extends ProtocolTask> protocolTasks = Arrays.asList(statusInformationTask);
        when(comTask.getProtocolTasks()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return protocolTasks;
            }
        });

        ComTaskExecutionImpl comTaskExecution = createComTaskExecution();
        initializeWithComTask(comTaskExecution, comTask);

        assertThat(comTaskExecution.isConfiguredToUpdateTopology()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCheckClock()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectEvents()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectLoadProfileData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectRegisterData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToReadStatusInformation()).isTrue();
        assertThat(comTaskExecution.isConfiguredToRunBasicChecks()).isFalse();
        assertThat(comTaskExecution.isConfiguredToSendMessages()).isFalse();
    }

    @Test
    public void basicCheckTaskTest() {
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        ComTask comTask = mock(ComTask.class);
        final List<? extends ProtocolTask> protocolTasks = Arrays.asList(basicCheckTask);
        when(comTask.getProtocolTasks()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return protocolTasks;
            }
        });

        ComTaskExecutionImpl comTaskExecution = createComTaskExecution();
        initializeWithComTask(comTaskExecution, comTask);

        assertThat(comTaskExecution.isConfiguredToUpdateTopology()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCheckClock()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectEvents()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectLoadProfileData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectRegisterData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToReadStatusInformation()).isFalse();
        assertThat(comTaskExecution.isConfiguredToRunBasicChecks()).isTrue();
        assertThat(comTaskExecution.isConfiguredToSendMessages()).isFalse();
    }

    @Test
    public void messagesTaskTest() {
        MessagesTask messagesTask = mock(MessagesTask.class);
        ComTask comTask = mock(ComTask.class);
        final List<? extends ProtocolTask> protocolTasks = Arrays.asList(messagesTask);
        when(comTask.getProtocolTasks()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return protocolTasks;
            }
        });

        ComTaskExecutionImpl comTaskExecution = createComTaskExecution();
        initializeWithComTask(comTaskExecution, comTask);

        assertThat(comTaskExecution.isConfiguredToUpdateTopology()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCheckClock()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectEvents()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectLoadProfileData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectRegisterData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToReadStatusInformation()).isFalse();
        assertThat(comTaskExecution.isConfiguredToRunBasicChecks()).isFalse();
        assertThat(comTaskExecution.isConfiguredToSendMessages()).isTrue();
    }

    @Test
    public void allTasksTest() {
        MessagesTask messagesTask = mock(MessagesTask.class);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        RegistersTask registersTask = mock(RegistersTask.class);
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        ClockTask clockTask = mock(ClockTask.class);
        TopologyTask topologyTask = mock(TopologyTask.class);
        StatusInformationTask statusInformationTask = mock(StatusInformationTask.class);
        ComTask comTask = mock(ComTask.class);
        final List<? extends ProtocolTask> protocolTasks = Arrays.asList(messagesTask, loadProfilesTask, logBooksTask, registersTask, basicCheckTask, clockTask, topologyTask, statusInformationTask);
        when(comTask.getProtocolTasks()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return protocolTasks;
            }
        });

        ComTaskExecutionImpl comTaskExecution = createComTaskExecution();
        initializeWithComTask(comTaskExecution, comTask);

        assertThat(comTaskExecution.isConfiguredToUpdateTopology()).isTrue();
        assertThat(comTaskExecution.isConfiguredToCheckClock()).isTrue();
        assertThat(comTaskExecution.isConfiguredToCollectEvents()).isTrue();
        assertThat(comTaskExecution.isConfiguredToCollectLoadProfileData()).isTrue();
        assertThat(comTaskExecution.isConfiguredToCollectRegisterData()).isTrue();
        assertThat(comTaskExecution.isConfiguredToReadStatusInformation()).isTrue();
        assertThat(comTaskExecution.isConfiguredToRunBasicChecks()).isTrue();
        assertThat(comTaskExecution.isConfiguredToSendMessages()).isTrue();
    }

    @Test
    public void logBooksAndLoadProfiles() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        ComTask comTask = mock(ComTask.class);
        final List<? extends ProtocolTask> protocolTasks = Arrays.asList(loadProfilesTask, logBooksTask);
        when(comTask.getProtocolTasks()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return protocolTasks;
            }
        });

        ComTaskExecutionImpl comTaskExecution = createComTaskExecution();
        initializeWithComTask(comTaskExecution, comTask);

        assertThat(comTaskExecution.isConfiguredToUpdateTopology()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCheckClock()).isFalse();
        assertThat(comTaskExecution.isConfiguredToCollectEvents()).isTrue();
        assertThat(comTaskExecution.isConfiguredToCollectLoadProfileData()).isTrue();
        assertThat(comTaskExecution.isConfiguredToCollectRegisterData()).isFalse();
        assertThat(comTaskExecution.isConfiguredToReadStatusInformation()).isFalse();
        assertThat(comTaskExecution.isConfiguredToRunBasicChecks()).isFalse();
        assertThat(comTaskExecution.isConfiguredToSendMessages()).isFalse();
    }

}
