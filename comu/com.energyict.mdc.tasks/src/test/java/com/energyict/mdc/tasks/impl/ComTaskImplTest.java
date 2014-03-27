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

    private ComTask createSimpleComTask() {
        ComTask comTask = getTaskService().createComTask();
        comTask.setName(COM_TASK_NAME);
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        comTask.createStatusInformationTask();
        comTask.save();
        return getTaskService().findComTask(comTask.getId());
    }

    private ComTask createComTaskWithBasicCheckAndStatusInformation() {
        ComTask comTask = getTaskService().createComTask();
        comTask.setName(COM_TASK_NAME);
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        comTask.createStatusInformationTask();
        comTask.createBasicCheckTask().add();
        comTask.save();
        return getTaskService().findComTask(comTask.getId()); // to make sure all elements in the composition are properly loaded
    }

    private <T extends ProtocolTask> T getTaskByType(List<? extends ProtocolTask> protocolTasks, Class<T> clazz) {
        for (ProtocolTask protocolTask : protocolTasks) {
            if (clazz.isAssignableFrom(protocolTask.getClass())) {
                return (T) protocolTask;
            }
        }
        return null;
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
        ComTask comTask = getTaskService().createComTask();

        // Business method
        String type = comTask.getType();

        // Asserts
        assertThat(type).doesNotContain(".Server");
    }



    @Test
    @Transactional
    public void createWithoutViolations() {
        ComTask comTask = createSimpleComTask();
        // asserts
        assertThat(comTask).isNotNull();
        assertThat(comTask.storeData()).isTrue();
    }



    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.CAN_NOT_BE_EMPTY+"}", property = "name")
    public void createWithoutNameTest()  {
        ComTask comTask = getTaskService().createComTask();
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        comTask.createStatusInformationTask();
        comTask.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.CAN_NOT_BE_EMPTY+"}", property = "name")
    public void createWithWhitespaceNameTest()  {
        ComTask comTask = getTaskService().createComTask();
        comTask.setName("           ");
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.setMaxNrOfTries(1);
        comTask.createStatusInformationTask();
        comTask.save();
    }

    @Test
    @Transactional
    public void createWithoutViolationsWithProtocolTasks() {
        ComTask comTask = getTaskService().createComTask();
        comTask.setName("name");
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
        ComTask comTask = getTaskService().createComTask();
        comTask.setName(COM_TASK_NAME);
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
        ComTask comTask = createSimpleComTask();
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
        createSimpleComTask();
        createSimpleComTask();
        // we expect to fail with a duplicate exception
    }

    @Test
    @Transactional
    public void deleteTest()  {
        ComTask comTask = createSimpleComTask();
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
        ComTask comTask = createSimpleComTask();

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
//
//
//
//    @Test (expected = BusinessException.class)
//
//    public void createWithNoProtocolTasksTest() throws BusinessException, SQLException {
//
//        try {
//
//            createSimpleComTask(createSimpleComTaskShadow());
//
//        } catch (BusinessException e) {
//
//            if(!e.getMessageId().equals("noProtocolTaskForComTaskX")){
//
//                fail();
//
//            }
//
//            throw e;
//
//        }
//
//    }
//
//
//
//    @Test(expected = BusinessException.class)
//
//    public void updateWithNoProtocolTasks() throws BusinessException, SQLException {
//
//        ComTask comTask = createSimpleComTask(createComTaskShadowWithoutViolations());
//
//        ComTaskShadow shadow = comTask.getShadow();
//
//        ShadowList<ProtocolTaskShadow> protocolTaskShadows = shadow.getProtocolTaskShadows();
//
//        List<ProtocolTaskShadow> shadowsToRemove = new ArrayList<>(protocolTaskShadows.size());
//
//        for (ProtocolTaskShadow protocolTaskShadow : protocolTaskShadows) {
//
//            shadowsToRemove.add(protocolTaskShadow);
//
//        }
//
//        for (ProtocolTaskShadow shadowToRemove : shadowsToRemove) {
//
//            protocolTaskShadows.remove(shadowToRemove);
//
//        }
//
//
//
//        try {
//
//            comTask.update(shadow);
//
//        } catch (BusinessException e) {
//
//            if(!e.getMessageId().equals("noProtocolTaskForComTaskX")){
//
//                fail();
//
//            }
//
//            throw e;
//
//        }
//
//    }
//
//
//
//    @Test
//
//    public void testStoreDataTrue() throws BusinessException, SQLException {
//
//        ComTaskShadow shadow = new ComTaskShadow();
//
//        shadow.setStoreData(STORE_DATA_TRUE);
//
//        shadow.setName(COM_TASK_NAME);
//
//        shadow.addProtocolTask(BasicCheckTaskImplTest.createBasicCheckTaskShadow());
//
//        ComTask comTask = createSimpleComTask(shadow);
//
//
//
//        // Business Method
//
//        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
//
//
//
//        // asserts
//
//        assertThat(reloadedComTask.storeData()).isTrue();
//
//    }
//
//
//
//    @Test
//
//    public void testStoreDataFalse() throws BusinessException, SQLException {
//
//        ComTaskShadow shadow = new ComTaskShadow();
//
//        shadow.setStoreData(false);
//
//        shadow.setName(COM_TASK_NAME);
//
//        shadow.addProtocolTask(BasicCheckTaskImplTest.createBasicCheckTaskShadow());
//
//        ComTask comTask = createSimpleComTask(shadow);
//
//
//
//        // Business Method
//
//        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
//
//
//
//        // asserts
//
//        assertThat(reloadedComTask.storeData()).isFalse();
//
//    }
//
//
//
////    @Test
//
////    public void testEmptyRescheduleRetryDelay() throws BusinessException, SQLException {
//
////        ComTaskShadow shadow = new ComTaskShadow();
//
////        shadow.setStoreData(false);
//
////        shadow.setName(COM_TASK_NAME);
//
////        shadow.addProtocolTask(BasicCheckTaskImplTest.createBasicCheckTaskShadow());
//
////        ComTask comTask = createSimpleComTask(shadow);
//
////
//
////        // Business Method
//
////        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
//
////
//
////        // asserts
//
////        assertThat(reloadedComTask.getRescheduleDelay()).isEqualTo(null);
//
////    }
//
////
//
////    @Test
//
////    public void testWithValidRescheduleRetryDelay() throws BusinessException, SQLException {
//
////        final TimeDuration rescheduleRetryDelay = new TimeDuration(900);
//
////        ComTaskShadow shadow = new ComTaskShadow();
//
////        shadow.setStoreData(false);
//
////        shadow.setName(COM_TASK_NAME);
//
////        shadow.setRescheduleRetryDelay(rescheduleRetryDelay);
//
////        shadow.addProtocolTask(BasicCheckTaskImplTest.createBasicCheckTaskShadow());
//
////        ComTask comTask = createSimpleComTask(shadow);
//
////
//
////        // Business Method
//
////        ComTask reloadedComTask = getTaskService().findComTask(comTask.getId());
//
////
//
////        // asserts
//
////        assertThat(reloadedComTask.getRescheduleDelay()).isEqualTo(rescheduleRetryDelay);
//
////    }
//
////
//
////    @Test
//
////    public void testRescheduleRetryDelayWithValueBelowMinimum() throws BusinessException, SQLException {
//
////        final TimeDuration rescheduleRetryDelay = new TimeDuration(5);
//
////        ComTaskShadow shadow = new ComTaskShadow();
//
////        shadow.setStoreData(false);
//
////        shadow.setName(COM_TASK_NAME);
//
////        shadow.setRescheduleRetryDelay(rescheduleRetryDelay);
//
////        shadow.addProtocolTask(BasicCheckTaskImplTest.createBasicCheckTaskShadow());
//
////        try {
//
////            createSimpleComTask(shadow);
//
////        } catch (BusinessException e) {
//
////            if(!e.getMessageId().equals("invalidValueXForYBelowZ")){
//
////                fail("Expected an exception indicating that the given value is below the minimum, but was " + e.getMessage());
//
////            }
//
////        }
//
////    }
//
////
//
////    @Test
//
////    public void testForNegativeRescheduleRetryDelay() throws SQLException, BusinessException {
//
////        final TimeDuration rescheduleRetryDelay = new TimeDuration(-100);
//
////        ComTaskShadow shadow = new ComTaskShadow();
//
////        shadow.setStoreData(false);
//
////        shadow.setName(COM_TASK_NAME);
//
////        shadow.setRescheduleRetryDelay(rescheduleRetryDelay);
//
////        shadow.addProtocolTask(BasicCheckTaskImplTest.createBasicCheckTaskShadow());
//
////        try {
//
////            createSimpleComTask(shadow);
//
////        } catch (InvalidValueException e) {
//
////            if(!e.getMessageId().equals("invalidNegativeValueForY")){
//
////                fail("Expected an exception indicating that the given value can not be negative, but was " + e.getMessage());
//
////            }
//
////        }
//
////    }
//
//
//
//    private BasicCheckTaskShadow getTheBasicCheckTaskShadowFromTheList(final List<ProtocolTaskShadow> shadowList) {
//
//        for (ProtocolTaskShadow shadow : shadowList) {
//
//            if (shadow instanceof BasicCheckTaskShadow) {
//
//                return (BasicCheckTaskShadow) shadow;
//
//            }
//
//        }
//
//        throw new IllegalArgumentException("We should have found a BasicCheckTaskShadow!");
//
//    }
//
//
//
//    private BasicCheckTask getTheBasicCheckTaskFromList(final List<ProtocolTask> protocolTasks) {
//
//        for (ProtocolTask protocolTask : protocolTasks) {
//
//            if (protocolTask instanceof BasicCheckTask) {
//
//                return (BasicCheckTask) protocolTask;
//
//            }
//
//        }
//
//        throw new IllegalArgumentException("We should have found a BasicCheckTask!");
//
//    }
//
//
}
