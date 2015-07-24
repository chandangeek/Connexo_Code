package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.RegistersTask;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Arrays;
import java.util.Collections;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link RegisterGroupDeletionEventHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-13 (15:31)
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterGroupDeletionEventHandlerTest {

    @Mock
    private ServerTaskService taskService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private RegisterGroup registerGroup;

    private int comTaskId;

    @Before
    public void initializeMocks() {
        when(this.taskService.getThesaurus()).thenReturn(this.thesaurus);
        when(this.registerGroup.getName()).thenReturn(RegisterGroupDeletionEventHandlerTest.class.getSimpleName());
    }

    @Test
    public void topicMatcherIsNotEmpty() {
        RegisterGroupDeletionEventHandler eventHandler = this.newTestInstance();

        // Business method
        String topicMatcher = eventHandler.getTopicMatcher();

        // Asserts
        assertThat(topicMatcher).isNotEmpty();
    }

    @Test
    public void testNotInUse() {
        when(this.taskService.findTasksUsing(this.registerGroup)).thenReturn(Collections.emptyList());
        RegisterGroupDeletionEventHandler eventHandler = this.newTestInstance();
        eventHandler.handle(this.deletionEvent(this.registerGroup));
        verify(this.taskService).findTasksUsing(this.registerGroup);
    }

    @Test(expected = VetoDeleteRegisterGroupException.class)
    public void testInUseBySingleTask() {
        RegistersTask task = this.mockedRegistersTask();
        when(this.taskService.findTasksUsing(this.registerGroup)).thenReturn(Arrays.asList(task));
        RegisterGroupDeletionEventHandler eventHandler = this.newTestInstance();
        eventHandler.handle(this.deletionEvent(this.registerGroup));
    }

    @Test(expected = VetoDeleteRegisterGroupException.class)
    public void testInUseByMultipleTasks() {
        RegistersTask task1 = this.mockedRegistersTask();
        RegistersTask task2 = this.mockedRegistersTask();
        RegistersTask task3 = this.mockedRegistersTask();
        when(this.taskService.findTasksUsing(this.registerGroup)).thenReturn(Arrays.asList(task1, task2, task3));
        RegisterGroupDeletionEventHandler eventHandler = this.newTestInstance();
        eventHandler.handle(this.deletionEvent(this.registerGroup));
    }

    private RegisterGroupDeletionEventHandler newTestInstance() {
        return new RegisterGroupDeletionEventHandler(this.taskService);
    }

    private LocalEvent deletionEvent(RegisterGroup registerGroup) {
        LocalEvent event = mock(LocalEvent.class);
        when(event.getSource()).thenReturn(registerGroup);
        return event;
    }

    private RegistersTask mockedRegistersTask() {
        ComTask comTask = mock(ComTask.class);
        when(comTask.getName()).thenReturn(RegisterGroupDeletionEventHandlerTest.class.getSimpleName() + this.comTaskId++);
        return this.mockedRegistersTask(comTask);
    }

    private RegistersTask mockedRegistersTask(ComTask comTask) {
        RegistersTask loadProfileTask = mock(RegistersTask.class);
        when(loadProfileTask.getComTask()).thenReturn(comTask);
        return loadProfileTask;
    }

}