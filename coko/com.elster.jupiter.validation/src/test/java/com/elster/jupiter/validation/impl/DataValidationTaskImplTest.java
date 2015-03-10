package com.elster.jupiter.validation.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationAction;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DataValidationTaskImplTest extends EqualsContractTest {

    public static final long ID = 2415151L;
    public static final long OTHER_ID = 615585L;

    private DataValidationTaskImpl validationTask;

    @Mock
    private DataModel dataModel;

    @Mock
    private EndDeviceGroup endDeviceGroup;

    private DataValidationTaskImpl newTask() {
        return new DataValidationTaskImpl(dataModel);
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
        DataValidationTaskImpl testPersistDataValidationTask = newTask();
        testPersistDataValidationTask.setName("testname");
        testPersistDataValidationTask.setEndDeviceGroup(endDeviceGroup);
        testPersistDataValidationTask.save();
        verify(dataModel).persist(testPersistDataValidationTask);
    }

    @Test
    public void testUpdate() {
        DataValidationTaskImpl testUpdateDataValidationTask = newTask();
        testUpdateDataValidationTask.setName("taskname");
        testUpdateDataValidationTask.setEndDeviceGroup(endDeviceGroup);
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
