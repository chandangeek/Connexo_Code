package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategoryPrimaryKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.PersistenceTest;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.TopologyTask;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Condition;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link ComTaskImpl} component.
 *
 * @author gna
 * @since 7/05/12 - 11:46
 */
public class ProtocolTaskImplTest extends PersistenceTest {

    private static final boolean STORE_DATA_TRUE = true;
    private static final String COM_TASK_NAME = "UniqueComTaskName";

    private static final TimeDuration minimumClockDifference = new TimeDuration(3, TimeDuration.SECONDS);
    private static final TimeDuration maximumClockDifference = new TimeDuration(5, TimeDuration.MINUTES);
    private static final TimeDuration maximumClockShift = new TimeDuration(13, TimeDuration.SECONDS);

    private ComTask createSimpleComTask() {
        ComTask comTask = getTaskService().newComTask(COM_TASK_NAME);
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        return comTask;
    }

    @Test
    @Transactional
    public void testCreateStatusInformationTask() throws Exception {
        ComTask simpleComTask = createSimpleComTask();
        simpleComTask.createStatusInformationTask();
        simpleComTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(simpleComTask.getId());
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        StatusInformationTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), StatusInformationTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(simpleComTask.getId());
    }

    @Test
    @Transactional
    public void testCreateSETClockTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SETCLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(maximumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        ClockTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), ClockTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getClockTaskType()).isEqualTo(ClockTaskType.SETCLOCK);
        assertThat(taskByType.getMaximumClockDifference()).isEqualTo(maximumClockDifference);
        assertThat(taskByType.getMaximumClockShift()).isEqualTo(maximumClockShift);
        assertThat(taskByType.getMinimumClockDifference()).isEqualTo(minimumClockDifference);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + Constants.TIMEDURATION_MUST_BE_POSITIVE + "}", property = "maximumClockDiff", strict = false)
    public void testCreateSETClockTaskMaximumIsZero() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SETCLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(TimeDuration.millis(0)).maximumClockShift(maximumClockShift).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + Constants.CAN_NOT_BE_EMPTY + "}", property = "minimumClockDiff")
    public void testCreateSETClockTaskWithoutMinimumDuration() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SETCLOCK).maximumClockDifference(maximumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + Constants.CAN_NOT_BE_EMPTY + "}", property = "maximumClockDiff")
    public void testCreateSETClockTaskWithoutMaximumDuration() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SETCLOCK).minimumClockDifference(minimumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + Constants.MIN_MUST_BE_BELOW_MAX + "}", strict = false)
    public void testCreateSETClockTaskWithMinClockLargerThenMaxClock() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SETCLOCK).
                minimumClockDifference(maximumClockDifference).
                maximumClockDifference(minimumClockDifference).
                maximumClockShift(maximumClockShift).add();
        comTask.save();
    }

    @Test
    @Transactional
    public void testCreateSYNCClockTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SYNCHRONIZECLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(maximumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        ClockTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), ClockTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getClockTaskType()).isEqualTo(ClockTaskType.SYNCHRONIZECLOCK);
        assertThat(taskByType.getMaximumClockDifference()).isEqualTo(maximumClockDifference);
        assertThat(taskByType.getMaximumClockShift()).isEqualTo(maximumClockShift);
        assertThat(taskByType.getMinimumClockDifference()).isEqualTo(minimumClockDifference);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + Constants.CAN_NOT_BE_EMPTY + "}", property = "maximumClockShift", strict = false)
    public void testCreateSYNCClockTaskMaximumShiftIsZero() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SYNCHRONIZECLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(maximumClockDifference).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + Constants.TIMEDURATION_MUST_BE_POSITIVE + "}", property = "maximumClockShift")
    public void testCreateSYNCClockTaskWithoutMaximumDuration() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SYNCHRONIZECLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(maximumClockDifference).maximumClockShift(TimeDuration.millis(0)).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + Constants.CAN_NOT_BE_EMPTY + "}", property = "minimumClockDiff")
    public void testCreateSYNCClockTaskWithoutMinimumDuration() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SYNCHRONIZECLOCK).maximumClockDifference(maximumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save();
    }

    @Test
    @Transactional
    public void testCreateFORCECClockTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.FORCECLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(maximumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        ClockTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), ClockTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getClockTaskType()).isEqualTo(ClockTaskType.FORCECLOCK);
        assertThat(taskByType.getMaximumClockDifference()).isEqualTo(maximumClockDifference);
        assertThat(taskByType.getMaximumClockShift()).isEqualTo(maximumClockShift);
        assertThat(taskByType.getMinimumClockDifference()).isEqualTo(minimumClockDifference);
    }

    @Test
    @Transactional
    public void testUpdateFORCECClockTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.FORCECLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(maximumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        ClockTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), ClockTask.class);
        taskByType.setMaximumClockDifference(TimeDuration.days(1));
        taskByType.setMinimumClockDifference(TimeDuration.hours(1));
        taskByType.setMaximumClockShift(TimeDuration.minutes(1));
        taskByType.save();

        ComTask rereloadedComTask = getTaskService().findComTask(comTask.getId());
        ClockTask reloadedTaskByType = getTaskByType(rereloadedComTask.getProtocolTasks(), ClockTask.class);
        assertThat(rereloadedComTask.getProtocolTasks()).hasSize(1);
        assertThat(reloadedTaskByType).isNotNull();
        assertThat(reloadedTaskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(reloadedTaskByType.getClockTaskType()).isEqualTo(ClockTaskType.FORCECLOCK);
        assertThat(reloadedTaskByType.getMaximumClockDifference()).isEqualTo(TimeDuration.days(1));
        assertThat(reloadedTaskByType.getMaximumClockShift()).isEqualTo(TimeDuration.minutes(1));
        assertThat(reloadedTaskByType.getMinimumClockDifference()).isEqualTo(TimeDuration.hours(1));
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + Constants.MIN_MUST_BE_BELOW_MAX + "}", strict = false)
    public void testUpdateFORCECClockTaskToInvalidState() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.FORCECLOCK).minimumClockDifference(maximumClockDifference).maximumClockDifference(minimumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save(); // MIN > MAX !

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        ClockTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), ClockTask.class);
        taskByType.setClockTaskType(ClockTaskType.SETCLOCK);
        taskByType.save();
    }

    @Test
    @Transactional
    public void testCreateFORCECClockTaskWithoutDurations() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.FORCECLOCK).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        ClockTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), ClockTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getClockTaskType()).isEqualTo(ClockTaskType.FORCECLOCK);
        assertThat(taskByType.getMaximumClockDifference().getCount()).isEqualTo(0);
        assertThat(taskByType.getMaximumClockShift().getCount()).isEqualTo(0);
        assertThat(taskByType.getMinimumClockDifference().getCount()).isEqualTo(0);
    }

    @Test
    @Transactional
    public void testCreateTopologyTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createTopologyTask(TopologyAction.VERIFY);
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        TopologyTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), TopologyTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getTopologyAction()).isEqualTo(TopologyAction.VERIFY);
    }

    @Test
    @Transactional
    public void testUpdateTopologyTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createTopologyTask(TopologyAction.VERIFY);
        comTask.save();

        // update
        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        TopologyTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), TopologyTask.class);
        taskByType.setTopologyAction(TopologyAction.UPDATE);
        taskByType.save();

        // verify
        reloadedComTask = getTaskService().findComTask(comTask.getId());
        taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), TopologyTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getTopologyAction()).isEqualTo(TopologyAction.UPDATE);
    }

    @Test
    @Transactional
    public void testCreateLoadProfilesTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createLoadProfilesTask().
                minClockDiffBeforeBadTime(TimeDuration.days(1)).
                markIntervalsAsBadTime(true).
                failIfConfigurationMisMatch(true).
                createMeterEventsFromFlags(true).add();

        comTask.save();
        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        LoadProfilesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), LoadProfilesTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.createMeterEventsFromStatusFlags()).isTrue();
        assertThat(taskByType.failIfLoadProfileConfigurationMisMatch()).isTrue();
        assertThat(taskByType.isMarkIntervalsAsBadTime()).isTrue();
        assertThat(taskByType.getMinClockDiffBeforeBadTime()).isEqualTo(TimeDuration.days(1));
    }

    @Test
    @Transactional
    public void testUpdateLoadProfilesTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createLoadProfilesTask().
                minClockDiffBeforeBadTime(TimeDuration.days(1)).
                markIntervalsAsBadTime(true).
                failIfConfigurationMisMatch(true).
                createMeterEventsFromFlags(true).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        LoadProfilesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), LoadProfilesTask.class);
        taskByType.setCreateMeterEventsFromStatusFlags(false);
        taskByType.setFailIfConfigurationMisMatch(false);
        taskByType.setMarkIntervalsAsBadTime(false);
        taskByType.setMinClockDiffBeforeBadTime(TimeDuration.hours(1));
        taskByType.save();

        ComTask rereloadedComTask = getTaskService().findComTask(comTask.getId());
        LoadProfilesTask reloadedTaskByType = getTaskByType(rereloadedComTask.getProtocolTasks(), LoadProfilesTask.class);

        assertThat(reloadedTaskByType).isNotNull();
        assertThat(reloadedTaskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(reloadedTaskByType.createMeterEventsFromStatusFlags()).isFalse();
        assertThat(reloadedTaskByType.failIfLoadProfileConfigurationMisMatch()).isFalse();
        assertThat(reloadedTaskByType.isMarkIntervalsAsBadTime()).isFalse();
        assertThat(reloadedTaskByType.getMinClockDiffBeforeBadTime()).isEqualTo(TimeDuration.hours(1));
    }

    @Test
    @Transactional
    public void testCreateLogBooksTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createLogbooksTask().add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        LogBooksTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), LogBooksTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
    }

    @Test
    @Transactional
    public void testDeleteLogBooksTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createLogbooksTask().add();
        comTask.createStatusInformationTask();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        LogBooksTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), LogBooksTask.class);
        reloadedComTask.removeTask(taskByType);
        reloadedComTask.save();

        ComTask rereloadedComTask = getTaskService().findComTask(comTask.getId());
        LogBooksTask reloadedTaskByType = getTaskByType(rereloadedComTask.getProtocolTasks(), LogBooksTask.class);
        assertThat(reloadedTaskByType).isNull();
    }

    @Test
    @Transactional
    public void testCreateMessagesTaskAllCategories() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createMessagesTask().allCategories().add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        MessagesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.isAllCategories()).isTrue();
    }

    @Test
    @Transactional
    public void testCreateMessagesTaskDeviceMessageSpecs() throws Exception {
        ComTask comTask = createSimpleComTask();
        DeviceMessageSpec deviceMessageSpec = mockDeviceMessageSpec(1, "name1");
        comTask.createMessagesTask().deviceMessageSpecs(Arrays.asList(deviceMessageSpec)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        MessagesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.isAllCategories()).isFalse();
        assertThat(taskByType.getDeviceMessageSpecs()).hasSize(1);
        assertThat(taskByType.getDeviceMessageSpecs().get(0).getName()).isEqualTo("name1");
    }

    @Test
    @Transactional
    public void testCreateMessagesTaskDeviceMessageCategory() throws Exception {
        ComTask comTask = createSimpleComTask();
        DeviceMessageCategory deviceMessageCategory = mockDeviceMessageCategory(1, "name1");
        comTask.createMessagesTask().deviceMessageCategories(Arrays.asList(deviceMessageCategory)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        MessagesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.isAllCategories()).isFalse();
        assertThat(taskByType.getDeviceMessageCategories()).hasSize(1);
        assertThat(taskByType.getDeviceMessageCategories().get(0).getName()).isEqualTo("name1");
    }

    @Test
    @Transactional
    public void testCreateMessagesAdd1TaskDeviceMessageCategory() throws Exception {
        ComTask comTask = createSimpleComTask();
        DeviceMessageCategory deviceMessageCategory = mockDeviceMessageCategory(1, "name1");
        comTask.createMessagesTask().deviceMessageCategories(Arrays.asList(deviceMessageCategory)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        MessagesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);
        DeviceMessageCategory deviceMessageCategory2 = mockDeviceMessageCategory(2, "name2");
        taskByType.setDeviceMessageCategories(Arrays.asList(deviceMessageCategory, deviceMessageCategory2));
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId());
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), MessagesTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.isAllCategories()).isFalse();
        assertThat(taskByType.getDeviceMessageCategories()).hasSize(2);
    }

    @Test
    @Transactional
    public void testCreateMessagesRemove1TaskDeviceMessageCategory() throws Exception {
        ComTask comTask = createSimpleComTask();
        DeviceMessageCategory deviceMessageCategory = mockDeviceMessageCategory(1, "name1");
        DeviceMessageCategory deviceMessageCategory2 = mockDeviceMessageCategory(2, "name2");
        comTask.createMessagesTask().deviceMessageCategories(Arrays.asList(deviceMessageCategory, deviceMessageCategory2)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        MessagesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);
        taskByType.setDeviceMessageCategories(Arrays.asList(deviceMessageCategory2)); // we dropped spec1
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId());
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), MessagesTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.isAllCategories()).isFalse();
        assertThat(taskByType.getDeviceMessageCategories()).hasSize(1);
        assertThat(taskByType.getDeviceMessageCategories().get(0).getName()).isEqualTo("name2");
    }

    @Test
    @Transactional
    public void testCreateMessagesAdd1Remove1TaskDeviceMessageCategory() throws Exception {
        ComTask comTask = createSimpleComTask();
        DeviceMessageCategory deviceMessageCategory = mockDeviceMessageCategory(1, "name1");
        DeviceMessageCategory deviceMessageCategory2 = mockDeviceMessageCategory(2, "name2");
        comTask.createMessagesTask().deviceMessageCategories(Arrays.asList(deviceMessageCategory, deviceMessageCategory2)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        MessagesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);
        DeviceMessageCategory deviceMessageCategory3 = mockDeviceMessageCategory(3, "name3");
        taskByType.setDeviceMessageCategories(Arrays.asList(deviceMessageCategory2, deviceMessageCategory3)); // we dropped spec1 but added spec3
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId());
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), MessagesTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.isAllCategories()).isFalse();
        assertThat(taskByType.getDeviceMessageCategories()).hasSize(2);
        assertThat(taskByType.getDeviceMessageCategories().get(0).getName()).isNotEqualTo("name1");
        assertThat(taskByType.getDeviceMessageCategories().get(1).getName()).isNotEqualTo("name1");
    }

    @Test
    @Transactional
    public void testUpdateAdd1MessageSpec() throws Exception {
        ComTask comTask = createSimpleComTask();
        DeviceMessageSpec deviceMessageSpec = mockDeviceMessageSpec(1, "name1");
        comTask.createMessagesTask().deviceMessageSpecs(Arrays.asList(deviceMessageSpec)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        MessagesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);
        DeviceMessageSpec deviceMessageSpec2 = mockDeviceMessageSpec(2, "name2");
        taskByType.setDeviceMessageSpecs(Arrays.asList(deviceMessageSpec, deviceMessageSpec2));
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId());
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), MessagesTask.class);
        assertThat(taskByType.getDeviceMessageSpecs()).hasSize(2);
        assertThat(taskByType.getDeviceMessageCategories()).isEmpty();
    }

    @Test
    @Transactional
    public void testUpdateRemove1MessageSpec() throws Exception {
        ComTask comTask = createSimpleComTask();
        DeviceMessageSpec deviceMessageSpec = mockDeviceMessageSpec(1, "name1");
        DeviceMessageSpec deviceMessageSpec2 = mockDeviceMessageSpec(2, "name2");
        comTask.createMessagesTask().deviceMessageSpecs(Arrays.asList(deviceMessageSpec, deviceMessageSpec2)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        MessagesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);
        taskByType.setDeviceMessageSpecs(Arrays.asList(deviceMessageSpec2)); // we dropped spec1
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId());
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), MessagesTask.class);
        assertThat(taskByType.getDeviceMessageSpecs()).hasSize(1);
        assertThat(taskByType.getDeviceMessageSpecs().get(0).getName()).isEqualTo("name2");
        assertThat(taskByType.getDeviceMessageCategories()).isEmpty();
    }

    @Test
    @Transactional
    public void testUpdateRemove1Add1MessageSpec() throws Exception {
        ComTask comTask = createSimpleComTask();
        DeviceMessageSpec deviceMessageSpec = mockDeviceMessageSpec(1, "name1");
        DeviceMessageSpec deviceMessageSpec2 = mockDeviceMessageSpec(2, "name2");
        comTask.createMessagesTask().deviceMessageSpecs(Arrays.asList(deviceMessageSpec, deviceMessageSpec2)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        DeviceMessageSpec deviceMessageSpec3 = mockDeviceMessageSpec(3, "name3");
        MessagesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);
        taskByType.setDeviceMessageSpecs(Arrays.asList(deviceMessageSpec2, deviceMessageSpec3)); // we dropped spec1 and added spec3
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId());
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), MessagesTask.class);
        assertThat(taskByType.getDeviceMessageSpecs()).hasSize(2);
        assertThat(taskByType.getDeviceMessageSpecs().get(0).getName()).isNotEqualTo("name1");
        assertThat(taskByType.getDeviceMessageSpecs().get(1).getName()).isNotEqualTo("name1");
        assertThat(taskByType.getDeviceMessageCategories()).isEmpty();
    }

    private DeviceMessageSpec mockDeviceMessageSpec(int id, String name) {
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        when(deviceMessageCategory.getId()).thenReturn(id);
        DeviceMessageSpec deviceMessageSpec = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec.getName()).thenReturn(name);
        when(deviceMessageSpec.getCategory()).thenReturn(deviceMessageCategory);
        DeviceMessageSpecPrimaryKey name1PK = new DeviceMessageSpecPrimaryKey(deviceMessageSpec, name);
        when(deviceMessageSpec.getPrimaryKey()).thenReturn(name1PK);
        when(getDeviceMessageService().findDeviceMessageSpec(name1PK.getValue())).thenReturn(deviceMessageSpec);
        return deviceMessageSpec;
    }

    private DeviceMessageCategory mockDeviceMessageCategory(int id, String name) {
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        when(deviceMessageCategory.getId()).thenReturn(id);
        when(deviceMessageCategory.getName()).thenReturn(name);
        DeviceMessageCategoryPrimaryKey name1PK = new DeviceMessageCategoryPrimaryKey(deviceMessageCategory, name);
        when(deviceMessageCategory.getPrimaryKey()).thenReturn(name1PK);
        when(getDeviceMessageService().findDeviceMessageCategory(name1PK.getValue())).thenReturn(deviceMessageCategory);
        return deviceMessageCategory;
    }

    @Test
    @Transactional
    public void testCreateRegistersTaskWithoutRegisters() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createRegistersTask().add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        RegistersTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), RegistersTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getRegisterGroups()).isEmpty();
    }

    @Test
    @Transactional
    public void testCreateRegistersTaskWithRegisterGroup() throws Exception {
        ComTask comTask = createSimpleComTask();

        RegisterGroup registerGroup = getMasterDataService().newRegisterGroup("group1");
        registerGroup.save();
        comTask.createRegistersTask().registerGroups(Arrays.asList(registerGroup)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        RegistersTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), RegistersTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getRegisterGroups()).hasSize(1);
    }

    @Test
    @Transactional
    public void testCreateRegistersTaskAdd1RegisterGroup() throws Exception {
        ComTask comTask = createSimpleComTask();

        RegisterGroup registerGroup = getMasterDataService().newRegisterGroup("group1");
        registerGroup.save();
        comTask.createRegistersTask().registerGroups(Arrays.asList(registerGroup)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        RegistersTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), RegistersTask.class);
        RegisterGroup registerGroup2 = getMasterDataService().newRegisterGroup("group2");
        registerGroup2.save();
        taskByType.setRegisterGroups(Arrays.asList(registerGroup, registerGroup2));
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId());
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), RegistersTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getRegisterGroups()).hasSize(2);

    }

    @Test
    @Transactional
    public void testCreateRegistersTaskRemove1RegisterGroup() throws Exception {
        ComTask comTask = createSimpleComTask();

        RegisterGroup registerGroup = getMasterDataService().newRegisterGroup("group1");
        registerGroup.save();
        RegisterGroup registerGroup2 = getMasterDataService().newRegisterGroup("group2");
        registerGroup2.save();
        comTask.createRegistersTask().registerGroups(Arrays.asList(registerGroup, registerGroup2)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        RegistersTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), RegistersTask.class);
        taskByType.setRegisterGroups(Arrays.asList(registerGroup2));
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId());
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), RegistersTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getRegisterGroups()).hasSize(1);
        assertThat(taskByType.getRegisterGroups().get(0).getName()).isEqualTo("group2");
    }

    @Test
    @Transactional
    public void testCreateRegistersTaskAdd1Remove1RegisterGroup() throws Exception {
        ComTask comTask = createSimpleComTask();

        RegisterGroup registerGroup = getMasterDataService().newRegisterGroup("group1");
        registerGroup.save();
        RegisterGroup registerGroup2 = getMasterDataService().newRegisterGroup("group2");
        registerGroup2.save();
        comTask.createRegistersTask().registerGroups(Arrays.asList(registerGroup, registerGroup2)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        RegistersTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), RegistersTask.class);
        RegisterGroup registerGroup3 = getMasterDataService().newRegisterGroup("group3");
        registerGroup3.save();
        taskByType.setRegisterGroups(Arrays.asList(registerGroup2, registerGroup3));
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId());
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), RegistersTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getRegisterGroups()).hasSize(2);
        assertThat(taskByType.getRegisterGroups().get(0).getName()).isNotEqualTo("group1");
        assertThat(taskByType.getRegisterGroups().get(1).getName()).isNotEqualTo("group1");
    }

    @Test
    @Transactional
    public void testCreateBasicTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createBasicCheckTask().verifyClockDifference(true).verifySerialNumber(true).maximumClockDifference(TimeDuration.days(1)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        BasicCheckTask actual = getTaskByType(reloadedComTask.getProtocolTasks(), BasicCheckTask.class);
        assertThat(actual).isNotNull();
        assertThat(actual.getMaximumClockDifference()).isEqualTo(TimeDuration.days(1));
        assertThat(actual.verifyClockDifference()).isTrue();
        assertThat(actual.verifySerialNumber()).isTrue();
    }

    @Test
    @Transactional
    public void testUpdateBasicTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createBasicCheckTask().verifyClockDifference(true).verifySerialNumber(true).maximumClockDifference(TimeDuration.days(1)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        BasicCheckTask actual = getTaskByType(reloadedComTask.getProtocolTasks(), BasicCheckTask.class);
        actual.setVerifyClockDifference(false);
        actual.setVerifySerialNumber(false);
        actual.setMaximumClockDifference(TimeDuration.hours(1));
        actual.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId());
        actual = getTaskByType(reReloadedComTask.getProtocolTasks(), BasicCheckTask.class);
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        assertThat(actual).isNotNull();
        assertThat(actual.getMaximumClockDifference()).isEqualTo(TimeDuration.hours(1));
        assertThat(actual.verifyClockDifference()).isFalse();
        assertThat(actual.verifySerialNumber()).isFalse();
    }

    @Test
    @Transactional
    public void testDeleteBasicTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createBasicCheckTask().verifyClockDifference(true).verifySerialNumber(true).maximumClockDifference(TimeDuration.days(1)).add();
        comTask.createStatusInformationTask();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        BasicCheckTask actual = getTaskByType(reloadedComTask.getProtocolTasks(), BasicCheckTask.class);
        reloadedComTask.removeTask(actual);
        reloadedComTask.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId());
        actual = getTaskByType(reReloadedComTask.getProtocolTasks(), BasicCheckTask.class);
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        assertThat(actual).isNull();
    }

    @Test
    @Transactional
    public void createLoadProfilesTaskWithLoadProfileTypeTest() {
        ComTask comTask = createSimpleComTask();

        final LoadProfileType myLPT = getMasterDataService().newLoadProfileType("MyLPT", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.days(1));
        myLPT.save();
        comTask.createLoadProfilesTask().loadProfileTypes(Arrays.asList(myLPT)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        LoadProfilesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), LoadProfilesTask.class);
        final LoadProfileType myLPT2 = getMasterDataService().newLoadProfileType("MyLPT2", ObisCode.fromString("1.0.99.2.0.255"), TimeDuration.days(1));
        myLPT2.save();
        taskByType.setLoadProfileTypes(Arrays.asList(myLPT, myLPT2));
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId());
        LoadProfilesTask loadProfileTask = getTaskByType(reReloadedComTask.getProtocolTasks(), LoadProfilesTask.class);
        assertThat(loadProfileTask).isNotNull();
        assertThat(loadProfileTask.getLoadProfileTypes()).hasSize(2);
        assertThat(loadProfileTask.getLoadProfileTypes()).haveExactly(1, new Condition<LoadProfileType>() {
            @Override
            public boolean matches(LoadProfileType loadProfileType) {
                return loadProfileType.getId() == myLPT.getId();
            }
        });
        assertThat(loadProfileTask.getLoadProfileTypes()).haveExactly(1, new Condition<LoadProfileType>() {
            @Override
            public boolean matches(LoadProfileType loadProfileType) {
                return loadProfileType.getId() == myLPT2.getId();
            }
        });
    }

    @Test
    @Transactional
    public void deleteComTaskWithLoadProfileTypeShouldClearTheLinkTableTest() {
        ComTask comTask = createSimpleComTask();

        final LoadProfileType myLPT = getMasterDataService().newLoadProfileType("MyLPT", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.days(1));
        myLPT.save();
        comTask.createLoadProfilesTask().loadProfileTypes(Arrays.asList(myLPT)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        reloadedComTask.delete();

        List<LoadProfileTypeUsageInProtocolTask> loadProfileTypeUsageInProtocolTasks = getDataModel().mapper(LoadProfileTypeUsageInProtocolTask.class).find();
        assertThat(loadProfileTypeUsageInProtocolTasks).isEmpty();
    }

    @Test
    @Transactional
    public void createLogBooksTaskWithLogBookTypesTest() {
        ComTask comTask = createSimpleComTask();
        final LogBookType lbType = getMasterDataService().newLogBookType("LBType", ObisCode.fromString("0.0.99.98.0.255"));
        lbType.save();
        comTask.createLogbooksTask().logBookTypes(Arrays.asList(lbType)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
        LogBooksTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), LogBooksTask.class);
        final LogBookType lbType2 = getMasterDataService().newLogBookType("LBType2", ObisCode.fromString("0.0.99.97.0.255"));
        lbType2.save();
        taskByType.setLogBookTypes(Arrays.asList(lbType, lbType2));
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId());
        LogBooksTask logBooksTask = getTaskByType(reloadedComTask.getProtocolTasks(), LogBooksTask.class);

        assertThat(logBooksTask).isNotNull();
        assertThat(logBooksTask.getLogBookTypes()).hasSize(2);
        assertThat(logBooksTask.getLogBookTypes()).haveExactly(1, new Condition<LogBookType>() {
            @Override
            public boolean matches(LogBookType logBookType) {
                return logBookType.getId() == lbType.getId();
            }
        });
        assertThat(logBooksTask.getLogBookTypes()).haveExactly(1, new Condition<LogBookType>() {
            @Override
            public boolean matches(LogBookType logBookType) {
                return logBookType.getId() == lbType2.getId();
            }
        });
    }

    @Test
    @Transactional
    public void deleteComTaskWithLogBookTypeShouldClearTheLinkTableTest() {
        ComTask comTask = createSimpleComTask();
        final LogBookType lbType = getMasterDataService().newLogBookType("LBType", ObisCode.fromString("0.0.99.98.0.255"));
        lbType.save();
        comTask.createLogbooksTask().logBookTypes(Arrays.asList(lbType)).add();
        comTask.save();

        comTask.delete();

        List<LogBookTypeUsageInProtocolTask> logBookTypeUsageInProtocolTasks = getDataModel().mapper(LogBookTypeUsageInProtocolTask.class).find();
        assertThat(logBookTypeUsageInProtocolTasks).isEmpty();
    }
}