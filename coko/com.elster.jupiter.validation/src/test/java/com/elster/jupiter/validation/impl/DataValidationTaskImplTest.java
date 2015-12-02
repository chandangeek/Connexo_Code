package com.elster.jupiter.validation.impl;

import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableList;

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
    @Mock
    private UsagePointGroup usagePointGroup;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ValidationService dataValidationService;
    @Mock
    private DestinationSpec destinationSpec;
    @Mock
    private QueryExecutor queryExecutor;
    @Mock
    private RecurrentTask recurrentTask;
    @Mock
    private RecurrentTaskBuilder taskBuilder;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;

    private DataValidationTaskImpl newTask() {
        DataValidationTaskImpl newTask = new DataValidationTaskImpl(dataModel,taskService,dataValidationService,thesaurus);
        newTask.setRecurrentTask(recurrentTask);
        return newTask;
    }

    private DataValidationTaskImpl setId(DataValidationTaskImpl entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
        return entity;
    }

    @Override
    protected Object getInstanceA() {
        if (validationTask == null) {
            validationTask = setId(newTask().init("taskname", Instant.now() ,dataValidationService, "MultiSense"), ID);
        }
        return validationTask;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return setId(newTask().init("taskname", Instant.now(), dataValidationService,"MultiSense"), ID);
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

    @Before
    public void setUp() {
        taskBuilder = FakeBuilder.initBuilderStub(recurrentTask, RecurrentTaskBuilder.class,
                RecurrentTaskBuilder.RecurrentTaskBuilderNameSetter.class,
                RecurrentTaskBuilder.RecurrentTaskBuilderDestinationSetter.class,
                RecurrentTaskBuilder.RecurrentTaskBuilderPayloadSetter.class,
                RecurrentTaskBuilder.RecurrentTaskBuilderScheduleSetter.class,
                RecurrentTaskBuilder.RecurrentTaskBuilderFinisher.class
        );
        when(taskService.newBuilder()).thenReturn(taskBuilder);
        when(recurrentTask.getLastOccurrence()).thenReturn(Optional.empty());
        when(recurrentTask.getName()).thenReturn("testname");


        doNothing().when(recurrentTask).setNextExecution(any(Instant.class));
        doNothing().when(recurrentTask).save();

        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), any())).thenReturn(Collections.emptySet());

        when(dataValidationService.getDestination()).thenReturn(destinationSpec);

    }

    @Test
    public void testPersist() {
        DataValidationTaskImpl testPersistDataValidationTask = newTask();
        testPersistDataValidationTask.setName("testname");
        testPersistDataValidationTask.setEndDeviceGroup(endDeviceGroup);
        testPersistDataValidationTask.save();
        verify(dataModel).persist(testPersistDataValidationTask);
    }
    
    @Test
    public void testPersistUsagePointGroup() {
        DataValidationTaskImpl testPersistDataValidationTask = newTask();
        testPersistDataValidationTask.setName("testname");
        testPersistDataValidationTask.setUsagePointGroup(usagePointGroup);
        testPersistDataValidationTask.save();
        verify(dataModel).persist(testPersistDataValidationTask);
    }

    @Test
    public void testUpdate() {
        DataValidationTaskImpl testUpdateDataValidationTask = newTask();
        testUpdateDataValidationTask.setName("taskname");
        testUpdateDataValidationTask.setEndDeviceGroup(endDeviceGroup);
        testUpdateDataValidationTask.setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)));
        field("id").ofType(Long.TYPE).in(testUpdateDataValidationTask).set(ID);
        when(recurrentTask.getName()).thenReturn("taskname");
        testUpdateDataValidationTask.save();
        verify(dataModel).update(testUpdateDataValidationTask);
    }
    
    @Test
    public void testUpdateUsagePointGroupt() {
        DataValidationTaskImpl testUpdateDataValidationTask = newTask();
        testUpdateDataValidationTask.setName("taskname");
        testUpdateDataValidationTask.setUsagePointGroup(usagePointGroup);
        testUpdateDataValidationTask.setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)));
        field("id").ofType(Long.TYPE).in(testUpdateDataValidationTask).set(ID);
        testUpdateDataValidationTask.save();
        verify(dataModel).update(testUpdateDataValidationTask);
    }

    @Test
    public void testDelete() {
        DataValidationTaskImpl task = newTask();
        task.setEndDeviceGroup(endDeviceGroup);
        field("id").ofType(Long.TYPE).in(task).set(ID);
        task.save();
        verify(dataModel).update(task);

        when(dataModel.query(any(), any())).thenReturn(queryExecutor);
        when(queryExecutor.select(any(), any(), any(), any(), any())).thenReturn(new ArrayList<DataValidationOccurrence>());

        DataMapper<DataValidationOccurrence> mapper = mock(DataMapper.class);
        when(dataModel.mapper(DataValidationOccurrence.class)).thenReturn(mapper);
        when(mapper.find(any(String.class), any(DataValidationTaskImpl.class))).thenReturn(new ArrayList<DataValidationOccurrence>());
        doNothing().when(mapper).remove(anyList());
        doNothing().when(recurrentTask).delete();

        task.delete();
        verify(recurrentTask).delete();
        verify(dataModel).remove(task);
    }
    
    @Test
    public void testDeleteUsagePoint() {
        DataValidationTaskImpl task = newTask();
        task.setUsagePointGroup(usagePointGroup);
        task.setName("taskname");
        field("id").ofType(Long.TYPE).in(task).set(ID);
        task.save();
        verify(dataModel).update(task);

        when(dataModel.query(any(), any())).thenReturn(queryExecutor);
        when(queryExecutor.select(any(), any(), any(), any(), any())).thenReturn(new ArrayList<DataValidationOccurrence>());

        DataMapper<DataValidationOccurrence> mapper = mock(DataMapper.class);
        when(dataModel.mapper(DataValidationOccurrence.class)).thenReturn(mapper);
        when(mapper.find(any(String.class), any(DataValidationTaskImpl.class))).thenReturn(new ArrayList<DataValidationOccurrence>());
        doNothing().when(mapper).remove(anyList());
        doNothing().when(recurrentTask).delete();

        task.delete();
        verify(recurrentTask).delete();
        verify(dataModel).remove(task);
    }


}
