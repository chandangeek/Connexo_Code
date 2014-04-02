package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.PersistenceTest;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.TopologyTask;
import org.junit.Test;

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

    private static final TimeDuration minimumClockDifference = new TimeDuration(3, TimeDuration.SECONDS);
    private static final TimeDuration maximumClockDifference = new TimeDuration(5, TimeDuration.MINUTES);
    private static final TimeDuration maximumClockShift = new TimeDuration(13, TimeDuration.SECONDS);

    private ComTask createSimpleComTask() {
        ComTask comTask = getTaskService().createComTask();
        comTask.setName(COM_TASK_NAME);
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
    @ExpectedConstraintViolation(messageId = "{"+Constants.TIMEDURATION_MUST_BE_POSITIVE +"}", property = "maximumClockDiff", strict=false)
    public void testCreateSETClockTaskMaximumIsZero() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SETCLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(TimeDuration.millis(0)).maximumClockShift(maximumClockShift).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.CAN_NOT_BE_EMPTY+"}", property = "minimumClockDiff")
    public void testCreateSETClockTaskWithoutMinimumDuration() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SETCLOCK).maximumClockDifference(maximumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.CAN_NOT_BE_EMPTY+"}", property = "maximumClockDiff")
    public void testCreateSETClockTaskWithoutMaximumDuration() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SETCLOCK).minimumClockDifference(minimumClockDifference).maximumClockShift(maximumClockShift).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.MIN_MUST_BE_BELOW_MAX+"}", strict=false)
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
    @ExpectedConstraintViolation(messageId = "{"+Constants.CAN_NOT_BE_EMPTY +"}", property = "maximumClockShift", strict=false)
    public void testCreateSYNCClockTaskMaximumShiftIsZero() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SYNCHRONIZECLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(maximumClockDifference).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.TIMEDURATION_MUST_BE_POSITIVE+"}", property = "maximumClockShift")
    public void testCreateSYNCClockTaskWithoutMaximumDuration() throws Exception {
        ComTask comTask = createSimpleComTask();
        comTask.createClockTask(ClockTaskType.SYNCHRONIZECLOCK).minimumClockDifference(minimumClockDifference).maximumClockDifference(maximumClockDifference).maximumClockShift(TimeDuration.millis(0)).add();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.CAN_NOT_BE_EMPTY+"}", property = "minimumClockDiff")
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
    @ExpectedConstraintViolation(messageId = "{"+Constants.MIN_MUST_BE_BELOW_MAX+"}", strict=false)
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
}
