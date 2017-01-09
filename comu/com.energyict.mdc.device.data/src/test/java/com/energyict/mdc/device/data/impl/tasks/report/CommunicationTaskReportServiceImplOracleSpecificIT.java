package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider;
import com.energyict.mdc.device.data.impl.OracleIntegrationPersistence;
import com.energyict.mdc.device.data.impl.search.DeviceSearchDomain;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
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
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

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
        com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel.class);
        int anySecurityLevel = 0;
        when(authenticationAccessLevel.getId()).thenReturn(anySecurityLevel);
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Collections.singletonList(authenticationAccessLevel));
        com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel encryptionAccessLevel = mock(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(anySecurityLevel);
        when(this.deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.singletonList(encryptionAccessLevel));
        DeviceType deviceType = oracleIntegrationPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceMessageIds.forEach(deviceConfiguration::createDeviceMessageEnablement);
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
        } else {
            EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = oracleIntegrationPersistence.getMeteringGroupsService().createEnumeratedEndDeviceGroup()
                    .setName("myDevices")
                    .setMRID("static")
                    .create();
            Device device = oracleIntegrationPersistence.getDeviceService()
                    .newDevice(deviceConfiguration, "myDevice", "ZAFO007", Instant.now());
            device.save();
            device.addToGroup(enumeratedEndDeviceGroup, range(Instant.EPOCH, BoundType.CLOSED, Instant.now(), BoundType.OPEN));
            return enumeratedEndDeviceGroup;
        }
    }


    private QueryEndDeviceGroup findOrCreateQueryEndDeviceGroup() {
        Optional<EndDeviceGroup> endDeviceGroup = oracleIntegrationPersistence.getMeteringGroupsService().findEndDeviceGroup("dynamic");
        if (endDeviceGroup.isPresent()) {
            return (QueryEndDeviceGroup) endDeviceGroup.get();
        } else {
            return oracleIntegrationPersistence
                    .getMeteringGroupsService()
                    .createQueryEndDeviceGroup()
                    .setMRID("dynamic")
                    .setSearchDomain(oracleIntegrationPersistence.getDeviceSearchDomain())
                    .setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_ENDDEVICE_QUERYPROVIDER)
                    .withConditions(buildSearchablePropertyCondition("deviceType", SearchablePropertyOperator.EQUAL, Collections.singletonList("1")))
                    .create();
        }
    }

    private SearchablePropertyValue buildSearchablePropertyCondition(String property, SearchablePropertyOperator operator, List<String> values) {
        DeviceSearchDomain deviceSearchDomain = oracleIntegrationPersistence.getDeviceSearchDomain();
        Optional<SearchableProperty> searchableProperty = deviceSearchDomain.getProperties().stream().filter(p -> property.equals(p.getName())).findFirst();
        if (searchableProperty.isPresent()) {
            SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
            valueBean.operator = operator;
            valueBean.values = values;
            SearchablePropertyValue searchablePropertyValue = new SearchablePropertyValue(searchableProperty.get());
            searchablePropertyValue.setValueBean(valueBean);
            return searchablePropertyValue;
        }
        throw new IllegalArgumentException("Searchable property with name '" + property + "' is not found");
    }
}