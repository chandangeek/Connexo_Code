package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.tasks.BasicCheckTask;
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
        return comTask;
    }

    private ComTask createComTaskWithBasicCheck() {
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
        ComTask comTask = createComTaskWithBasicCheck();
        BasicCheckTask basicCheckTask = getTaskByType(comTask.getProtocolTasks(), BasicCheckTask.class);
        assertThat(basicCheckTask.verifySerialNumber()).isFalse();
        basicCheckTask.setVerifySerialNumber(true);
        basicCheckTask.save();

        BasicCheckTask refreshedBasicCheckTask = getTaskByType(comTask.getProtocolTasks(), BasicCheckTask.class);
        // asserts
        assertThat(getTaskByType(comTask.getProtocolTasks(), BasicCheckTask.class).verifySerialNumber()).isTrue();
    }

//
//
//    @Test
//
//    public void updateWithDeleteAndNewProtocolTaskTypeTest() throws BusinessException, SQLException {
//
//        ComTask comTask = createComTaskWithBasicAndClockTask();
//
//        ComTaskShadow shadow = comTask.getShadow();
//
//        BasicCheckTaskShadow basicCheckTaskShadow = BasicCheckTaskImplTest.createBasicCheckTaskShadow();
//
//        BasicCheckTaskShadow shadowToRemove = getTheBasicCheckTaskShadowFromTheList(shadow.getProtocolTaskShadows());
//
//        shadow.removeProtocolTask(shadowToRemove);
//
//        shadow.addProtocolTask(basicCheckTaskShadow);
//
//
//
//        //business method
//
//        comTask.update(shadow);
//
//        assertNotNull(comTask.getProtocolTasks());
//
//        assertEquals("We should still have 2 protocolTask, a ClockTask and a BasicCheckTask", 2, comTask.getProtocolTasks().size());
//
//    }
//
//
//
//    @Test(expected = DuplicateException.class)
//
//    public void createWithDuplicateNameTest() throws BusinessException, SQLException {
//
//        createComTaskWithBasicAndClockTask();
//
//        createSimpleComTask(createSimpleComTaskShadow());
//
//
//
//        // we expect to fail with a duplicate exception
//
//    }
//
//
//
//    @Test
//
//    public void loadTest() throws BusinessException, SQLException {
//
//        ComTask comTask = createComTaskWithBasicAndClockTask();
//
//        ComTask loadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
//
//
//
//        // asserts
//
//        assertEquals(comTask.getName(), loadedComTask.getName());
//
//        assertEquals(comTask.storeData(), loadedComTask.storeData());
//
//        assertArrayEquals(comTask.getProtocolTasks().toArray(), loadedComTask.getProtocolTasks().toArray());
//
//    }
//
//
//
//    @Test
//
//    public void deleteTest() throws BusinessException, SQLException {
//
//        ComTask comTask = createComTaskWithBasicAndClockTask();
//
//        List<ProtocolTask> protocolTasks = comTask.getProtocolTasks();
//
//        comTask.delete();
//
//
//
//        for (ProtocolTask protocolTask : protocolTasks) {
//
//            assertNull(ManagerFactory.getCurrent().getProtocolTaskFactory().find(protocolTask.getId()));
//
//        }
//
//    }
//
//
//
//    @Test(expected = BusinessException.class)
//
//    public void testDeleteWhenUsedByDeviceConfigurations () throws BusinessException, SQLException {
//
//        ComTask comTask = this.createComTaskWithBasicAndClockTask();
//
//        ServerComTaskEnablementFactory comTaskEnablementFactory = mock(ServerComTaskEnablementFactory.class);
//
//        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
//
//        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
//
//        when(deviceConfiguration.getName()).thenReturn("testDeleteWhenUsedByDeviceConfigurations");
//
//        DeviceCommunicationConfiguration deviceCommunicationConfiguration = mock(DeviceCommunicationConfiguration.class);
//
//        when(deviceCommunicationConfiguration.getDeviceConfiguration()).thenReturn(deviceConfiguration);
//
//        when(deviceConfiguration.getCommunicationConfiguration()).thenReturn(deviceCommunicationConfiguration);
//
//        when(comTaskEnablement.getDeviceCommunicationConfiguration()).thenReturn(deviceCommunicationConfiguration);
//
//        when(comTaskEnablementFactory.findByComTask(comTask)).thenReturn(Arrays.asList(comTaskEnablement));
//
//        ServerManager manager = mock(ServerManager.class);
//
//        when(manager.getComTaskEnablementFactory()).thenReturn(comTaskEnablementFactory);
//
//        when(manager.getComTaskExecutionFactory()).thenReturn(mock(ServerComTaskExecutionFactory.class));
//
//        ManagerFactory.setCurrent(manager);
//
//
//
//        try {
//
//            // Business method
//
//            comTask.delete();
//
//        }
//
//        catch (BusinessException e) {
//
//            assertThat(e.getMessageId()).isEqualTo("comTaskXStillInUseByDeviceConfigurationY");
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
//    public void testDeleteWhenUsedByComTaskExecution () throws BusinessException, SQLException {
//
//        ComTask comTask = this.createComTaskWithBasicAndClockTask();
//
//        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
//
//        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
//
//        when(comTaskExecutionFactory.findByComTask(comTask)).thenReturn(Arrays.asList(comTaskExecution));
//
//        ServerManager manager = mock(ServerManager.class);
//
//        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
//
//        ManagerFactory.setCurrent(manager);
//
//
//
//        try {
//
//            // Business method
//
//            comTask.delete();
//
//        }
//
//        catch (BusinessException e) {
//
//            assertThat(e.getMessageId()).isEqualTo("comTaskXStillInUseByAtLeastOneComTaskExecution");
//
//            throw e;
//
//        }
//
//    }
//
//
//
//    /**
//
//     * Test the full change of creating a ComTask from a shadow.
//
//     * This will create a LoadProfilesTask which has 1 LoadProfileType usage
//
//     *
//
//     * @throws BusinessException
//
//     * @throws SQLException
//
//     */
//
//    @Test
//
//    public void updateWithNewNameAndTaskUsageTest() throws BusinessException, SQLException {
//
//        final String newName = "NewComTaskNameAfterUpdate";
//
//        final boolean failIfLoadProfileConfigMisMatch = true;
//
//        final boolean meterEventsFromStatusFlags = true;
//
//        final String loadProfileTypeDescription = "Standard LoadProfileType description";
//
//        final ObisCode loadProfileTypeObisCode = ObisCode.fromString("1.0.99.1.0.255");
//
//        final int loadProfileTypeInterval = 900;
//
//        final String loadProfileTypeName = "LoadProfileTypeName";
//
//
//
//        ComTaskShadow shadow = new ComTaskShadow();
//
//        shadow.setName(COM_TASK_NAME);
//
//        shadow.addProtocolTask(BasicCheckTaskImplTest.createBasicCheckTaskShadow());
//
//        ComTask comTask = createSimpleComTask(shadow);
//
//        ComTaskShadow comTaskShadow = comTask.getShadow();
//
//
//
//        LoadProfileTypeShadow loadProfileTypeShadow = new LoadProfileTypeShadow();
//
//        loadProfileTypeShadow.setDescription(loadProfileTypeDescription);
//
//        loadProfileTypeShadow.setObisCode(loadProfileTypeObisCode);
//
//        loadProfileTypeShadow.setName(loadProfileTypeName);
//
//        loadProfileTypeShadow.setInterval(new TimeDuration(loadProfileTypeInterval));
//
//        LoadProfileType loadProfileType = MeteringWarehouse.getCurrent().getLoadProfileTypeFactory().create(loadProfileTypeShadow);
//
//
//
//        LoadProfilesTaskShadow loadProfilesTaskShadow = new LoadProfilesTaskShadow();
//
//        loadProfilesTaskShadow.setFailIfLoadProfileConfigurationMisMatch(failIfLoadProfileConfigMisMatch);
//
//        loadProfilesTaskShadow.setMeterEventsFromStatusFlags(meterEventsFromStatusFlags);
//
//        loadProfilesTaskShadow.addLoadProfileType(loadProfileType.getShadow()); // we refetch the shadow so we get the updated ID
//
//
//
//        comTaskShadow.addProtocolTask(loadProfilesTaskShadow);
//
//        comTaskShadow.setName(newName);
//
//
//
//        // business method
//
//        comTask.update(comTaskShadow);
//
//
//
//        // asserts
//
//        assertEquals("Name should be changed", newName, comTask.getName());
//
//        assertEquals("Should have 2 ProtocolTask", 2, comTask.getProtocolTasks().size());
//
//        assertThat(comTask.getProtocolTasks()).areExactly(1, new Condition<Object>() {
//
//            @Override
//
//            public boolean matches(Object value) {
//
//                return value instanceof LoadProfilesTask;
//
//            }
//
//        });
//
//        assertThat(comTask.getProtocolTasks()).areExactly(1, new Condition<Object>() {
//
//            @Override
//
//            public boolean matches(Object value) {
//
//                return value instanceof BasicCheckTask;
//
//            }
//
//        });
//
//        LoadProfilesTask loadProfilesTask = null;
//
//        for (ProtocolTask protocolTask : comTask.getProtocolTasks()) {
//
//            if(protocolTask instanceof LoadProfilesTask){
//
//                loadProfilesTask = (LoadProfilesTask) protocolTask;
//
//            }
//
//        }
//
//        assertNotNull("loadProfilesTask should not be null because we already validated that the list contains an instance of this type.",loadProfilesTask);
//
//        assertTrue("The task should fail if channelconfig mismatch", loadProfilesTask.failIfLoadProfileConfigurationMisMatch());
//
//        assertTrue("The task should create meterevents from statusflags", loadProfilesTask.createMeterEventsFromStatusFlags());
//
//        assertFalse("The task should not mark intervals as bad time", loadProfilesTask.doMarkIntervalsAsBadTime());
//
//        assertNotNull(loadProfilesTask.getLoadProfileTypes());
//
//        assertEquals("The task should have 1 loadProfileType", 1, loadProfilesTask.getLoadProfileTypes().size());
//
//        LoadProfileType lpt = loadProfilesTask.getLoadProfileTypes().get(0);
//
//        assertEquals("The name of the loadProfileType should be " + loadProfileTypeName, loadProfileTypeName, lpt.getName());
//
//        assertEquals(loadProfileTypeDescription, lpt.getDescription());
//
//        assertEquals("The interval should be 900s", loadProfileTypeInterval, lpt.getInterval().getSeconds());
//
//    }
//
//
//
//    @Test
//
//    public void createForProtocolTaskXTest() throws BusinessException, SQLException {
//
//        ComTask comTask = createSimpleComTask(createComTaskShadowWithoutViolations());
//
//
//
//        ComTask loadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
//
//        assertEquals("Should have 1 protocolTask", 1, loadedComTask.getProtocolTasks().size());
//
//
//
//        comTask.createClockTask(ClockTaskImplTest.createClockTaskShadow());
//
//        loadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
//
//        assertEquals("Should have 2 protocolTask", 2, loadedComTask.getProtocolTasks().size());
//
//
//
//        comTask.createLoadProfilesTask(LoadProfilesTaskImplTest.createSimpleLoadProfileTaskShadow());
//
//        loadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
//
//        assertEquals("Should have 3 protocolTask", 3, loadedComTask.getProtocolTasks().size());
//
//
//
//        comTask.createLogbooksTask(new LogBooksTaskShadow());
//
//        loadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
//
//        assertEquals("Should have 4 protocolTask", 4, loadedComTask.getProtocolTasks().size());
//
//
//
//        comTask.createMessagesTask(new MessagesTaskShadow());
//
//        loadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
//
//        assertEquals("Should have 5 protocolTask", 5, loadedComTask.getProtocolTasks().size());
//
//
//
//        comTask.createBasicCheckTask(BasicCheckTaskImplTest.createBasicCheckTaskShadow());
//
//        loadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
//
//        assertEquals("Should have 6 protocolTask", 6, loadedComTask.getProtocolTasks().size());
//
//
//
//        comTask.createRegistersTask(new RegistersTaskShadow());
//
//        loadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
//
//        assertEquals("Should have 7 protocolTask", 7, loadedComTask.getProtocolTasks().size());
//
//
//
//        comTask.createStatusInformationTask(new StatusInformationTaskShadow());
//
//        loadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
//
//        assertEquals("Should have 8 protocolTask", 8, loadedComTask.getProtocolTasks().size());
//
//
//
//        comTask.createTopologyTask(TopologyTaskImplTest.createSimpleTopologyTaskShadow());
//
//        loadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
//
//        assertEquals("Should have 9 protocolTask", 9, loadedComTask.getProtocolTasks().size());
//
//    }
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
//        ComTask reloadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
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
//        ComTask reloadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
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
////        ComTask reloadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
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
////        ComTask reloadedComTask = ManagerFactory.getCurrent().getComTaskFactory().find(comTask.getId());
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
