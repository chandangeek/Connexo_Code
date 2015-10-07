package com.energyict.mdc.device.data.impl.tasks.report;

import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider;
import com.energyict.mdc.device.data.impl.OracleIntegrationPersistence;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.collect.BoundType;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.google.common.collect.Range.range;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests methods of the {@link CommunicationTaskReportServiceImpl} component
 * that are using oracle specific syntax that is not supported on H2.
 * Run these tests manually on a clean schema (see setup method).
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class CommunicationTaskReportServiceImplOracleSpecificIT {

    private static final String DEVICE_TYPE_NAME = CommunicationTaskReportServiceImplOracleSpecificIT.class.getSimpleName() + "Type";
    private static final String DEVICE_CONFIGURATION_NAME = CommunicationTaskReportServiceImplOracleSpecificIT.class.getSimpleName() + "Config";
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    private DeviceConfiguration deviceConfiguration;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;

    private static OracleIntegrationPersistence oracleIntegrationPersistence;

    @BeforeClass
    public static void initialize() throws SQLException {
        oracleIntegrationPersistence = new OracleIntegrationPersistence();
        initializeClock();
        oracleIntegrationPersistence.initializeDatabase("ConnectionTaskServiceImplOracleSpecificTest.mdc.device.data");
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        oracleIntegrationPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return oracleIntegrationPersistence.getTransactionService();
    }

    @Before
    public void initializeMocks() {
        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        EnumSet<DeviceMessageId> deviceMessageIds = EnumSet.of(DeviceMessageId.CONTACTOR_CLOSE,
                DeviceMessageId.CONTACTOR_OPEN,
                DeviceMessageId.CONTACTOR_ARM,
                DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT,
                DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE,
                DeviceMessageId.DISPLAY_SET_MESSAGE_WITH_OPTIONS);
        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);
        AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        int anySecurityLevel = 0;
        when(authenticationAccessLevel.getId()).thenReturn(anySecurityLevel);
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authenticationAccessLevel));
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(anySecurityLevel);
        when(this.deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(encryptionAccessLevel));
        DeviceType deviceType = oracleIntegrationPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        deviceType.save();
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

    private static void initializeClock() {
        when(oracleIntegrationPersistence.getClock().getZone()).thenReturn(utcTimeZone.toZoneId());
        when(oracleIntegrationPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());
    }

    @Transactional
    @Test
    public void getCommunicationTaskBreakdownsDoesNotProduceSQLExceptions() {
        // Business method
        oracleIntegrationPersistence.getCommunicationTaskReportService().getCommunicationTaskBreakdowns();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getCommunicationTaskBreakdownsWithEnumeratedDeviceGroupDoesNotProduceSQLExceptions() {
        EnumeratedEndDeviceGroup deviceGroup = findOrCreateEnumeratedEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getCommunicationTaskReportService().getCommunicationTaskBreakdowns(deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getCommunicationTaskBreakdownsWithQueryDeviceGroupDoesNotProduceSQLExceptions() {
        QueryEndDeviceGroup deviceGroup = findOrCreateQueryEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getCommunicationTaskReportService().getCommunicationTaskBreakdowns(deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getComTaskExecutionStatusCountDoesNotProduceSQLExceptions() {
        // Business method
        oracleIntegrationPersistence.getCommunicationTaskReportService().getComTaskExecutionStatusCount();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getComTaskExecutionStatusCountWithEnumeratedDeviceGroupDoesNotProduceSQLExceptions() {
        EnumeratedEndDeviceGroup deviceGroup = findOrCreateEnumeratedEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getCommunicationTaskReportService().getComTaskExecutionStatusCount(deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getComTaskExecutionStatusCountWithQueryDeviceGroupDoesNotProduceSQLExceptions() {
        QueryEndDeviceGroup deviceGroup = findOrCreateQueryEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getCommunicationTaskReportService().getComTaskExecutionStatusCount(deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getCommunicationTasksComScheduleBreakdownDoesNotProduceSQLExceptions() {
        // Business method
        oracleIntegrationPersistence.getCommunicationTaskReportService().getCommunicationTasksComScheduleBreakdown(this.testTaskStatusses());

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getCommunicationTasksComScheduleBreakdownWithEnumeratedDeviceGroupDoesNotProduceSQLExceptions() {
        EnumeratedEndDeviceGroup deviceGroup = findOrCreateEnumeratedEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getCommunicationTaskReportService().getCommunicationTasksComScheduleBreakdown(this.testTaskStatusses(), deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getCommunicationTasksComScheduleBreakdownWithQueryDeviceGroupDoesNotProduceSQLExceptions() {
        QueryEndDeviceGroup deviceGroup = findOrCreateQueryEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getCommunicationTaskReportService().getCommunicationTasksComScheduleBreakdown(this.testTaskStatusses(), deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getCommunicationTasksDeviceTypeBreakdownDoesNotProduceSQLExceptions() {
        // Business method
        oracleIntegrationPersistence.getCommunicationTaskReportService().getCommunicationTasksDeviceTypeBreakdown(this.testTaskStatusses());

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getCommunicationTasksDeviceTypeBreakdownWithEnumeratedDeviceGroupDoesNotProduceSQLExceptions() {
        EnumeratedEndDeviceGroup deviceGroup = findOrCreateEnumeratedEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getCommunicationTaskReportService().getCommunicationTasksDeviceTypeBreakdown(this.testTaskStatusses(), deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getCommunicationTasksDeviceTypeBreakdownWithQueryDeviceGroupDoesNotProduceSQLExceptions() {
        QueryEndDeviceGroup deviceGroup = findOrCreateQueryEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getCommunicationTaskReportService().getCommunicationTasksDeviceTypeBreakdown(this.testTaskStatusses(), deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    private Set<TaskStatus> testTaskStatusses() {
        return EnumSet.of(TaskStatus.Waiting, TaskStatus.Failed, TaskStatus.NeverCompleted, TaskStatus.Pending, TaskStatus.Busy, TaskStatus.Retrying);
    }

    private EnumeratedEndDeviceGroup findOrCreateEnumeratedEndDeviceGroup() {
        Optional<EndDeviceGroup> endDeviceGroup = oracleIntegrationPersistence.getMeteringGroupsService().findEndDeviceGroup("static");
        if (endDeviceGroup.isPresent()) {
            return (EnumeratedEndDeviceGroup) endDeviceGroup.get();
        }
        else {
            EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = oracleIntegrationPersistence.getMeteringGroupsService().createEnumeratedEndDeviceGroup().setName("myDevices").create();
            Device device = oracleIntegrationPersistence.getDeviceService().newDevice(deviceConfiguration, "myDevice", "ZAFO007");
            device.save();
            device.addToGroup(enumeratedEndDeviceGroup, range(Instant.EPOCH, BoundType.CLOSED, Instant.now(), BoundType.OPEN));
            enumeratedEndDeviceGroup.setMRID("static");
            enumeratedEndDeviceGroup.setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_ENDDEVICE_QUERYPROVIDER);
            enumeratedEndDeviceGroup.save();
            return enumeratedEndDeviceGroup;
        }
    }


    private QueryEndDeviceGroup findOrCreateQueryEndDeviceGroup() {
        Optional<EndDeviceGroup> endDeviceGroup = oracleIntegrationPersistence.getMeteringGroupsService().findEndDeviceGroup("dynamic");
        if (endDeviceGroup.isPresent()) {
            return (QueryEndDeviceGroup) endDeviceGroup.get();
        }
        else {
            Condition conditionDevice = Condition.TRUE.and(where("deviceConfiguration.deviceType.name").isEqualTo("myType"));
            return oracleIntegrationPersistence
                    .getMeteringGroupsService()
                    .createQueryEndDeviceGroup(conditionDevice)
                        .setMRID("dynamic")
                        .setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_ENDDEVICE_QUERYPROVIDER)
                        .create();
        }
    }

}