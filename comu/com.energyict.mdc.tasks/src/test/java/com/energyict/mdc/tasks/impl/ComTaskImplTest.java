package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.PersistenceTest;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.upl.tasks.TopologyAction;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link ComTaskImpl} component.
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
        return getTaskService().findComTask(comTask.getId()).get();
    }

    private ComTask createComTaskWithBasicCheckAndStatusInformation() {
        ComTask comTask = getTaskService().newComTask(COM_TASK_NAME);
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        comTask.createStatusInformationTask();
        comTask.createBasicCheckTask().add();
        comTask.save();
        return getTaskService().findComTask(comTask.getId()).get(); // to make sure all elements in the composition are properly loaded
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
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}", property = "name")
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
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.DUPLICATE_COMTASK_NAME+"}", property = "name")
    public void createWithDuplicateNameTest()  {
        createSimpleComTaskWithStatusInformation();
        createSimpleComTaskWithStatusInformation();
        // we expect to fail with a duplicate exception
    }

    @Test
    @Transactional
    public void deleteTest()  {
        ComTask comTask = createSimpleComTaskWithStatusInformation();
        comTask = getTaskService().findComTask(comTask.getId()).get();
        List<? extends ProtocolTask> protocolTasks = comTask.getProtocolTasks();
        comTask.delete();
        assertThat(getTaskService().findComTask(comTask.getId()).isPresent()).isFalse();
        for (ProtocolTask protocolTask : protocolTasks) {
            assertThat(getTaskService().findProtocolTask(protocolTask.getId()));
        }
//        assertThat(event); TODO assert this
    }


    @Test
    @Transactional
    public void createAllProtocolTaskOnComTask() {
        ComTask comTask = createSimpleComTaskWithStatusInformation();

        ComTask loadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(loadedComTask.getProtocolTasks()).hasSize(1);

        comTask.createClockTask(ClockTaskType.SETCLOCK).minimumClockDifference(TimeDuration.hours(1)).maximumClockDifference(TimeDuration.days(1)).add();
        loadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(loadedComTask.getProtocolTasks()).hasSize(2);

        comTask.createLoadProfilesTask().add();
        loadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(loadedComTask.getProtocolTasks()).hasSize(3);

        comTask.createLogbooksTask().add();
        loadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(loadedComTask.getProtocolTasks()).hasSize(4);

        comTask.createMessagesTask().deviceMessageCategories(this.getDeviceMessageService().filteredCategoriesForUserSelection()).add();
        loadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(loadedComTask.getProtocolTasks()).hasSize(5);

        comTask.createBasicCheckTask().add();
        loadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(loadedComTask.getProtocolTasks()).hasSize(6);

        comTask.createRegistersTask().add();
        loadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(loadedComTask.getProtocolTasks()).hasSize(7);

        comTask.createTopologyTask(TopologyAction.UPDATE);
        loadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(loadedComTask.getProtocolTasks()).hasSize(8);
    }

    @Test
    @Transactional
    public void testNameIsTrimmed() throws Exception {
        ComTask comTask = getTaskService().newComTask(" "+COM_TASK_NAME+" ");
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        comTask.createStatusInformationTask();
        comTask.save();

        ComTask loadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(loadedComTask.getName()).isEqualTo(COM_TASK_NAME);
    }

    @Test
    @Transactional
    public void firmwareComTaskIsCreatedByDefaultTest() {
        assertThat(getTaskService().findFirmwareComTask().isPresent()).isTrue();
        assertThat(getTaskService().findFirmwareComTask().get().getName()).isEqualTo(ServerTaskService.FIRMWARE_COMTASK_NAME);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.ONLY_ONE_COMTASK_WITH_FIRMWARE_ALLOWED + "}")
    public void createMultipleComTasksForFirmwareTest() {
        // the system will create one by default, so creating a second one should fail

        SystemComTask myFirmwareComTask = getDataModel().getInstance(SystemComTask.class);
        myFirmwareComTask.setName("MyFirmwareComTask");
        myFirmwareComTask.createFirmwareUpgradeTask();
        myFirmwareComTask.save();
    }

    @Test
    @Transactional
    public void successfullyCreateFirmwareTaskAfterRemovingSystemDefaultTest() {
        ComTask comTask = getTaskService().findFirmwareComTask().get();
        comTask.delete();

        SystemComTask newFirmwareTask = getDataModel().getInstance(SystemComTask.class);
        newFirmwareTask.setName(ServerTaskService.FIRMWARE_COMTASK_NAME);
        newFirmwareTask.createFirmwareUpgradeTask();
        newFirmwareTask.save();

        Optional<ComTask> myNewFirmwareTask = getTaskService().findFirmwareComTask();
        assertThat(myNewFirmwareTask.isPresent()).isTrue();
        assertThat(myNewFirmwareTask.get().getName()).isEqualTo(ServerTaskService.FIRMWARE_COMTASK_NAME);
        assertThat(myNewFirmwareTask.get().getId()).isEqualTo(newFirmwareTask.getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.ONLY_ONE_PROTOCOLTASK_WHEN_FIRMWARE_UPGRADE + "}")
    public void createFirmwareUpgradeTaskWithMultipleProtocolTasksTest() {
        ComTask comTask = getTaskService().findFirmwareComTask().get();
        comTask.delete();

        SystemComTask newFirmwareTask = getDataModel().getInstance(SystemComTask.class);
        newFirmwareTask.setName(ServerTaskService.FIRMWARE_COMTASK_NAME);
        newFirmwareTask.createFirmwareUpgradeTask();
        newFirmwareTask.createTopologyTask(TopologyAction.UPDATE);
        newFirmwareTask.save();
    }
}
