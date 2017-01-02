package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider;
import com.energyict.mdc.device.data.impl.OracleIntegrationPersistence;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.energyict.mdc.tasks.ComTask;
import com.google.common.collect.BoundType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import static com.google.common.collect.Range.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests methods of the {@link CommunicationTaskServiceImpl} component
 * that are using oracle specific syntax that is not supported on H2.
 * Run these tests manually on a clean schema (see setup method).
 * Primary goal of these tests is to verify there are no sql exceptions along the way, hence the lack of asserts
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class CommunicationTaskServiceImplOracleSpecificIT {

    private static final String DEVICE_TYPE_NAME = CommunicationTaskServiceImplOracleSpecificIT.class.getSimpleName() + "Type";
    private static final String DEVICE_CONFIGURATION_NAME = CommunicationTaskServiceImplOracleSpecificIT.class.getSimpleName() + "Config";
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    private static OracleIntegrationPersistence oracleIntegrationPersistence;
    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    private DeviceType deviceType;

    @BeforeClass
    public static void initialize() throws SQLException {
        oracleIntegrationPersistence = new OracleIntegrationPersistence();
        initializeClock();
        oracleIntegrationPersistence.initializeDatabase("CommunicationTaskServiceImplOracleSpecificTest.mdc.device.data");
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        oracleIntegrationPersistence.cleanUpDataBase();
    }

    private static TransactionService getTransactionService() {
        return oracleIntegrationPersistence.getTransactionService();
    }

    private static void initializeClock() {
        when(oracleIntegrationPersistence.getClock().getZone()).thenReturn(utcTimeZone.toZoneId());
        when(oracleIntegrationPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());
    }

    @Before
    public void initializeMocks() {
        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);

        List<com.energyict.mdc.upl.messages.DeviceMessageSpec> deviceMessageIds = new ArrayList<>();
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec1 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec1.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN.dbValue());
        deviceMessageIds.add(deviceMessageSpec1);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec2 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec2.getId()).thenReturn(DeviceMessageId.CONTACTOR_ARM.dbValue());
        deviceMessageIds.add(deviceMessageSpec2);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec3 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec3.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT.dbValue());
        deviceMessageIds.add(deviceMessageSpec3);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec4 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec4.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.dbValue());
        deviceMessageIds.add(deviceMessageSpec4);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec5 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec5.getId()).thenReturn(DeviceMessageId.DISPLAY_SET_MESSAGE_WITH_OPTIONS.dbValue());
        deviceMessageIds.add(deviceMessageSpec5);

        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);
        AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        int anySecurityLevel = 0;
        when(authenticationAccessLevel.getId()).thenReturn(anySecurityLevel);
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Collections.singletonList(authenticationAccessLevel));
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(anySecurityLevel);
        when(this.deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.singletonList(encryptionAccessLevel));
        this.deviceType = oracleIntegrationPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceMessageIds.stream().forEach(deviceConfiguration::createDeviceMessageEnablement);
        deviceConfiguration.activate();
        SecurityPropertySetBuilder securityPropertySetBuilder = deviceConfiguration.createSecurityPropertySet("No Security");
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES3);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES3);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES3);
        securityPropertySetBuilder.addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4);
        securityPropertySetBuilder.authenticationLevel(anySecurityLevel);
        securityPropertySetBuilder.encryptionLevel(anySecurityLevel);
        securityPropertySetBuilder.build();
        this.resetClock();
    }

    @After
    public void resetClock() {
        initializeClock();
    }

    @Test
    @Transactional
    public void testComTaskExecutionStatusCountForQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        oracleIntegrationPersistence.getCommunicationTaskReportService().getComTaskExecutionStatusCount(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testComTaskExecutionStatusCountForEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        oracleIntegrationPersistence.getCommunicationTaskReportService().getComTaskExecutionStatusCount(enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testCommunicationTasksComScheduleBreakdownWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        oracleIntegrationPersistence.getCommunicationTaskReportService().getCommunicationTasksComScheduleBreakdown(EnumSet.of(TaskStatus.Busy), queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testCommunicationTasksComScheduleBreakdownWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        oracleIntegrationPersistence.getCommunicationTaskReportService().getComTaskExecutionStatusCount(enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testCommunicationTasksDeviceTypeBreakdownWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        oracleIntegrationPersistence.getCommunicationTaskReportService().getCommunicationTasksDeviceTypeBreakdown(EnumSet.of(TaskStatus.Busy), queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testCommunicationTasksDeviceTypeBreakdownWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        oracleIntegrationPersistence.getCommunicationTaskReportService().getCommunicationTasksDeviceTypeBreakdown(EnumSet.of(TaskStatus.Busy), enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testComTasksDeviceTypeHeatMapWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        oracleIntegrationPersistence.getCommunicationTaskReportService().getComTasksDeviceTypeHeatMap(queryEndDeviceGroup);
    }

    @Transactional
    @Test
    public void testGetCommunicationTasksComScheduleBreakdown() {
        ComScheduleBuilder scheduleBuilder =
                oracleIntegrationPersistence.getSchedulingService().newComSchedule(
                        "testGetCommunicationTasksComScheduleBreakdown",
                        new TemporalExpression(TimeDuration.days(1)),
                        Instant.now());

        ComTask simpleComTask = oracleIntegrationPersistence.getTaskService().newComTask("Simple task");
        simpleComTask.createStatusInformationTask();
        simpleComTask.save();
        ComSchedule comSchedule = scheduleBuilder.addComTask(simpleComTask).build();

        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = this.breakdownStatusses();
        filter.comSchedules.add(comSchedule);

        // Business method
        Map<TaskStatus, Long> statusCount = oracleIntegrationPersistence.getCommunicationTaskReportService().getComTaskExecutionStatusCount(filter);

        // Assertts
        assertThat(statusCount).isNotNull();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            assertThat(statusCount.get(taskStatus)).isNotNull();
        }
    }

    @Transactional
    @Test
    public void testGetCommunicationTasksBreakdown() {
        ComTask comTask = oracleIntegrationPersistence.getTaskService().newComTask("testGetCommunicationTasksBreakdown");
        comTask.createBasicCheckTask().add();
        comTask.save();
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = this.breakdownStatusses();
        filter.comTasks.add(comTask);

        // Business method
        Map<TaskStatus, Long> statusCount = oracleIntegrationPersistence.getCommunicationTaskReportService().getComTaskExecutionStatusCount(filter);

        // Assertts
        assertThat(statusCount).isNotNull();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            assertThat(statusCount.get(taskStatus)).isNotNull();
        }
    }

    @Transactional
    @Test
    public void testGetComTaskExecutionStatusCount() {
        // Business method
        Map<TaskStatus, Long> statusCount = oracleIntegrationPersistence.getCommunicationTaskReportService().getComTaskExecutionStatusCount();

        // Assertts
        assertThat(statusCount).isNotNull();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            assertThat(statusCount.get(taskStatus)).isNotNull();
        }
    }

    @Transactional
    @Test
    public void testGetCommunicationTasksDeviceTypeBreakdown() {
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = this.breakdownStatusses();
        filter.deviceTypes.add(deviceType);

        // Business method
        Map<TaskStatus, Long> statusCount = oracleIntegrationPersistence.getCommunicationTaskReportService().getComTaskExecutionStatusCount(filter);

        // Assertts
        assertThat(statusCount).isNotNull();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            assertThat(statusCount.get(taskStatus)).isNotNull();
        }
    }

    private Set<TaskStatus> breakdownStatusses() {
        Set<TaskStatus> taskStatuses = EnumSet.noneOf(TaskStatus.class);
        taskStatuses.addAll(EnumSet.of(TaskStatus.Waiting));
        taskStatuses.addAll(EnumSet.of(TaskStatus.Failed, TaskStatus.NeverCompleted));
        taskStatuses.addAll(EnumSet.of(TaskStatus.Pending, TaskStatus.Busy, TaskStatus.Retrying));
        return taskStatuses;
    }

    private EnumeratedEndDeviceGroup findOrCreateEnumeratedEndDeviceGroup() {
        Optional<EndDeviceGroup> endDeviceGroup = oracleIntegrationPersistence.getMeteringGroupsService().findEndDeviceGroup("static");
        if (endDeviceGroup.isPresent()) {
            return (EnumeratedEndDeviceGroup) endDeviceGroup.get();
        } else {
            EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = oracleIntegrationPersistence.getMeteringGroupsService().createEnumeratedEndDeviceGroup()
                    .setName("myDevices")
                    .create();
            Device device = oracleIntegrationPersistence.getDeviceService()
                    .newDevice(deviceConfiguration, "myDevice", "ZAFO007", Instant.now());
            device.save();
            device.addToGroup(enumeratedEndDeviceGroup, range(Instant.EPOCH, BoundType.CLOSED, Instant.now(), BoundType.OPEN));
            enumeratedEndDeviceGroup.setMRID("static");
            enumeratedEndDeviceGroup.update();
            return enumeratedEndDeviceGroup;
        }
    }


    private QueryEndDeviceGroup findOrCreateQueryEndDeviceGroup() {
        Optional<EndDeviceGroup> endDeviceGroup = oracleIntegrationPersistence.getMeteringGroupsService().findEndDeviceGroup("dynamic");
        if (endDeviceGroup.isPresent()) {
            return (QueryEndDeviceGroup) endDeviceGroup.get();
        } else {
            QueryEndDeviceGroup queryEndDeviceGroup = oracleIntegrationPersistence.getMeteringGroupsService().createQueryEndDeviceGroup()
                    .setMRID("dynamic")
                    .setSearchDomain(oracleIntegrationPersistence.getDeviceSearchDomain())
                    .setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_ENDDEVICE_QUERYPROVIDER)
                    .withConditions(buildSearchablePropertyCondition("deviceType", SearchablePropertyOperator.EQUAL, Collections.singletonList("1")))
                    .create();
            return queryEndDeviceGroup;
        }
    }

    private SearchablePropertyValue buildSearchablePropertyCondition(String property, SearchablePropertyOperator operator, List<String> values) {
        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.propertyName = property;
        valueBean.operator = operator;
        valueBean.values = values;
        SearchablePropertyValue searchablePropertyValue = new SearchablePropertyValue(null, valueBean);
        return searchablePropertyValue;
    }
}
