/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceTypePurpose;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:04)
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceIntegrationTest {

    protected static final String MRID = "MyUniqueMRID";
    protected static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    static final String DEVICE_TYPE_NAME = PersistenceIntegrationTest.class.getName() + "Type";
    static final String DATA_LOGGER_ENABLED_DEVICE_TYPE_NAME = "DataLoggerEnabledType";
    static final String DATA_LOGGER_DEVICE_TYPE = "DataLoggerType";
    static final String DEVICE_CONFIGURATION_NAME = PersistenceIntegrationTest.class.getName() + "Config";
    static final String DATA_LOGGER_ENABLED_DEVICE_CONFIGURATION_NAME = "DataLoggerConfig";
    static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    protected static Clock clock = mock(Clock.class);
    protected static InMemoryIntegrationPersistence inMemoryPersistence;
    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    protected DeviceType deviceType;
    protected DeviceType dataLoggerEnabledDeviceType;
    protected DeviceType dataLoggerSlaveDeviceType;
    protected DeviceConfiguration deviceConfiguration;
    protected DeviceConfiguration dataLoggerEnabledDeviceConfiguration;
    protected DeviceConfiguration dataLoggerSlaveDeviceConfiguration;
    protected SecurityPropertySet securityPropertySet;
    @Mock
    protected DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    List<DeviceMessageSpec> deviceMessageIds;
    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    private DeviceProtocol deviceProtocol;

    @BeforeClass
    public static void initialize() throws SQLException {
        initializeClock();
        inMemoryPersistence = new InMemoryIntegrationPersistence(clock);
        inMemoryPersistence.initializeDatabase("PersistenceIntegrationTest.mdc.device.topology", false);
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    private static void initializeClock() {
        when(clock.getZone()).thenReturn(utcTimeZone.toZoneId());
        when(clock.instant()).thenAnswer(invocationOnMock -> Instant.now());
    }

    @Before
    public void initializeMocks() {
        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);

        deviceMessageIds = new ArrayList<>();
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec0 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec0.getId()).thenReturn(DeviceMessageId.CONTACTOR_CLOSE.dbValue());
        deviceMessageIds.add(deviceMessageSpec0);
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
        when(this.deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.values()));
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.empty());
        when(clock.getZone()).thenReturn(utcTimeZone.toZoneId());
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(0L));  // Create DeviceType as early as possible to support unit tests that go back in time

        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);

        dataLoggerEnabledDeviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DATA_LOGGER_ENABLED_DEVICE_TYPE_NAME, deviceProtocolPluggableClass);

        dataLoggerSlaveDeviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DATA_LOGGER_DEVICE_TYPE, deviceProtocolPluggableClass);
        dataLoggerSlaveDeviceType.setDeviceTypePurpose(DeviceTypePurpose.DATALOGGER_SLAVE);
        dataLoggerSlaveDeviceType.update();

        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfigurationBuilder.isDirectlyAddressable(true);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceMessageIds.stream().map(DeviceMessageSpec::getId).map(DeviceMessageId::from).forEach(deviceConfiguration::createDeviceMessageEnablement);
        deviceConfiguration.activate();

        DeviceType.DeviceConfigurationBuilder dataLoggerEnabledDeviceConfigurationBuilder = dataLoggerEnabledDeviceType.newConfiguration(DATA_LOGGER_ENABLED_DEVICE_CONFIGURATION_NAME);
        dataLoggerEnabledDeviceConfigurationBuilder.isDirectlyAddressable(true);
        dataLoggerEnabledDeviceConfigurationBuilder.dataloggerEnabled(true);

        dataLoggerEnabledDeviceConfiguration = dataLoggerEnabledDeviceConfigurationBuilder.add();
        deviceMessageIds.stream().map(DeviceMessageSpec::getId).map(DeviceMessageId::from).forEach(dataLoggerEnabledDeviceConfiguration::createDeviceMessageEnablement);
        ReadingType activeEnergy = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.0.255"), Unit.get("kWh"));
        RegisterType registerType1 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(activeEnergy).get();
        ReadingType reactiveEnergy = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.2.8.0.255"), Unit.get("kWh"));
        RegisterType registerType2 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(reactiveEnergy).get();
        dataLoggerEnabledDeviceType.addRegisterType(registerType1);
        dataLoggerEnabledDeviceType.addRegisterType(registerType2);
        dataLoggerEnabledDeviceConfiguration.createNumericalRegisterSpec(registerType1).overflowValue(BigDecimal.valueOf(1000L)).numberOfFractionDigits(0).add();
        dataLoggerEnabledDeviceConfiguration.createNumericalRegisterSpec(registerType2).overflowValue(BigDecimal.valueOf(1000L)).numberOfFractionDigits(0).add();
        dataLoggerEnabledDeviceConfiguration.activate();

        DeviceType.DeviceConfigurationBuilder dataLoggerSlaveDeviceConfigurationBuilder = dataLoggerSlaveDeviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        dataLoggerSlaveDeviceConfigurationBuilder.isDirectlyAddressable(true);
        dataLoggerSlaveDeviceConfiguration = dataLoggerSlaveDeviceConfigurationBuilder.add();
        dataLoggerSlaveDeviceType.addRegisterType(registerType1);
        deviceMessageIds.stream().map(DeviceMessageSpec::getId).map(DeviceMessageId::from).forEach(dataLoggerSlaveDeviceConfiguration::createDeviceMessageEnablement);
        dataLoggerSlaveDeviceConfiguration.createNumericalRegisterSpec(registerType1).overflowValue(BigDecimal.valueOf(1000L)).numberOfFractionDigits(0).add();
        dataLoggerSlaveDeviceConfiguration.activate();

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
        this.securityPropertySet = securityPropertySetBuilder.build();
        this.resetClock();
        IssueStatus wontFix = mock(IssueStatus.class);
        when(inMemoryPersistence.getIssueService().findStatus(IssueStatus.WONT_FIX)).thenReturn(Optional.of(wontFix));
    }

    @After
    public void resetClock() {
        initializeClock();
    }

    protected Device createSimpleDevice() {
        return createSimpleDeviceWithName(this.getClass().getSimpleName());
    }

    protected Device createSimpleDeviceWithName(String name, String mRID) {
        return inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, mRID, Instant.now());
    }

    protected Device createSimpleDeviceWithName(String name) {
        return createSimpleDeviceWithName(name, "SimpleMrId");
    }

    protected Device createSlaveDevice(String name) {
        return inMemoryPersistence.getDeviceService().newDevice(dataLoggerSlaveDeviceConfiguration, name, name + "MrId", clock.instant());
    }

    protected Device createDataLoggerDevice(String name) {
        return inMemoryPersistence.getDeviceService().newDevice(dataLoggerEnabledDeviceConfiguration, name, name + "MrId", clock.instant());
    }

    protected Device getReloadedDevice(Device device) {
        return inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();
    }

}