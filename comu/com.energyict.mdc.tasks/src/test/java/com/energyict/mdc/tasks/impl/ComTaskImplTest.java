package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.PersistenceTest;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link com.energyict.mdc.tasks.impl.ComTaskImpl} component.
 *
 * @author gna
 * @since 7/05/12 - 11:46
 */
public class ComTaskImplTest extends PersistenceTest {

    private static final boolean STORE_DATA_TRUE = true;
    private static final String COM_TASK_NAME = "UniqueComTaskName";

    private ComTask createSimpleComTaskWithStatusInformation() {
        ComTask comTask = getTaskService().newComTask(COM_TASK_NAME);
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        comTask.createStatusInformationTask();
        comTask.save();
        return getTaskService().findComTask(comTask.getId());
    }

    private ComTask createComTaskWithBasicCheckAndStatusInformation() {
        ComTask comTask = getTaskService().newComTask(COM_TASK_NAME);
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        comTask.createStatusInformationTask();
        comTask.createBasicCheckTask().add();
        comTask.save();
        return getTaskService().findComTask(comTask.getId()); // to make sure all elements in the composition are properly loaded
    }

//    private ComTaskShadow createComTaskShadowWithoutViolations () {
//        ComTaskShadow shadow = createSimpleComTask();
//        shadow.addProtocolTask(BasicCheckTaskImplTest.createBasicCheckTaskShadow());
//        return shadow;
//    }
//
//    private ComTask createComTaskWithBasicAndClockTask () throws BusinessException, SQLException {
//        return createSimpleComTask(createComTaskShadowWithBasicAndClockTask());
//    }
//
//    private ComTaskShadow createComTaskShadowWithBasicAndClockTask () {
//        ComTaskShadow shadow = createSimpleComTask();
//        BasicCheckTaskShadow basicCheckTaskShadow = BasicCheckTaskImplTest.createBasicCheckTaskShadow();
//        ClockTaskShadow clockTaskShadow = ClockTaskImplTest.createClockTaskShadow();
//        shadow.setProtocolTaskShadows(new ShadowList<>(Arrays.asList(basicCheckTaskShadow, clockTaskShadow)));
//        return shadow;
//    }

    @Test
    public void testGetTypeDoesNotReturnServerBasedClassName () {
        ComTask comTask = getTaskService().newComTask(COM_TASK_NAME);

        // Business method
        String type = comTask.getType();

        // Asserts
        assertThat(type).doesNotContain(".Server");
    }



