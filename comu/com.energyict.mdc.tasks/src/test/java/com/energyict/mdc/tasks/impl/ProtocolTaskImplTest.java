package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
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
import org.assertj.core.api.Condition;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link ComTaskImpl} component.
 *
 * @author gna
 * @since 7/05/12 - 11:46
 */
public class ProtocolTaskImplTest extends PersistenceTest {

    private static final boolean STORE_DATA_TRUE = true;
    private static final String COM_TASK_NAME = "UniqueComTaskName";

    private static final TimeDuration minimumClockDifference = new TimeDuration(3, TimeDuration.TimeUnit.SECONDS);
    private static final TimeDuration maximumClockDifference = new TimeDuration(5, TimeDuration.TimeUnit.MINUTES);
    private static final TimeDuration maximumClockShift = new TimeDuration(13, TimeDuration.TimeUnit.SECONDS);
    private RegisterType registerType;

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

        ComTask reloadedComTask = getTaskService().findComTask(simpleComTask.getId()).get();
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

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        ClockTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), ClockTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getClockTaskType()).isEqualTo(ClockTaskType.SETCLOCK);
        assertThat(taskByType.getMaximumClockDifference().isPresent()).isTrue();
        assertThat(taskByType.getMaximumClockDifference().get()).isEqualTo(maximumClockDifference);
        assertThat(taskByType.getMaximumClockShift().isPresent()).isTrue();
        assertThat(taskByType.getMaximumClockShift().get()).isEqualTo(maximumClockShift);
        assertThat(taskByType.getMinimumClockDifference().isPresent()).isTrue();
        assertThat(taskByType.getMinimumClockDifference().get()).isEqualTo(minimumClockDifference);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TIMEDURATION_MUST_BE_POSITIVE + "}", property = "maximumClockDiff", strict = false)
    public void testCreateSETClockTaskMaximumIsZero() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SETCLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(TimeDuration.millis(0)).maximumClockShift(maximumClockShift).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "minimumClockDiff")
    public void testCreateSETClockTaskWithoutMinimumDuration() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SETCLOCK).maximumClockDifference(maximumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "maximumClockDiff")
    public void testCreateSETClockTaskWithoutMaximumDuration() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SETCLOCK).minimumClockDifference(minimumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MIN_MUST_BE_BELOW_MAX + "}", strict = false)
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

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        ClockTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), ClockTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getClockTaskType()).isEqualTo(ClockTaskType.SYNCHRONIZECLOCK);
        assertThat(taskByType.getMaximumClockDifference().isPresent()).isTrue();
        assertThat(taskByType.getMaximumClockDifference().get()).isEqualTo(maximumClockDifference);
        assertThat(taskByType.getMaximumClockShift().isPresent()).isTrue();
        assertThat(taskByType.getMaximumClockShift().get()).isEqualTo(maximumClockShift);
        assertThat(taskByType.getMinimumClockDifference().isPresent()).isTrue();
        assertThat(taskByType.getMinimumClockDifference().get()).isEqualTo(minimumClockDifference);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "maximumClockShift", strict = false)
    public void testCreateSYNCClockTaskMaximumShiftIsZero() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SYNCHRONIZECLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(maximumClockDifference).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TIMEDURATION_MUST_BE_POSITIVE + "}", property = "maximumClockShift")
    public void testCreateSYNCClockTaskWithoutMaximumDuration() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SYNCHRONIZECLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(maximumClockDifference).maximumClockShift(TimeDuration.millis(0)).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "minimumClockDiff")
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

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        ClockTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), ClockTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getClockTaskType()).isEqualTo(ClockTaskType.FORCECLOCK);
        assertThat(taskByType.getMaximumClockDifference().isPresent()).isTrue();
        assertThat(taskByType.getMaximumClockDifference().get()).isEqualTo(maximumClockDifference);
        assertThat(taskByType.getMaximumClockShift().isPresent()).isTrue();
        assertThat(taskByType.getMaximumClockShift().get()).isEqualTo(maximumClockShift);
        assertThat(taskByType.getMinimumClockDifference().isPresent()).isTrue();
        assertThat(taskByType.getMinimumClockDifference().get()).isEqualTo(minimumClockDifference);
    }

    @Test
    @Transactional
    public void testUpdateFORCECClockTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.FORCECLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(maximumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        ClockTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), ClockTask.class);
        taskByType.setMaximumClockDifference(TimeDuration.days(1));
        taskByType.setMinimumClockDifference(TimeDuration.hours(1));
        taskByType.setMaximumClockShift(TimeDuration.minutes(1));
        taskByType.save();

        ComTask rereloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        ClockTask reloadedTaskByType = getTaskByType(rereloadedComTask.getProtocolTasks(), ClockTask.class);
        assertThat(rereloadedComTask.getProtocolTasks()).hasSize(1);
        assertThat(reloadedTaskByType).isNotNull();
        assertThat(reloadedTaskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(reloadedTaskByType.getClockTaskType()).isEqualTo(ClockTaskType.FORCECLOCK);
        assertThat(reloadedTaskByType.getMaximumClockDifference().isPresent()).isTrue();
        assertThat(reloadedTaskByType.getMaximumClockDifference().get()).isEqualTo(TimeDuration.days(1));
        assertThat(reloadedTaskByType.getMaximumClockShift().isPresent()).isTrue();
        assertThat(reloadedTaskByType.getMaximumClockShift().get()).isEqualTo(TimeDuration.minutes(1));
        assertThat(reloadedTaskByType.getMinimumClockDifference().isPresent()).isTrue();
        assertThat(reloadedTaskByType.getMinimumClockDifference().get()).isEqualTo(TimeDuration.hours(1));
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MIN_MUST_BE_BELOW_MAX + "}", strict = false)
    public void testUpdateFORCECClockTaskToInvalidState() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.FORCECLOCK).minimumClockDifference(maximumClockDifference).maximumClockDifference(minimumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save(); // MIN > MAX !

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
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

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        ClockTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), ClockTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getClockTaskType()).isEqualTo(ClockTaskType.FORCECLOCK);
        assertThat(taskByType.getMaximumClockDifference().isPresent()).isFalse();
        assertThat(taskByType.getMaximumClockShift().isPresent()).isFalse();
        assertThat(taskByType.getMinimumClockDifference().isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testCreateTopologyTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createTopologyTask(TopologyAction.VERIFY);
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
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
        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        TopologyTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), TopologyTask.class);
        taskByType.setTopologyAction(TopologyAction.UPDATE);
        taskByType.save();

        // verify
        reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
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
        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        LoadProfilesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), LoadProfilesTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.createMeterEventsFromStatusFlags()).isTrue();
        assertThat(taskByType.failIfLoadProfileConfigurationMisMatch()).isTrue();
        assertThat(taskByType.isMarkIntervalsAsBadTime()).isTrue();
        assertThat(taskByType.getMinClockDiffBeforeBadTime().isPresent()).isTrue();
        assertThat(taskByType.getMinClockDiffBeforeBadTime().get()).isEqualTo(TimeDuration.days(1));
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

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        LoadProfilesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), LoadProfilesTask.class);
        taskByType.setCreateMeterEventsFromStatusFlags(false);
        taskByType.setFailIfConfigurationMisMatch(false);
        taskByType.setMarkIntervalsAsBadTime(false);
        taskByType.setMinClockDiffBeforeBadTime(TimeDuration.hours(1));
        taskByType.save();

        ComTask rereloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        LoadProfilesTask reloadedTaskByType = getTaskByType(rereloadedComTask.getProtocolTasks(), LoadProfilesTask.class);

        assertThat(reloadedTaskByType).isNotNull();
        assertThat(reloadedTaskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(reloadedTaskByType.createMeterEventsFromStatusFlags()).isFalse();
        assertThat(reloadedTaskByType.failIfLoadProfileConfigurationMisMatch()).isFalse();
        assertThat(reloadedTaskByType.isMarkIntervalsAsBadTime()).isFalse();
        assertThat(reloadedTaskByType.getMinClockDiffBeforeBadTime().isPresent()).isTrue();
        assertThat(reloadedTaskByType.getMinClockDiffBeforeBadTime().get()).isEqualTo(TimeDuration.hours(1));
    }

    @Test
    @Transactional
    public void testCreateLogBooksTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createLogbooksTask().add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
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

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        LogBooksTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), LogBooksTask.class);
        reloadedComTask.removeTask(taskByType);
        reloadedComTask.save();

        ComTask rereloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        LogBooksTask reloadedTaskByType = getTaskByType(rereloadedComTask.getProtocolTasks(), LogBooksTask.class);
        assertThat(reloadedTaskByType).isNull();
    }

    @Test
    @Transactional
    public void testCreateMessagesTaskDeviceMessageCategory() throws Exception {
        ComTask comTask = createSimpleComTask();
        DeviceMessageCategory deviceMessageCategory = this.getDeviceMessageCategory(1);
        String name1 = deviceMessageCategory.getName();
        comTask.createMessagesTask().deviceMessageCategories(Arrays.asList(deviceMessageCategory)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        MessagesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getDeviceMessageCategories()).hasSize(1);
        assertThat(taskByType.getDeviceMessageCategories().get(0).getName()).isEqualTo(name1);
    }

    @Test
    @Transactional
    public void testCreateMessagesAdd1TaskDeviceMessageCategory() throws Exception {
        ComTask comTask = createSimpleComTask();
        DeviceMessageCategory deviceMessageCategory = this.getDeviceMessageCategory(1);
        comTask.createMessagesTask().deviceMessageCategories(Arrays.asList(deviceMessageCategory)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        MessagesTaskImpl taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTaskImpl.class);
        DeviceMessageCategory deviceMessageCategory2 = this.getDeviceMessageCategory(2);
        taskByType.setDeviceMessageCategories(Arrays.asList(deviceMessageCategory, deviceMessageCategory2));
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), MessagesTaskImpl.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getDeviceMessageCategories()).hasSize(2);
    }

    @Test
    @Transactional
    public void testCreateMessagesRemove1TaskDeviceMessageCategory() throws Exception {
        ComTask comTask = createSimpleComTask();
        DeviceMessageCategory deviceMessageCategory = this.getDeviceMessageCategory(1);
        DeviceMessageCategory deviceMessageCategory2 = this.getDeviceMessageCategory(2);
        String name2 = deviceMessageCategory2.getName();
        comTask.createMessagesTask().deviceMessageCategories(Arrays.asList(deviceMessageCategory, deviceMessageCategory2)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        MessagesTaskImpl taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTaskImpl.class);
        taskByType.setDeviceMessageCategories(Arrays.asList(deviceMessageCategory2)); // we dropped spec1
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), MessagesTaskImpl.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getDeviceMessageCategories()).hasSize(1);
        assertThat(taskByType.getDeviceMessageCategories().get(0).getName()).isEqualTo(name2);
    }

    @Test
    @Transactional
    public void testCreateMessagesAdd1Remove1TaskDeviceMessageCategory() throws Exception {
        ComTask comTask = createSimpleComTask();
        DeviceMessageCategory deviceMessageCategory = this.getDeviceMessageCategory(1);
        String name1 = deviceMessageCategory.getName();
        DeviceMessageCategory deviceMessageCategory2 = this.getDeviceMessageCategory(2);
        comTask.createMessagesTask().deviceMessageCategories(Arrays.asList(deviceMessageCategory, deviceMessageCategory2)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        MessagesTaskImpl taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTaskImpl.class);
        DeviceMessageCategory deviceMessageCategory3 = this.getDeviceMessageCategory(3);
        taskByType.setDeviceMessageCategories(Arrays.asList(deviceMessageCategory2, deviceMessageCategory3)); // we dropped spec1 but added spec3
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), MessagesTaskImpl.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getComTask().getId()).isEqualTo(comTask.getId());
        assertThat(taskByType.getDeviceMessageCategories()).hasSize(2);
        assertThat(taskByType.getDeviceMessageCategories().get(0).getName()).isNotEqualTo(name1);
        assertThat(taskByType.getDeviceMessageCategories().get(1).getName()).isNotEqualTo(name1);
    }

    @Test
    @Transactional
    public void testCreateRegistersTaskWithoutRegisters() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createRegistersTask().add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        RegistersTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), RegistersTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getRegisterGroups()).isEmpty();
    }

    @Test
    @Transactional
    public void testCreateRegistersTaskWithRegisterGroup() throws Exception {
        this.setupReadingTypeInExistingTransaction();
        ComTask comTask = createSimpleComTask();

        RegisterGroup registerGroup = getMasterDataService().newRegisterGroup("group1");
        registerGroup.addRegisterType(this.registerType);
        registerGroup.save();
        comTask.createRegistersTask().registerGroups(Arrays.asList(registerGroup)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        RegistersTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), RegistersTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getRegisterGroups()).hasSize(1);
    }

    @Test
    @Transactional
    public void testCreateRegistersTaskAdd1RegisterGroup() throws Exception {
        this.setupReadingTypeInExistingTransaction();
        ComTask comTask = createSimpleComTask();

        RegisterGroup registerGroup = getMasterDataService().newRegisterGroup("group1");
        registerGroup.addRegisterType(this.registerType);
        registerGroup.save();
        comTask.createRegistersTask().registerGroups(Arrays.asList(registerGroup)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        RegistersTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), RegistersTask.class);
        RegisterGroup registerGroup2 = getMasterDataService().newRegisterGroup("group2");
        registerGroup2.addRegisterType(this.registerType);
        registerGroup2.save();
        taskByType.setRegisterGroups(Arrays.asList(registerGroup, registerGroup2));
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), RegistersTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getRegisterGroups()).hasSize(2);

    }

    @Test
    @Transactional
    public void testCreateRegistersTaskRemove1RegisterGroup() throws Exception {
        this.setupReadingTypeInExistingTransaction();
        ComTask comTask = createSimpleComTask();

        RegisterGroup registerGroup = getMasterDataService().newRegisterGroup("group1");
        registerGroup.addRegisterType(this.registerType);
        registerGroup.save();
        RegisterGroup registerGroup2 = getMasterDataService().newRegisterGroup("group2");
        registerGroup2.addRegisterType(this.registerType);
        registerGroup2.save();
        comTask.createRegistersTask().registerGroups(Arrays.asList(registerGroup, registerGroup2)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        RegistersTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), RegistersTask.class);
        taskByType.setRegisterGroups(Arrays.asList(registerGroup2));
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        taskByType = getTaskByType(reReloadedComTask.getProtocolTasks(), RegistersTask.class);
        assertThat(taskByType).isNotNull();
        assertThat(taskByType.getRegisterGroups()).hasSize(1);
        assertThat(taskByType.getRegisterGroups().get(0).getName()).isEqualTo("group2");
    }

    @Test
    @Transactional
    public void testCreateRegistersTaskAdd1Remove1RegisterGroup() throws Exception {
        this.setupReadingTypeInExistingTransaction();
        ComTask comTask = createSimpleComTask();

        RegisterGroup registerGroup = getMasterDataService().newRegisterGroup("group1");
        registerGroup.addRegisterType(this.registerType);
        registerGroup.save();
        RegisterGroup registerGroup2 = getMasterDataService().newRegisterGroup("group2");
        registerGroup2.addRegisterType(this.registerType);
        registerGroup2.save();
        comTask.createRegistersTask().registerGroups(Arrays.asList(registerGroup, registerGroup2)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        RegistersTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), RegistersTask.class);
        RegisterGroup registerGroup3 = getMasterDataService().newRegisterGroup("group3");
        registerGroup3.addRegisterType(this.registerType);
        registerGroup3.save();
        taskByType.setRegisterGroups(Arrays.asList(registerGroup2, registerGroup3));
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId()).get();
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

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        BasicCheckTask actual = getTaskByType(reloadedComTask.getProtocolTasks(), BasicCheckTask.class);
        assertThat(actual).isNotNull();
        assertThat(actual.getMaximumClockDifference().isPresent()).isTrue();
        assertThat(actual.getMaximumClockDifference().get()).isEqualTo(TimeDuration.days(1));
        assertThat(actual.verifyClockDifference()).isTrue();
        assertThat(actual.verifySerialNumber()).isTrue();
    }

    @Test
    @Transactional
    public void testUpdateBasicTask() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createBasicCheckTask().verifyClockDifference(true).verifySerialNumber(true).maximumClockDifference(TimeDuration.days(1)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        BasicCheckTask actual = getTaskByType(reloadedComTask.getProtocolTasks(), BasicCheckTask.class);
        actual.setVerifyClockDifference(false);
        actual.setVerifySerialNumber(false);
        actual.setMaximumClockDifference(TimeDuration.hours(1));
        actual.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        actual = getTaskByType(reReloadedComTask.getProtocolTasks(), BasicCheckTask.class);
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        assertThat(actual).isNotNull();
        assertThat(actual.getMaximumClockDifference().isPresent()).isTrue();
        assertThat(actual.getMaximumClockDifference().get()).isEqualTo(TimeDuration.hours(1));
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

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        BasicCheckTask actual = getTaskByType(reloadedComTask.getProtocolTasks(), BasicCheckTask.class);
        reloadedComTask.removeTask(actual);
        reloadedComTask.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        actual = getTaskByType(reReloadedComTask.getProtocolTasks(), BasicCheckTask.class);
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        assertThat(actual).isNull();
    }

    @Test
    @Transactional
    public void createLoadProfilesTaskWithLoadProfileTypeTest() {
        ComTask comTask = createSimpleComTask();

        setupReadingTypeInExistingTransaction();
        final LoadProfileType myLPT = getMasterDataService().newLoadProfileType("MyLPT", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.days(1), Arrays.asList(this.registerType));
        myLPT.save();
        comTask.createLoadProfilesTask().loadProfileTypes(Arrays.asList(myLPT)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        LoadProfilesTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), LoadProfilesTask.class);
        final LoadProfileType myLPT2 = getMasterDataService().newLoadProfileType("MyLPT2", ObisCode.fromString("1.0.99.2.0.255"), TimeDuration.days(1), Arrays.asList(this.registerType));
        myLPT2.save();
        taskByType.setLoadProfileTypes(Arrays.asList(myLPT, myLPT2));
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId()).get();
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
        setupReadingTypeInExistingTransaction();
        final LoadProfileType myLPT = getMasterDataService().newLoadProfileType("MyLPT", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.days(1), Arrays.asList(this.registerType));
        myLPT.save();
        comTask.createLoadProfilesTask().loadProfileTypes(Arrays.asList(myLPT)).add();
        comTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
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

        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId()).get();
        LogBooksTask taskByType = getTaskByType(reloadedComTask.getProtocolTasks(), LogBooksTask.class);
        final LogBookType lbType2 = getMasterDataService().newLogBookType("LBType2", ObisCode.fromString("0.0.99.97.0.255"));
        lbType2.save();
        taskByType.setLogBookTypes(Arrays.asList(lbType, lbType2));
        taskByType.save();

        ComTask reReloadedComTask = getTaskService().findComTask(comTask.getId()).get();
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

    private DeviceMessageCategory getDeviceMessageCategory(int categoryId) {
        return this.getDeviceMessageService().findCategoryById(categoryId).orElseThrow(() -> new RuntimeException("Setup failure: unable to find DeviceMessageCategory with id " + categoryId));
    }

    @Test
    @Transactional
    public void initialMessageTaskIsSetToNoneTest() {
        ComTask simpleComTask = createSimpleComTask();
        MessagesTask messagesTask = simpleComTask.createMessagesTask().add();

        assertThat(messagesTask.getMessageTaskType()).isEqualTo(MessagesTask.MessageTaskType.NONE);
    }

    @Test
    @Transactional
    public void setMessageTaskToAllMessagesTest() {
        ComTask simpleComTask = createSimpleComTask();
        MessagesTask messagesTask = simpleComTask.createMessagesTask().setMessageTaskType(MessagesTask.MessageTaskType.ALL).add();
        simpleComTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(simpleComTask.getId()).get();
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        MessagesTask reloadedMessageTask = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);

        assertThat(reloadedMessageTask.getMessageTaskType()).isEqualTo(MessagesTask.MessageTaskType.ALL);
    }

    @Test
    @Transactional
    public void setMessageTaskCategoriesSetTaskTypeToSelectedTest() {
        ComTask simpleComTask = createSimpleComTask();
        List<DeviceMessageCategory> categories = Arrays.asList(getDeviceMessageCategory(1), getDeviceMessageCategory(2));
        MessagesTask messagesTask = simpleComTask.createMessagesTask().deviceMessageCategories(categories).add();
        simpleComTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(simpleComTask.getId()).get();
        assertThat(reloadedComTask.getProtocolTasks()).hasSize(1);
        MessagesTask reloadedMessageTask = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);

        assertThat(reloadedMessageTask.getMessageTaskType()).isEqualTo(MessagesTask.MessageTaskType.SELECTED);
    }

    @Test
    @Transactional
    public void updateMessageTaskToNoneTest() {
        ComTask simpleComTask = createSimpleComTask();
        MessagesTask messagesTask = simpleComTask.createMessagesTask().setMessageTaskType(MessagesTask.MessageTaskType.ALL).add();
        simpleComTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(simpleComTask.getId()).get();
        MessagesTask reloadedMessageTask = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);

        reloadedMessageTask.setMessageTaskType(MessagesTask.MessageTaskType.NONE);
        reloadedMessageTask.save();

        reloadedComTask = getTaskService().findComTask(simpleComTask.getId()).get();
        reloadedMessageTask = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);

        assertThat(reloadedMessageTask.getMessageTaskType()).isEqualTo(MessagesTask.MessageTaskType.NONE);
    }


    @Test
    @Transactional
    public void updateMessagesTaskToNoneDeletesCategoriesTest() {
        ComTask simpleComTask = createSimpleComTask();
        List<DeviceMessageCategory> categories = Arrays.asList(getDeviceMessageCategory(1), getDeviceMessageCategory(2));
        MessagesTask messagesTask = simpleComTask.createMessagesTask().deviceMessageCategories(categories).add();
        simpleComTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(simpleComTask.getId()).get();
        MessagesTask reloadedMessageTask = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);

        reloadedMessageTask.setMessageTaskType(MessagesTask.MessageTaskType.NONE);
        reloadedMessageTask.save();

        reloadedComTask = getTaskService().findComTask(simpleComTask.getId()).get();
        reloadedMessageTask = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);

        assertThat(reloadedMessageTask.getMessageTaskType()).isEqualTo(MessagesTask.MessageTaskType.NONE);
        assertThat(reloadedMessageTask.getDeviceMessageCategories()).hasSize(0);
    }

    @Test
    @Transactional
    public void updateMessagesTaskWithCategoriesFromNoneTypeTest() {
        List<DeviceMessageCategory> categories = Arrays.asList(getDeviceMessageCategory(1), getDeviceMessageCategory(2));
        ComTask simpleComTask = createSimpleComTask();
        MessagesTask messagesTask = simpleComTask.createMessagesTask().setMessageTaskType(MessagesTask.MessageTaskType.NONE).add();
        simpleComTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(simpleComTask.getId()).get();
        MessagesTask reloadedMessageTask = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);

        reloadedMessageTask.setDeviceMessageCategories(categories);
        reloadedMessageTask.save();

        reloadedComTask = getTaskService().findComTask(simpleComTask.getId()).get();
        reloadedMessageTask = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);

        assertThat(reloadedMessageTask.getMessageTaskType()).isEqualTo(MessagesTask.MessageTaskType.SELECTED);
        assertThat(reloadedMessageTask.getDeviceMessageCategories()).hasSize(2);
    }

    @Test
    @Transactional
    public void updateMessagesTaskWithCategoriesFromAllTypeTest() {
        List<DeviceMessageCategory> categories = Arrays.asList(getDeviceMessageCategory(1), getDeviceMessageCategory(2));
        ComTask simpleComTask = createSimpleComTask();
        MessagesTask messagesTask = simpleComTask.createMessagesTask().setMessageTaskType(MessagesTask.MessageTaskType.ALL).add();
        simpleComTask.save();

        ComTask reloadedComTask = getTaskService().findComTask(simpleComTask.getId()).get();
        MessagesTask reloadedMessageTask = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);

        reloadedMessageTask.setDeviceMessageCategories(categories);
        reloadedMessageTask.save();

        reloadedComTask = getTaskService().findComTask(simpleComTask.getId()).get();
        reloadedMessageTask = getTaskByType(reloadedComTask.getProtocolTasks(), MessagesTask.class);

        assertThat(reloadedMessageTask.getMessageTaskType()).isEqualTo(MessagesTask.MessageTaskType.SELECTED);
        assertThat(reloadedMessageTask.getDeviceMessageCategories()).hasSize(2);
    }

    private void setupReadingTypeInExistingTransaction() {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED)
                .flow(FORWARD)
                .measure(ENERGY)
                .in(KILO, WATTHOUR)
                .accumulate(Accumulation.BULKQUANTITY)
                .code();
        ReadingType readingType = getMeteringService().getReadingType(code).get();
        registerType = getMasterDataService().findRegisterTypeByReadingType(readingType).get();
    }

}