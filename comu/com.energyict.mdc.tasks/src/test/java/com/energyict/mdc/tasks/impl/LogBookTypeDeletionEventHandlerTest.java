/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LogBooksTask;

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

/**
 * Tests the {@link LogBookTypeDeletionEventHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-21 (11:14)
 */
@RunWith(MockitoJUnitRunner.class)
public class LogBookTypeDeletionEventHandlerTest {

    @Mock
    private ServerTaskService taskService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private LogBookType logBookType;

    private int comTaskId;

    @Before
    public void initializeMocks() {
        when(this.taskService.getThesaurus()).thenReturn(this.thesaurus);
        when(this.logBookType.getName()).thenReturn(LogBookTypeDeletionEventHandlerTest.class.getSimpleName());
    }

    @Test
    public void testNotInUse() {
        when(this.taskService.findTasksUsing(this.logBookType)).thenReturn(Collections.emptyList());
        LogBookTypeDeletionEventHandler eventHandler = this.newTestInstance();

        // Business method
        eventHandler.handle(this.deletionEvent(this.logBookType));

        // Asserts
        verify(this.taskService).findTasksUsing(this.logBookType);
    }

    @Test(expected = VetoDeleteLogBookTypeException.class)
    public void testInUseBySingleTask() {
        LogBooksTask task = this.mockedLogBooksTask();
        when(this.taskService.findTasksUsing(this.logBookType)).thenReturn(Arrays.asList(task));
        LogBookTypeDeletionEventHandler eventHandler = this.newTestInstance();

        // Business method
        eventHandler.handle(this.deletionEvent(this.logBookType));

        // Asserts: see expected exception rule
    }

    @Test(expected = VetoDeleteLogBookTypeException.class)
    public void testInUseByMultipleTasks() {
        LogBooksTask task1 = this.mockedLogBooksTask();
        LogBooksTask task2 = this.mockedLogBooksTask();
        LogBooksTask task3 = this.mockedLogBooksTask();
        when(this.taskService.findTasksUsing(this.logBookType)).thenReturn(Arrays.asList(task1, task2, task3));
        LogBookTypeDeletionEventHandler eventHandler = this.newTestInstance();

        // Business method
        eventHandler.handle(this.deletionEvent(this.logBookType));

        // Asserts: see expected exception rule
    }

    private LogBookTypeDeletionEventHandler newTestInstance() {
        return new LogBookTypeDeletionEventHandler(this.taskService);
    }

    private LocalEvent deletionEvent(LogBookType logBookType) {
        LocalEvent event = mock(LocalEvent.class);
        when(event.getSource()).thenReturn(logBookType);
        return event;
    }

    private LogBooksTask mockedLogBooksTask() {
        ComTask comTask = mock(ComTask.class);
        when(comTask.getName()).thenReturn(LogBookTypeDeletionEventHandlerTest.class.getSimpleName() + this.comTaskId++);
        return this.mockedLogBooksTask(comTask);
    }

    private LogBooksTask mockedLogBooksTask(ComTask comTask) {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        when(logBooksTask.getComTask()).thenReturn(comTask);
        return logBooksTask;
    }

}