package com.elster.jupiter.validation.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataValidationTaskImplTest extends EqualsContractTest {

    public static final long ID = 2415151L;
    public static final long OTHER_ID = 615585L;

    private DataValidationTaskImpl validationTask;

    @Mock
    private DataModel dataModel;
    @Mock
    private TaskService taskService;
    @Mock
    private EndDeviceGroup endDeviceGroup;

    private DataValidationTaskImpl newTask() {
        return new DataValidationTaskImpl(dataModel,taskService);
    }

    private DataValidationTaskImpl setId(DataValidationTaskImpl entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
        return entity;
    }

    @Override
    protected Object getInstanceA() {
        if (validationTask == null) {
            validationTask = setId(newTask(), ID);
        }
        return validationTask;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return setId(newTask(), ID);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(setId(newTask(), OTHER_ID));
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }


    @Test
    public void testPersist() {
        RecurrentTask recurrentTask = mock(RecurrentTask.class);
        RecurrentTaskBuilder taskBuilder = mock(RecurrentTaskBuilder.class);
        when(taskService.newBuilder()).thenReturn(taskBuilder);
        when(taskBuilder.setName(any(String.class))).thenReturn(taskBuilder);
        when(taskBuilder.setScheduleExpression(any(ScheduleExpression.class))).thenReturn(taskBuilder);
        when(taskBuilder.setPayLoad(any(String.class))).thenReturn(taskBuilder);
        when(taskBuilder.build()).thenReturn(recurrentTask);

        doNothing().when(recurrentTask).setNextExecution(any(Instant.class));
        doNothing().when(recurrentTask).save();

        ValidatorFactory validatorFactory = mock(ValidatorFactory.class);
        Validator validator = mock(Validator.class);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), any())).thenReturn(Collections.emptySet());
        DataValidationTaskImpl testPersistDataValidationTask = newTask();
        testPersistDataValidationTask.setName("testname");
        testPersistDataValidationTask.setEndDeviceGroup(endDeviceGroup);
        testPersistDataValidationTask.save();
        verify(dataModel).persist(testPersistDataValidationTask);
    }

    @Test
    public void testUpdate() {
        RecurrentTask recurrentTask = mock(RecurrentTask.class);
        RecurrentTaskBuilder taskBuilder = mock(RecurrentTaskBuilder.class);
        when(taskService.newBuilder()).thenReturn(taskBuilder);
        when(taskBuilder.setName(any(String.class))).thenReturn(taskBuilder);
        when(taskBuilder.setScheduleExpression(any(ScheduleExpression.class))).thenReturn(taskBuilder);
        when(taskBuilder.setPayLoad(any(String.class))).thenReturn(taskBuilder);
        when(taskBuilder.build()).thenReturn(recurrentTask);

        doNothing().when(recurrentTask).setNextExecution(any(Instant.class));
        doNothing().when(recurrentTask).save();

        ValidatorFactory validatorFactory = mock(ValidatorFactory.class);
        Validator validator = mock(Validator.class);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), any())).thenReturn(Collections.emptySet());

        DataValidationTaskImpl testUpdateDataValidationTask = newTask();
        testUpdateDataValidationTask.setName("taskname");
        testUpdateDataValidationTask.setEndDeviceGroup(endDeviceGroup);
        testUpdateDataValidationTask.setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)));
        field("id").ofType(Long.TYPE).in(testUpdateDataValidationTask).set(ID);
        testUpdateDataValidationTask.save();
        verify(dataModel).update(testUpdateDataValidationTask);
    }

    @Test
    public void testDelete() {
        DataValidationTaskImpl task = newTask();
        task.setEndDeviceGroup(endDeviceGroup);
        task.setName("taskname");
        field("id").ofType(Long.TYPE).in(task).set(ID);
        task.save();
        verify(dataModel).update(task);
        task.delete();
        verify(dataModel).remove(task);
    }

}