    @Test
    @Transactional
    public void createWithoutViolations() {
        ComTask comTask = createSimpleComTaskWithStatusInformation();
        // asserts
        assertThat(comTask).isNotNull();
        assertThat(comTask.storeData()).isTrue();
    }



    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.CAN_NOT_BE_EMPTY+"}", property = "name")
    public void createWithWhitespaceNameTest()  {
        ComTask comTask = getTaskService().newComTask("");
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        comTask.createStatusInformationTask();
        comTask.save();
    }

    @Test
    @Transactional
    public void createWithoutViolationsWithProtocolTasks() {
        ComTask comTask = getTaskService().newComTask("name");
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        comTask.createBasicCheckTask().verifyClockDifference(true).add();
        comTask.createClockTask(ClockTaskType.SETCLOCK).maximumClockDifference(TimeDuration.days(1)).minimumClockDifference(TimeDuration.hours(1)).add();
        comTask.save();
        assertThat(comTask).isNotNull();
        assertThat(comTask.storeData()).isTrue();
        assertThat(comTask.getProtocolTasks()).hasSize(2);
    }

    @Test
    @Transactional
    @Expected(value = TranslatableApplicationException.class, message = "ComTask contains multiple ProtocolTasks of the same type")
    public void createWithSameProtocolTaskTypes() {
        ComTask comTask = getTaskService().newComTask(COM_TASK_NAME);
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        comTask.createStatusInformationTask();
        comTask.createBasicCheckTask().verifyClockDifference(true).add();
        comTask.createBasicCheckTask().verifyClockDifference(true).add();
        comTask.save();
    }

    @Test
    @Transactional
    @Expected(value = TranslatableApplicationException.class, message = "ComTask contains multiple ProtocolTasks of the same type")
    public void updateWithSameProtocolTaskTypeTest()  {
        ComTask comTask = createSimpleComTaskWithStatusInformation();
        comTask.createBasicCheckTask().verifyClockDifference(true).add();
        comTask.createBasicCheckTask().verifyClockDifference(true).add();
    }

    @Test
    @Transactional
    public void updateProtocolTaskTest() {
        ComTask comTask = createComTaskWithBasicCheckAndStatusInformation();
        BasicCheckTask basicCheckTask = getTaskByType(comTask.getProtocolTasks(), BasicCheckTask.class);
        assertThat(basicCheckTask.verifySerialNumber()).isFalse();
        basicCheckTask.setVerifySerialNumber(true);
        basicCheckTask.save();

        BasicCheckTask refreshedBasicCheckTask = getTaskByType(comTask.getProtocolTasks(), BasicCheckTask.class);
        // asserts
        assertThat(getTaskByType(comTask.getProtocolTasks(), BasicCheckTask.class).verifySerialNumber()).isTrue();
    }

    @Test
    @Transactional
    public void updateWithDeleteAndNewProtocolTaskTypeTest() {
        ComTask comTask = createComTaskWithBasicCheckAndStatusInformation();
        ClockTask clockTask = comTask.createClockTask(ClockTaskType.SETCLOCK).maximumClockDifference(TimeDuration.days(1)).minimumClockDifference(TimeDuration.hours(1)).add();
        comTask.save();
        comTask.removeTask(clockTask);
        comTask.save();
        assertThat(comTask.getProtocolTasks()).hasSize(2);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.DUPLICATE_COMTASK_NAME+"}", property = "name")
    public void createWithDuplicateNameTest()  {
        createSimpleComTaskWithStatusInformation();
        createSimpleComTaskWithStatusInformation();
        // we expect to fail with a duplicate exception
    }

    @Test
    @Transactional
    public void deleteTest()  {
        ComTask comTask = createSimpleComTaskWithStatusInformation();
        comTask = getTaskService().findComTask(comTask.getId());
        List<? extends ProtocolTask> protocolTasks = comTask.getProtocolTasks();
        comTask.delete();
        assertThat(getTaskService().findComTask(comTask.getId())).isNull();
        for (ProtocolTask protocolTask : protocolTasks) {
            assertThat(getTaskService().findProtocolTask(protocolTask.getId()));
        }
//        assertThat(event); TODO assert this
    }


    @Test
    @Transactional
    public void createAllProtocolTaskOnComTask() {
        ComTask comTask = createSimpleComTaskWithStatusInformation();

        ComTask loadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(loadedComTask.getProtocolTasks()).hasSize(1);

        comTask.createClockTask(ClockTaskType.SETCLOCK).minimumClockDifference(TimeDuration.hours(1)).maximumClockDifference(TimeDuration.days(1)).add();
        loadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(loadedComTask.getProtocolTasks()).hasSize(2);

        comTask.createLoadProfilesTask().add();
        loadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(loadedComTask.getProtocolTasks()).hasSize(3);

        comTask.createLogbooksTask().add();
        loadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(loadedComTask.getProtocolTasks()).hasSize(4);

        comTask.createMessagesTask().allCategories().add();
        loadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(loadedComTask.getProtocolTasks()).hasSize(5);

        comTask.createBasicCheckTask().add();
        loadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(loadedComTask.getProtocolTasks()).hasSize(6);

        comTask.createRegistersTask().add();
        loadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(loadedComTask.getProtocolTasks()).hasSize(7);

        comTask.createTopologyTask(TopologyAction.UPDATE);
        loadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(loadedComTask.getProtocolTasks()).hasSize(8);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.COMTASK_WITHOUT_PROTOCOLTASK+"}", property = "protocolTasks")
    public void createWithNoProtocolTasksTest() {
        ComTask comTask = getTaskService().newComTask(COM_TASK_NAME);
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        comTask.save();
    }

    @Test
    @Transactional
    @Expected(value = TranslatableApplicationException.class,message = "At least one protocol task is required for a communication task.")
    public void testRemoveLastProtocolTask() throws Exception {
        ComTask simpleComTask = createSimpleComTaskWithStatusInformation();
        StatusInformationTask statusInformationTask = getTaskByType(simpleComTask.getProtocolTasks(), StatusInformationTask.class);
        simpleComTask.removeTask(statusInformationTask);// can't remove the last task
    }

    @Test
    @Transactional
    public void testNameIsTrimmed() throws Exception {
        ComTask comTask = getTaskService().newComTask(" "+COM_TASK_NAME+" ");
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        comTask.createStatusInformationTask();
        comTask.save();

        ComTask loadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(loadedComTask.getName()).isEqualTo(COM_TASK_NAME);
    }
}
