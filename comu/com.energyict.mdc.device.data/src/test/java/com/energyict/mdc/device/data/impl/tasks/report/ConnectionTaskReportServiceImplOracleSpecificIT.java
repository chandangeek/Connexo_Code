/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
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
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

import com.google.common.collect.BoundType;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

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

import static com.google.common.collect.Range.range;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests methods of the {@link ConnectionTaskReportServiceImpl} component
 * that are using oracle specific syntax that is not supported on H2.
 * Run these tests manually on a clean schema (see setup method).
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class ConnectionTaskReportServiceImplOracleSpecificIT {

    private static final String DEVICE_TYPE_NAME = ConnectionTaskReportServiceImplOracleSpecificIT.class.getSimpleName() + "Type";
    private static final String DEVICE_CONFIGURATION_NAME = ConnectionTaskReportServiceImplOracleSpecificIT.class.getSimpleName() + "Config";
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
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Collections.singletonList(authenticationAccessLevel));
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
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

    private static void initializeClock() {
        when(oracleIntegrationPersistence.getClock().getZone()).thenReturn(utcTimeZone.toZoneId());
        when(oracleIntegrationPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());
    }

    @Test
    @Transactional
    public void testConnectionTaskStatusCountWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTaskStatusCount(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionTaskStatusCountWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTaskStatusCount(enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testComPortPoolBreakdownWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getComPortPoolBreakdown(EnumSet.of(TaskStatus.Failed), queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testComPortPoolBreakdownWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getComPortPoolBreakdown(EnumSet.of(TaskStatus.Failed), enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testDeviceTypeBreakdownWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getDeviceTypeBreakdown(EnumSet.of(TaskStatus.Failed), queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testDeviceTypeBreakdownWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getDeviceTypeBreakdown(EnumSet.of(TaskStatus.Failed), enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionTypeBreakdownWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTypeBreakdown(EnumSet.of(TaskStatus.Failed), queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionTypeBreakdownWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTypeBreakdown(EnumSet.of(TaskStatus.Failed), enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionTaskLastComSessionSuccessIndicatorCountWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTaskLastComSessionSuccessIndicatorCount(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionTaskLastComSessionSuccessIndicatorCountWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTaskLastComSessionSuccessIndicatorCount(enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionTypeHeatMapWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTypeHeatMap(enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionTypeHeatMapWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTypeHeatMap(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionsDeviceTypeHeatMapWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionsDeviceTypeHeatMap(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionsDeviceTypeHeatMapWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionsDeviceTypeHeatMap(enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionsComPortPoolHeatMapWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionsComPortPoolHeatMap(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionsComPortPoolHeatMapWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionsComPortPoolHeatMap(enumeratedEndDeviceGroup);
    }

    @Transactional
    @Test
    public void getDeviceTypeBreakdownDoesNotProduceSQLExceptions() {
        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getDeviceTypeBreakdown(this.testTaskStatusses());

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getDeviceTypeBreakdownWithEnumeratedDeviceGroupDoesNotProduceSQLExceptions() {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getDeviceTypeBreakdown(this.testTaskStatusses(), enumeratedEndDeviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getDeviceTypeBreakdownWithQueryDeviceGroupDoesNotProduceSQLExceptions() {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getDeviceTypeBreakdown(this.testTaskStatusses(), queryEndDeviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTaskStatusCountDoesNotProduceSQLExceptions() {
        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTaskStatusCount();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTaskStatusCountWithEnumeratedDeviceGroupDoesNotProduceSQLExceptions() {
        EnumeratedEndDeviceGroup deviceGroup = findOrCreateEnumeratedEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTaskStatusCount(deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTaskStatusCountWithQueryDeviceGroupDoesNotProduceSQLExceptions() {
        QueryEndDeviceGroup deviceGroup = findOrCreateQueryEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTaskStatusCount(deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTypeHeatMapDoesNotProduceSQLExceptions() {
        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTypeHeatMap();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTypeHeatMapWithEnumeratedDeviceGroupDoesNotProduceSQLExceptions() {
        EnumeratedEndDeviceGroup deviceGroup = findOrCreateEnumeratedEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTypeHeatMap(deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTypeHeatMapWithQueryDeviceGroupDoesNotProduceSQLExceptions() {
        QueryEndDeviceGroup deviceGroup = findOrCreateQueryEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTypeHeatMap(deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getComPortPoolBreakdownDoesNotProduceSQLExceptions() {
        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getComPortPoolBreakdown(this.testTaskStatusses());

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getComPortPoolBreakdownWithEnumeratedDeviceGroupDoesNotProduceSQLExceptions() {
        EnumeratedEndDeviceGroup deviceGroup = findOrCreateEnumeratedEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getComPortPoolBreakdown(this.testTaskStatusses(), deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getComPortPoolBreakdownWithQueryDeviceGroupDoesNotProduceSQLExceptions() {
        QueryEndDeviceGroup deviceGroup = findOrCreateQueryEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getComPortPoolBreakdown(this.testTaskStatusses(), deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionsDeviceTypeHeatMapDoesNotProduceSQLExceptions() {
        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionsDeviceTypeHeatMap();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionsDeviceTypeHeatMapWithEnumeratedDeviceGroupDoesNotProduceSQLExceptions() {
        EnumeratedEndDeviceGroup deviceGroup = findOrCreateEnumeratedEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionsDeviceTypeHeatMap(deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionsDeviceTypeHeatMapWithQueryDeviceGroupDoesNotProduceSQLExceptions() {
        QueryEndDeviceGroup deviceGroup = findOrCreateQueryEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionsDeviceTypeHeatMap(deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTypeBreakdownDoesNotProduceSQLExceptions() {
        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTypeBreakdown(this.testTaskStatusses());

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTypeBreakdownWithEnumeratedDeviceGroupDoesNotProduceSQLExceptions() {
        EnumeratedEndDeviceGroup deviceGroup = findOrCreateEnumeratedEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTypeBreakdown(this.testTaskStatusses(), deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTypeBreakdownWithQueryDeviceGroupDoesNotProduceSQLExceptions() {
        QueryEndDeviceGroup deviceGroup = findOrCreateQueryEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTypeBreakdown(this.testTaskStatusses(), deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionsComPortPoolHeatMapDoesNotProduceSQLExceptions() {
        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionsComPortPoolHeatMap();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionsComPortPoolHeatMapWithEnumeratedDeviceGroupDoesNotProduceSQLExceptions() {
        EnumeratedEndDeviceGroup deviceGroup = findOrCreateEnumeratedEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionsComPortPoolHeatMap(deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionsComPortPoolHeatMapWithQueryDeviceGroupDoesNotProduceSQLExceptions() {
        QueryEndDeviceGroup deviceGroup = findOrCreateQueryEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionsComPortPoolHeatMap(deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTaskBreakdownsDoesNotProduceSQLExceptions() {
        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTaskBreakdowns();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTaskBreakdownsWithEnumeratedDeviceGroupDoesNotProduceSQLExceptions() {
        EnumeratedEndDeviceGroup deviceGroup = findOrCreateEnumeratedEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTaskBreakdowns(deviceGroup);

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTaskBreakdownsWithQueryDeviceGroupDoesNotProduceSQLExceptions() {
        QueryEndDeviceGroup deviceGroup = findOrCreateQueryEndDeviceGroup();

        // Business method
        oracleIntegrationPersistence.getConnectionTaskReportService().getConnectionTaskBreakdowns(deviceGroup);

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
        }
        else {
            return oracleIntegrationPersistence
                    .getMeteringGroupsService()
                    .createQueryEndDeviceGroup(createSearchablePropertyValue("deviceConfiguration.deviceType.name", Collections.singletonList("myType")))
                        .setMRID("dynamic")
                        .setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_END_DEVICE_QUERY_PROVIDER)
                        .create();
        }
    }

    private SearchablePropertyValue createSearchablePropertyValue(String searchableProperty, List<String> values) {
        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.propertyName = searchableProperty;
        valueBean.operator = SearchablePropertyOperator.EQUAL;
        valueBean.values = values;
        return new SearchablePropertyValue(null, valueBean);
    }

}
