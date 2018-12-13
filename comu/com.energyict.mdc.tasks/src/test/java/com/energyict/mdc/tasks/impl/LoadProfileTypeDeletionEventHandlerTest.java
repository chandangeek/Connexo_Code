/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoadProfileTypeDeletionEventHandlerTest {

    @Mock
    private ServerTaskService taskService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private LoadProfileType loadProfileType;

    private int comTaskId;

    @Before
    public void initializeMocks() {
        when(this.taskService.getThesaurus()).thenReturn(this.thesaurus);
        when(this.loadProfileType.getName()).thenReturn(LoadProfileTypeDeletionEventHandlerTest.class.getSimpleName());
    }

    @Test
    public void testNotInUse() {
        when(this.taskService.findTasksUsing(this.loadProfileType)).thenReturn(Collections.emptyList());
        LoadProfileTypeDeletionEventHandler eventHandler = this.newTestInstance();
        eventHandler.handle(this.deletionEvent(this.loadProfileType));
        verify(this.taskService).findTasksUsing(this.loadProfileType);
    }

    @Test(expected = VetoDeleteLoadProfileTypeException.class)
    public void testInUseBySingleTask() {
        LoadProfilesTask task = this.mockedLoadProfileTask();
        when(this.taskService.findTasksUsing(this.loadProfileType)).thenReturn(Arrays.asList(task));
        LoadProfileTypeDeletionEventHandler eventHandler = this.newTestInstance();
        eventHandler.handle(this.deletionEvent(this.loadProfileType));
    }

    @Test(expected = VetoDeleteLoadProfileTypeException.class)
    public void testInUseByMultipleTasks() {
        LoadProfilesTask task1 = this.mockedLoadProfileTask();
        LoadProfilesTask task2 = this.mockedLoadProfileTask();
        LoadProfilesTask task3 = this.mockedLoadProfileTask();
        when(this.taskService.findTasksUsing(this.loadProfileType)).thenReturn(Arrays.asList(task1, task2, task3));
        LoadProfileTypeDeletionEventHandler eventHandler = this.newTestInstance();
        eventHandler.handle(this.deletionEvent(this.loadProfileType));
    }

    private LoadProfileTypeDeletionEventHandler newTestInstance() {
        return new LoadProfileTypeDeletionEventHandler(this.taskService);
    }

    private LocalEvent deletionEvent(LoadProfileType loadProfileType) {
        LocalEvent event = mock(LocalEvent.class);
        when(event.getSource()).thenReturn(loadProfileType);
        return event;
    }

    private LoadProfilesTask mockedLoadProfileTask() {
        ComTask comTask = mock(ComTask.class);
        when(comTask.getName()).thenReturn(LoadProfileTypeDeletionEventHandlerTest.class.getSimpleName() + this.comTaskId++);
        return this.mockedLoadProfileTask(comTask);
    }

    private LoadProfilesTask mockedLoadProfileTask(ComTask comTask) {
        LoadProfilesTask loadProfileTask = mock(LoadProfilesTask.class);
        when(loadProfileTask.getComTask()).thenReturn(comTask);
        return loadProfileTask;
    }
}