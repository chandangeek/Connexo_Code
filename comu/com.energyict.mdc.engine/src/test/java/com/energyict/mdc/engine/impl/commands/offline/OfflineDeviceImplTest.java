/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.config.AllowedCalendar;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.LoadProfileSpec;
import com.energyict.mdc.common.device.config.NumericalRegisterSpec;
import com.energyict.mdc.common.device.config.RegisterSpec;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.RegisterGroup;
import com.energyict.mdc.common.masterdata.RegisterType;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceMessageFactory;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.DeviceMessageTestCategories;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.DeviceMessageTestSpec;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageSpecificationServiceImpl;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnexoDeviceMessageSpecAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConverterImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLNlsServiceImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLPropertySpecServiceImpl;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.obis.ObisCode;
import com.google.common.collect.ImmutableSet;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link OfflineDeviceImpl} component.
 *
 * @author gna
 * @since 17/04/12 - 9:47
 */
@RunWith(MockitoJUnitRunner.class)
public class OfflineDeviceImplTest {

    private static final String cp_prop1 = "CP_Prop1";
    private static final String cp_prop2 = "CP_Prop2";
    private static final String cp_prop3 = "CP_Prop3";
    private static final String cp_propValue1 = "CP_PropValue1";
    private static final String cp_propValue2 = "CP_PropValue2";
    private static final String cp_propValue3 = "CP_PropValue3";

    private static final String device_prop1 = "DEVICE_Prop1";
    private static final String device_prop2 = "DEVICE_Prop2";
    private static final String device_prop3 = "DEVICE_Prop3";
    private static final String DEVICE_TIMEZONE_PROPERTY = "deviceTimeZone";
    private static final String device_propValue1 = "DEVICE_PropValue1";
    private static final String device_propValue2 = "DEVICE_PropValue2";
    private static final String device_propValue3 = "DEVICE_PropValue3";

    private static final String mRID = "mRID";
    private static final String meterSerialNumber = "MeterSerialNumber";
    private static final int DEVICE_ID = 93;
    private static final TimeZone deviceProtocolPluggableClassTimeZone = TimeZone.getTimeZone("GMT");
    private static final TimeZone deviceTimeZone = TimeZone.getTimeZone("GMT+05");

    private static final String CALENDAR_NAME = "Whatever";

    @Mock
    DataModel dataModel;
    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceMessageFactory deviceMessageFactory;
    @Mock
    private DeviceMessageCategory deviceMessageCategory;
    @Mock
    private TopologyService topologyService;
    @Mock
    private OfflineDeviceImpl.ServiceProvider offlineDeviceServiceProvider;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private FirmwareService firmwareService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private EventService eventService;
    @Mock
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private DeviceMessageService deviceMessageService;
    @Mock
    private TransactionService transactionService;

    private static LoadProfile getNewMockedLoadProfile(final ObisCode obisCode, Device device) {
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getObisCode()).thenReturn(obisCode);
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
        when(loadProfileSpec.getInterval()).thenReturn(TimeDuration.minutes(15));
        LoadProfile loadProfile = mock(LoadProfile.class);
        when(loadProfile.getLastReading()).thenReturn(null);
        when(loadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfile.getLoadProfileTypeObisCode()).thenReturn(obisCode);
        when(loadProfile.getDevice()).thenReturn(device);
        when(loadProfile.getDeviceObisCode()).thenReturn(obisCode);
        return loadProfile;
    }

    private TypedProperties getDeviceProtocolProperties() {
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(cp_prop1, cp_propValue1);
        properties.setProperty(cp_prop2, cp_propValue2);
        properties.setProperty(cp_prop3, cp_propValue3);
        properties.setProperty(DEVICE_TIMEZONE_PROPERTY, deviceTimeZone);
        return properties;
    }

    private DeviceProtocolPluggableClass createMockDeviceProtocolPluggableClass() {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getProperties(anyList())).thenReturn(getDeviceProtocolPluggableClassProperties());
        return deviceProtocolPluggableClass;
    }

    private TypedProperties getDeviceProperties() {
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(device_prop1, device_propValue1);
        properties.setProperty(device_prop2, device_propValue2);
        properties.setProperty(device_prop3, device_propValue3);
        return properties;
    }

    private TypedProperties getDeviceProtocolPluggableClassProperties() {
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(DEVICE_TIMEZONE_PROPERTY, deviceProtocolPluggableClassTimeZone);
        return properties;
    }

    private Device createMockedDevice(long deviceId, String meterSerialNumber) {
        return createMockedDevice(deviceId, meterSerialNumber, meterSerialNumber, mock(DeviceProtocol.class));
    }

    private Device createMockedDevice(long deviceId, String meterSerialNumber, String mRID) {
        return createMockedDevice(deviceId, meterSerialNumber, mRID, mock(DeviceProtocol.class));
    }

    private Device createMockedDevice(long deviceId, String meterSerialNumber, final DeviceProtocol deviceProtocol) {
        return createMockedDevice(deviceId, meterSerialNumber, meterSerialNumber, deviceProtocol);
    }

    private Device createMockedDevice(long deviceId, String mRID, String meterSerialNumber, final DeviceProtocol deviceProtocol) {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        Device device = mock(Device.class);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getDeviceProtocolProperties()).thenReturn(getDeviceProtocolProperties());
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = createMockDeviceProtocolPluggableClass();
        when(device.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(device.getId()).thenReturn(deviceId);
        when(device.getmRID()).thenReturn(mRID);
        when(device.getSerialNumber()).thenReturn(meterSerialNumber);
        when(device.getLocation()).thenReturn(Optional.empty());
        when(device.getUsagePoint()).thenReturn(Optional.empty());
        when(deviceProtocol.prepareMessageContext(eq(device), any(OfflineDevice.class), any(DeviceMessage.class))).thenReturn(Optional.empty());
        return device;
    }

    private Device createMockedDevice() {
        return createMockedDevice(DEVICE_ID, mRID, meterSerialNumber);
    }

    @Before
    public void initBefore() {
        when(offlineDeviceServiceProvider.findProtocolCacheByDevice(any(Device.class))).thenReturn(Optional.empty());
        when(offlineDeviceServiceProvider.thesaurus()).thenReturn(thesaurus);
        when(offlineDeviceServiceProvider.topologyService()).thenReturn(topologyService);
        when(offlineDeviceServiceProvider.identificationService()).thenReturn(identificationService);
        when(offlineDeviceServiceProvider.firmwareService()).thenReturn(firmwareService);
        when(offlineDeviceServiceProvider.deviceConfigurationService()).thenReturn(deviceConfigurationService);
        when(offlineDeviceServiceProvider.eventService()).thenReturn(eventService);
        when(offlineDeviceServiceProvider.protocolPluggableService()).thenReturn(protocolPluggableService);
        when(offlineDeviceServiceProvider.deviceMessageSpecificationService()).thenReturn(deviceMessageSpecificationService);
        when(offlineDeviceServiceProvider.deviceMessageService()).thenReturn(deviceMessageService);
        when(offlineDeviceServiceProvider.transactionService()).thenReturn(transactionService);
        when(this.topologyService.getPhysicalGateway(any(Device.class))).thenReturn(Optional.empty());
        when(this.topologyService.getPhysicalGateway(any(Device.class), any(Instant.class))).thenReturn(Optional.empty());
        when(this.topologyService.findPhysicalConnectedDevices(any(Device.class))).thenReturn(Collections.emptyList());
        when(this.ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        when(this.firmwareService.findFirmwareManagementOptions(any(DeviceType.class))).thenReturn(Optional.empty());
        when(this.deviceConfigurationService.findTimeOfUseOptions(any(DeviceType.class))).thenReturn(Optional.empty());
        when(this.deviceMessageSpecificationService.getFirmwareCategory()).thenReturn(deviceMessageCategory);
        when(deviceMessageCategory.getId()).thenReturn(DeviceMessageTestCategories.FIRST_TEST_CATEGORY.getId());
        when(transactionService.isInTransaction()).thenReturn(true);
        Services.tariffCalendarExtractor(mock(TariffCalendarExtractor.class, RETURNS_DEEP_STUBS));
        Services.keyAccessorTypeExtractor(mock(KeyAccessorTypeExtractor.class, RETURNS_DEEP_STUBS));
    }

    private int getTotalSizeOfProperties() {
        return getDeviceProperties().size() + getDeviceProtocolProperties().size() + getDeviceProtocolPluggableClassProperties().size();
    }

    @Before
    public void setupDeviceMessageService() {
        when(this.thesaurus.getString(anyString(), anyString())).thenReturn("Translation not supported in unit testing");
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        PropertySpecService propertySpecService =
                new UPLPropertySpecServiceImpl(
                        new PropertySpecServiceImpl(
                                new com.elster.jupiter.properties.impl.PropertySpecServiceImpl(),
                                mock(DataVaultService.class),
                                this.ormService),
                        this.nlsService);
        this.deviceMessageSpecificationService = new DeviceMessageSpecificationServiceImpl(new ConverterImpl(), new UPLNlsServiceImpl(this.nlsService), propertySpecService);
    }

    @Test
    public void goOfflineTest() {
        Device device = createMockedDevice();
        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        assertEquals("The SerialNumber should match", meterSerialNumber, offlineRtu.getSerialNumber());
        assertEquals("The mRID should match", mRID, offlineRtu.getmRID());
        assertEquals("The ID should match", DEVICE_ID, offlineRtu.getId());
        assertNotNull("The DeviceProtocol PluggableClass should not be null", offlineRtu.getDeviceProtocolPluggableClass());
        verify(topologyService, times(1)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void propertiesTest() {
        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(createMockedDevice(), DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        offlineRtu.addProperties(getDeviceProperties());
        offlineRtu.addProperties(getDeviceProtocolProperties());
        assertEquals("Size should be equal to seven", getTotalSizeOfProperties(), offlineRtu.getAllProperties().localSize());
        assertEquals(cp_propValue1, offlineRtu.getAllProperties().getProperty(cp_prop1));
        assertEquals(cp_propValue2, offlineRtu.getAllProperties().getProperty(cp_prop2));
        assertEquals(cp_propValue3, offlineRtu.getAllProperties().getProperty(cp_prop3));
        assertEquals(device_propValue1, offlineRtu.getAllProperties().getProperty(device_prop1));
        assertEquals(device_propValue2, offlineRtu.getAllProperties().getProperty(device_prop2));
        assertEquals(device_propValue3, offlineRtu.getAllProperties().getProperty(device_prop3));
        assertEquals(device_propValue3, offlineRtu.getAllProperties().getProperty(device_prop3));
        assertEquals(offlineRtu.getSerialNumber(), offlineRtu.getAllProperties().getProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName()));
        verify(topologyService, times(1)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void convertToOfflineRtuTest() {
        DeviceType slaveRtuType = mock(DeviceType.class);
        when(slaveRtuType.isLogicalSlave()).thenReturn(true);

        Device rtu = createMockedDevice();

        Device slave1 = createMockedDevice(123, "slave1");
        when(slave1.getDeviceType()).thenReturn(slaveRtuType);
        Device slaveFromSlave1 = createMockedDevice(789, "slaveFromSlave1");
        when(slaveFromSlave1.getDeviceType()).thenReturn(slaveRtuType);
        when(this.topologyService.findPhysicalConnectedDevices(slave1)).thenReturn(Collections.singletonList(slaveFromSlave1));

        Device slave2 = createMockedDevice(456, "slave2");
        when(slave2.getDeviceType()).thenReturn(slaveRtuType);
        when(this.topologyService.findPhysicalConnectedDevices(rtu)).thenReturn(Arrays.asList(slave1, slave2));

        // Business method
        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(rtu, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        // Asserts
        assertNotNull(offlineRtu.getAllSlaveDevices());
        assertEquals("Expected three slave devices", 3, offlineRtu.getAllSlaveDevices().size());
        verify(topologyService, times(4)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void getAllLoadProfilesIncludingDownStreamsTest() {
        DeviceType slaveRtuType = mock(DeviceType.class);
        when(slaveRtuType.isLogicalSlave()).thenReturn(true);

        Device device = createMockedDevice();

        Device slave1 = createMockedDevice(123, "slave1");
        when(slave1.getDeviceType()).thenReturn(slaveRtuType);
        Device slaveFromSlave1 = createMockedDevice(789, "slaveFromSlave1");
        when(slaveFromSlave1.getDeviceType()).thenReturn(slaveRtuType);
        when(this.topologyService.findPhysicalConnectedDevices(slave1)).thenReturn(Collections.singletonList(slaveFromSlave1));

        Device slave2 = createMockedDevice(456, "slave2");
        when(slave2.getDeviceType()).thenReturn(slaveRtuType);
        when(this.topologyService.findPhysicalConnectedDevices(device)).thenReturn(Arrays.asList(slave1, slave2));

        ObisCode obisCode1 = ObisCode.fromString("1.0.99.1.0.255");
        ObisCode obisCode2 = ObisCode.fromString("1.0.99.2.0.255");
        ObisCode obisCode3 = ObisCode.fromString("1.0.99.3.0.255");
        ObisCode obisCode4 = ObisCode.fromString("1.0.99.4.0.255");
        ObisCode obisCodex = ObisCode.fromString("1.x.24.1.0.255");

        LoadProfile loadProfile1 = getNewMockedLoadProfile(obisCode1, device);
        LoadProfile loadProfile2 = getNewMockedLoadProfile(obisCode2, device);
        LoadProfile loadProfile3 = getNewMockedLoadProfile(obisCode3, device);
        LoadProfile loadProfile4 = getNewMockedLoadProfile(obisCode4, device);
        LoadProfile loadProfilex = getNewMockedLoadProfile(obisCodex, device);

        when(device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2));
        when(slave1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile3, loadProfilex));
        when(slave2.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile4, loadProfile2));
        when(slaveFromSlave1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfilex));

        OfflineDeviceImpl offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        // Asserts
        assertNotNull(offlineDevice.getMasterOfflineLoadProfiles());
        assertEquals("Expected two loadProfiles on the master", 2, offlineDevice.getMasterOfflineLoadProfiles().size());
        assertNotNull(offlineDevice.getAllOfflineLoadProfiles());
        assertEquals("Expected 9 loadProfiles in total", 9, offlineDevice.getAllOfflineLoadProfiles().size());
        verify(topologyService, times(4)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void getAllRegistersTest() {
        Device device = createMockedDevice();
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("getAllRegistersTest");
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getReadingType()).thenReturn(readingType);
        Register register1 = createMockedRegister(createMockedRegisterSpec(registerType), device);
        Register register2 = createMockedRegister(createMockedRegisterSpec(registerType), device);
        when(device.getRegisters()).thenReturn(Arrays.asList(register1, register2));

        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        // asserts
        assertNotNull(offlineRtu.getAllOfflineRegisters());
        assertEquals("Should have gotten 2 registers", 2, offlineRtu.getAllOfflineRegisters().size());
        verify(topologyService, times(1)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void getAllRegistersIncludingSlavesTest() {
        Device device = createMockedDevice();
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("getAllRegistersIncludingSlavesTest");
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getReadingType()).thenReturn(readingType);
        Register register1 = createMockedRegister(createMockedRegisterSpec(registerType), device);
        Register register2 = createMockedRegister(createMockedRegisterSpec(registerType), device);
        when(device.getRegisters()).thenReturn(Arrays.asList(register1, register2));

        DeviceType slaveRtuType = mock(DeviceType.class);
        when(slaveRtuType.isLogicalSlave()).thenReturn(true);
        Device slaveWithNeedProxy = createMockedDevice(132, "654654");
        when(slaveWithNeedProxy.getDeviceType()).thenReturn(slaveRtuType);
        Register registerSlave1 = createMockedRegister(createMockedRegisterSpec(registerType), device);
        when(slaveWithNeedProxy.getRegisters()).thenReturn(Collections.singletonList(registerSlave1));
        Device slaveWithoutNeedProxy = createMockedDevice(133, "65465415");
        DeviceType notASlaveRtuType = mock(DeviceType.class);
        Register registerSlave2 = createMockedRegister(createMockedRegisterSpec(registerType), device);
        when(slaveWithoutNeedProxy.getDeviceType()).thenReturn(notASlaveRtuType);
        when(slaveWithoutNeedProxy.getRegisters()).thenReturn(Collections.singletonList(registerSlave2));
        when(this.topologyService.findPhysicalConnectedDevices(device)).thenReturn(Arrays.asList(slaveWithNeedProxy, slaveWithoutNeedProxy));

        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        // asserts
        assertNotNull(offlineRtu.getAllOfflineRegisters());
        assertEquals("Should have gotten 3 registers", 3, offlineRtu.getAllOfflineRegisters().size());
        verify(topologyService, times(3)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void getRegistersForRegisterGroup() {
        final long rtuRegisterGroupId = 135143654;
        RegisterGroup rtuRegisterGroup = mock(RegisterGroup.class);
        when(rtuRegisterGroup.getId()).thenReturn(rtuRegisterGroupId);
        Device device = createMockedDevice(DEVICE_ID, "getRegistersForRegisterGroup", meterSerialNumber, mock(DeviceProtocol.class));
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(OfflineDeviceImplTest.class.getSimpleName());
        RegisterType registerType1 = mock(RegisterType.class);
        when(registerType1.getReadingType()).thenReturn(readingType);
        when(registerType1.getRegisterGroups()).thenReturn(Collections.singletonList(rtuRegisterGroup));
        RegisterSpec registerSpec1 = createMockedRegisterSpec(registerType1);
        Register register1 = createMockedRegister(registerSpec1, device);
        when(register1.getRegisterSpec().getRegisterType().getRegisterGroups()).thenReturn(Collections.singletonList(rtuRegisterGroup));
        OfflineRegister offlineRegister1 = mock(OfflineRegister.class);
        when(offlineRegister1.inGroup(rtuRegisterGroupId)).thenReturn(true);
        when(offlineRegister1.inAtLeastOneGroup(Collections.singletonList(rtuRegisterGroupId))).thenReturn(true);
        when(offlineRegister1.getDeviceMRID()).thenReturn("getRegistersForRegisterGroup");

        RegisterType registerType2 = mock(RegisterType.class);
        when(registerType2.getReadingType()).thenReturn(readingType);
        when(registerType2.getRegisterGroups()).thenReturn(Collections.emptyList());
        RegisterSpec registerSpec2 = createMockedRegisterSpec(registerType2);
        Register register2 = createMockedRegister(registerSpec2, device);
        when(device.getRegisters()).thenReturn(Arrays.asList(register1, register2));
        OfflineRegister offlineRegister2 = mock(OfflineRegister.class);
        when(offlineRegister2.getDeviceMRID()).thenReturn("getRegistersForRegisterGroup");
        when(offlineRegister2.inGroup(rtuRegisterGroupId)).thenReturn(false);
        when(offlineRegister2.inAtLeastOneGroup(Collections.singletonList(rtuRegisterGroupId))).thenReturn(false);

        OfflineDeviceImpl offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        when(offlineDevice.getAllOfflineRegisters()).thenReturn(Arrays.asList(offlineRegister1, offlineRegister2));

        // asserts
        assertThat(offlineDevice.getRegistersForRegisterGroupAndMRID(Collections.singletonList(rtuRegisterGroupId), "getRegistersForRegisterGroup")).hasSize(1);
        verify(topologyService, times(1)).findPhysicalConnectedDevices((Device) any());
    }

    private RegisterSpec createMockedRegisterSpec(RegisterType registerType) {
        RegisterSpec registerSpec = mock(RegisterSpec.class, withSettings().extraInterfaces(NumericalRegisterSpec.class));
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(((NumericalRegisterSpec) registerSpec).getOverflowValue()).thenReturn(Optional.empty());
        return registerSpec;
    }

    private Register createMockedRegister(RegisterSpec registerSpec, Device device) {
        Register register = mock(Register.class);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
        when(register.getDevice()).thenReturn(device);
        return register;
    }

    @Test
    public void pendingDeviceMessagesTest() {
        Device device = createMockedDevice();
        Calendar calendar = mock(Calendar.class);
        AllowedCalendar allowedCalendar = mock(AllowedCalendar.class);
        when(allowedCalendar.getCalendar()).thenReturn(Optional.of(calendar));
        when(allowedCalendar.getName()).thenReturn(CALENDAR_NAME);
        when(calendar.getName()).thenReturn(CALENDAR_NAME);
        when(device.getDeviceType().getAllowedCalendars()).thenReturn(Collections.singletonList(allowedCalendar));

        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec2 = this.getDeviceMessageSpec(DeviceMessageId.CLOCK_SET_TIME.dbValue());
        when(deviceMessage2.getSpecification()).thenReturn(mockedDeviceMessageSpec2);
        when(deviceMessage2.getDevice()).thenReturn(device);
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        assertNotNull(offlineDevice.getAllPendingDeviceMessages());
        assertEquals("Size of the pending list should be zero", 0, offlineDevice.getAllPendingDeviceMessages().size());

        when(device.getMessagesByState(DeviceMessageStatus.PENDING)).thenReturn(Collections.singletonList(deviceMessage2));
        when(deviceMessageService.findAndLockPendingMessagesForDevices(Collections.singleton(device))).thenReturn(Collections.singletonList(deviceMessage2));
        when(protocolPluggableService.adapt(mockedDeviceMessageSpec2)).thenReturn(ConnexoDeviceMessageSpecAdapter.adaptTo(mockedDeviceMessageSpec2));

        offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        assertNotNull(offlineDevice.getAllPendingDeviceMessages());
        assertEquals("Size of the pending list should be one", 1, offlineDevice.getAllPendingDeviceMessages().size());
        assertEquals("Size of the sent list should be 0", 0, offlineDevice.getAllSentDeviceMessages().size());
        verify(topologyService, times(2)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void pendingInvalidDeviceMessagesTest() {
        Calendar calendar = mock(Calendar.class);
        when(calendar.getName()).thenReturn(CALENDAR_NAME);
        Device device = createMockedDevice();
        when(device.getDeviceType().getAllowedCalendars()).thenReturn(Collections.emptyList());
        DeviceMessageSpec sendCalendarSpec = this.getDeviceMessageSpec(DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_SEND.dbValue());
        List<PropertySpec> propertySpecs = sendCalendarSpec.getPropertySpecs();
        DeviceMessage sendCalendar = mock(DeviceMessage.class);
        when(sendCalendar.getDeviceMessageId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_SEND);
        when(sendCalendar.getMessageId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_SEND.dbValue());
        when(sendCalendar.getSpecification()).thenReturn(sendCalendarSpec);

        DeviceMessageAttribute calendarNameAttribute = mock(DeviceMessageAttribute.class);
        when(calendarNameAttribute.getDeviceMessage()).thenReturn(sendCalendar);
        String defaultFormat = "Name";
        when(calendarNameAttribute.getName()).thenReturn(defaultFormat);
        when(calendarNameAttribute.getValue()).thenReturn("pendingInvalidDeviceMessagesTest");
        PropertySpec firstPropertySpec = propertySpecs.get(0);
        when(calendarNameAttribute.getSpecification()).thenReturn(firstPropertySpec);
        DeviceMessageAttribute calendarAttribute = mock(DeviceMessageAttribute.class);
        when(calendarAttribute.getDeviceMessage()).thenReturn(sendCalendar);
        when(calendarAttribute.getName()).thenReturn("Activity calendar");
        when(calendarAttribute.getValue()).thenReturn(calendar);
        when(calendarAttribute.getSpecification()).thenReturn(propertySpecs.get(1));
        doReturn(Arrays.asList(calendarNameAttribute, calendarAttribute)).when(sendCalendar).getAttributes();
        when(sendCalendar.getDevice()).thenReturn(device);
        when(device.getMessagesByState(DeviceMessageStatus.PENDING)).thenReturn(Collections.singletonList(sendCalendar));
        when(deviceMessageService.findAndLockPendingMessagesForDevices(Collections.singleton(device))).thenReturn(Collections.singletonList(sendCalendar));
        when(protocolPluggableService.adapt(sendCalendarSpec)).thenReturn(ConnexoDeviceMessageSpecAdapter.adaptTo(sendCalendarSpec));

        // Business method
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        // Asserts
        assertEquals("Size of the pending list should be zero", 0, offlineDevice.getAllPendingDeviceMessages().size());
        assertEquals("Size of the pending list should be one", 1, offlineDevice.getAllInvalidPendingDeviceMessages().size());
        assertEquals("Size of the sent list should be 0", 0, offlineDevice.getAllSentDeviceMessages().size());
        verify(topologyService, times(1)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void getAllPendingDeviceMessagesIncludingDownStreamsTest() {
        Device device = createMockedDevice();
        DeviceMessage deviceMessage1 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec1 = this.getDeviceMessageSpec(DeviceMessageId.CLOCK_SET_TIME.dbValue());
        when(protocolPluggableService.adapt(mockedDeviceMessageSpec1)).thenReturn(ConnexoDeviceMessageSpecAdapter.adaptTo(mockedDeviceMessageSpec1));
        when(deviceMessage1.getSpecification()).thenReturn(mockedDeviceMessageSpec1);
        when(deviceMessage1.getDevice()).thenReturn(device);
        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec2 = this.getDeviceMessageSpec(DeviceMessageTestSpec.TEST_SPEC_WITH_SIMPLE_SPECS.getId());
        when(protocolPluggableService.adapt(mockedDeviceMessageSpec2)).thenReturn(ConnexoDeviceMessageSpecAdapter.adaptTo(mockedDeviceMessageSpec2));
        when(deviceMessage2.getSpecification()).thenReturn(mockedDeviceMessageSpec2);
        when(deviceMessage2.getDevice()).thenReturn(device);
        when(device.getMessagesByState(DeviceMessageStatus.PENDING)).thenReturn(Arrays.asList(deviceMessage1, deviceMessage2));
        when(device.getDeviceConfiguration()).thenReturn(mock(DeviceConfiguration.class));

        DeviceType slaveRtuType = mock(DeviceType.class);
        when(slaveRtuType.isLogicalSlave()).thenReturn(true);
        DeviceProtocol slaveDeviceProtocol = mock(DeviceProtocol.class);
        Device slaveWithNeedProxy = createMockedDevice(132, "654654", slaveDeviceProtocol);
        when(slaveWithNeedProxy.getDeviceType()).thenReturn(slaveRtuType);
        DeviceMessage slaveDeviceMessage1 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedSlaveDeviceMessageSpec1 = this.getDeviceMessageSpec(DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS.getId());
        when(protocolPluggableService.adapt(mockedSlaveDeviceMessageSpec1)).thenReturn(ConnexoDeviceMessageSpecAdapter.adaptTo(mockedSlaveDeviceMessageSpec1));
        when(slaveDeviceMessage1.getSpecification()).thenReturn(mockedSlaveDeviceMessageSpec1);
        when(slaveDeviceMessage1.getDevice()).thenReturn(slaveWithNeedProxy);
        when(slaveWithNeedProxy.getMessagesByState(DeviceMessageStatus.PENDING)).thenReturn(Collections.singletonList(slaveDeviceMessage1));

        DeviceType notASlaveRtuType = mock(DeviceType.class);
        Device slaveWithoutNeedProxy = createMockedDevice(133, "65465415", slaveDeviceProtocol);
        when(slaveWithoutNeedProxy.getDeviceType()).thenReturn(notASlaveRtuType);
        DeviceMessage slaveDeviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedSlaveDeviceMessageSpec2 = this.getDeviceMessageSpec(DeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS.getId());
        when(protocolPluggableService.adapt(mockedSlaveDeviceMessageSpec2)).thenReturn(ConnexoDeviceMessageSpecAdapter.adaptTo(mockedSlaveDeviceMessageSpec2));
        when(slaveDeviceMessage2.getSpecification()).thenReturn(mockedSlaveDeviceMessageSpec2);
        when(slaveDeviceMessage2.getDevice()).thenReturn(slaveWithoutNeedProxy);
        when(slaveWithoutNeedProxy.getMessagesByState(DeviceMessageStatus.PENDING)).thenReturn(Collections.singletonList(slaveDeviceMessage2));

        when(deviceMessageFactory.findByDeviceAndState(device, DeviceMessageStatus.PENDING)).thenReturn(Arrays.asList(deviceMessage1, deviceMessage2));
        when(deviceMessageFactory.findByDeviceAndState(slaveWithNeedProxy, DeviceMessageStatus.PENDING)).thenReturn(Collections.singletonList(slaveDeviceMessage1));
        when(deviceMessageFactory.findByDeviceAndState(slaveWithoutNeedProxy, DeviceMessageStatus.PENDING)).thenReturn(Collections.singletonList(slaveDeviceMessage2));
        when(topologyService.findPhysicalConnectedDevices(device)).thenReturn(Arrays.asList(slaveWithNeedProxy, slaveWithoutNeedProxy));
        when(deviceMessageService.findAndLockPendingMessagesForDevices(ImmutableSet.of(device, slaveWithNeedProxy)))
                .thenReturn(Arrays.asList(deviceMessage1, deviceMessage2, slaveDeviceMessage1));

        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        assertNotNull(offlineDevice.getAllPendingDeviceMessages());
        assertEquals("Size of the pending list should be three", 3, offlineDevice.getAllPendingDeviceMessages().size());
        assertEquals("Size of the sent list should be 0", 0, offlineDevice.getAllSentDeviceMessages().size());

        List<OfflineDeviceMessage> offlineDeviceMessages = offlineDevice.getAllPendingDeviceMessages()
                .stream()
                .filter(offlineDeviceMessage -> offlineDeviceMessage.getDeviceId() == 132)
                .collect(Collectors.toList());
        assertEquals("Expecting one slave device message", 1, offlineDeviceMessages.size());
        assertEquals("Expecting the serialnumber of the slave", "654654", offlineDeviceMessages.get(0).getDeviceSerialNumber());
        assertEquals("Expecting the DeviceProtocol of the slave", slaveDeviceProtocol, offlineDeviceMessages.get(0).getDeviceProtocol());
        verify(topologyService, times(3)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void sentDeviceMessagesTest() {
        Device device = createMockedDevice();
        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec2 = this.getDeviceMessageSpec(DeviceMessageId.CLOCK_SET_TIME);

        when(deviceMessage2.getSpecification()).thenReturn(mockedDeviceMessageSpec2);
        when(deviceMessage2.getDevice()).thenReturn(device);
        when(protocolPluggableService.adapt(mockedDeviceMessageSpec2)).thenReturn(ConnexoDeviceMessageSpecAdapter.adaptTo(mockedDeviceMessageSpec2));

        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        assertNotNull(offlineDevice.getAllSentDeviceMessages());
        assertEquals("Size of the sent list should be zero", 0, offlineDevice.getAllSentDeviceMessages().size());

        when(device.getMessagesByState(DeviceMessageStatus.SENT)).thenReturn(Collections.singletonList(deviceMessage2));

        offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        assertNotNull(offlineDevice.getAllSentDeviceMessages());
        assertEquals("Size of the sent list should be one", 1, offlineDevice.getAllSentDeviceMessages().size());
        assertEquals("Size of the pending list should be 0", 0, offlineDevice.getAllPendingDeviceMessages().size());
        verify(topologyService, times(2)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void getAllSentDeviceMessagesIncludingDownStreamsTest() {
        Device device = createMockedDevice();
        DeviceMessage deviceMessage1 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec1 = this.getDeviceMessageSpec(DeviceMessageId.CLOCK_SET_TIME.dbValue());
        when(protocolPluggableService.adapt(mockedDeviceMessageSpec1)).thenReturn(ConnexoDeviceMessageSpecAdapter.adaptTo(mockedDeviceMessageSpec1));
        when(deviceMessage1.getSpecification()).thenReturn(mockedDeviceMessageSpec1);
        when(deviceMessage1.getDevice()).thenReturn(device);
        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec2 = this.getDeviceMessageSpec(DeviceMessageTestSpec.TEST_SPEC_WITH_SIMPLE_SPECS.getId());
        when(protocolPluggableService.adapt(mockedDeviceMessageSpec2)).thenReturn(ConnexoDeviceMessageSpecAdapter.adaptTo(mockedDeviceMessageSpec2));
        when(deviceMessage2.getSpecification()).thenReturn(mockedDeviceMessageSpec2);
        when(deviceMessage2.getDevice()).thenReturn(device);
        when(device.getMessagesByState(DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(deviceMessage1, deviceMessage2));

        DeviceType slaveRtuType = mock(DeviceType.class);
        when(slaveRtuType.isLogicalSlave()).thenReturn(true);
        DeviceProtocol slaveDeviceProtocol = mock(DeviceProtocol.class);
        Device slaveWithNeedProxy = createMockedDevice(132, "654654", slaveDeviceProtocol);
        when(slaveWithNeedProxy.getDeviceType()).thenReturn(slaveRtuType);
        DeviceMessage slaveDeviceMessage1 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedSlaveDeviceMessageSpec1 = this.getDeviceMessageSpec(DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS.getId());
        when(protocolPluggableService.adapt(mockedSlaveDeviceMessageSpec1)).thenReturn(ConnexoDeviceMessageSpecAdapter.adaptTo(mockedSlaveDeviceMessageSpec1));
        when(slaveDeviceMessage1.getSpecification()).thenReturn(mockedSlaveDeviceMessageSpec1);
        when(slaveDeviceMessage1.getDevice()).thenReturn(slaveWithNeedProxy);
        when(slaveWithNeedProxy.getMessagesByState(DeviceMessageStatus.SENT)).thenReturn(Collections.singletonList(slaveDeviceMessage1));

        DeviceType notASlaveRtuType = mock(DeviceType.class);
        Device slaveWithoutNeedProxy = createMockedDevice(133, "65465415", slaveDeviceProtocol);
        when(slaveWithoutNeedProxy.getDeviceType()).thenReturn(notASlaveRtuType);
        DeviceMessage slaveDeviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedSlaveDeviceMessageSpec2 = this.getDeviceMessageSpec(DeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS.getId());
        when(protocolPluggableService.adapt(mockedSlaveDeviceMessageSpec2)).thenReturn(ConnexoDeviceMessageSpecAdapter.adaptTo(mockedSlaveDeviceMessageSpec2));
        when(slaveDeviceMessage2.getSpecification()).thenReturn(mockedSlaveDeviceMessageSpec2);
        when(slaveDeviceMessage2.getDevice()).thenReturn(slaveWithoutNeedProxy);
        when(slaveWithoutNeedProxy.getMessagesByState(DeviceMessageStatus.SENT)).thenReturn(Collections.singletonList(slaveDeviceMessage2));

        when(deviceMessageFactory.findByDeviceAndState(device, DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(deviceMessage1, deviceMessage2));
        when(deviceMessageFactory.findByDeviceAndState(slaveWithNeedProxy, DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(slaveDeviceMessage1));
        when(deviceMessageFactory.findByDeviceAndState(slaveWithoutNeedProxy, DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(slaveDeviceMessage2));
        when(this.topologyService.findPhysicalConnectedDevices(device)).thenReturn(Arrays.asList(slaveWithNeedProxy, slaveWithoutNeedProxy));
        when(protocolPluggableService.adapt(mockedDeviceMessageSpec1)).thenReturn(ConnexoDeviceMessageSpecAdapter.adaptTo(mockedDeviceMessageSpec1));
        when(protocolPluggableService.adapt(mockedDeviceMessageSpec2)).thenReturn(ConnexoDeviceMessageSpecAdapter.adaptTo(mockedDeviceMessageSpec2));

        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        assertNotNull(offlineDevice.getAllPendingDeviceMessages());
        assertEquals("Size of the pending list should be three", 0, offlineDevice.getAllPendingDeviceMessages().size());
        assertEquals("Size of the sent list should be 0", 3, offlineDevice.getAllSentDeviceMessages().size());

        List<OfflineDeviceMessage> offlineDeviceMessages = offlineDevice.getAllSentDeviceMessages()
                .stream()
                .filter(offlineDeviceMessage -> offlineDeviceMessage.getDeviceId() == 132)
                .collect(Collectors.toList());
        assertEquals("Expecting one slave device message", 1, offlineDeviceMessages.size());
        assertEquals("Expecting the serialnumber of the slave", "654654", offlineDeviceMessages.get(0).getDeviceSerialNumber());
        assertEquals("Expecting the DeviceProtocol of the slave", slaveDeviceProtocol, offlineDeviceMessages.get(0).getDeviceProtocol());
        verify(topologyService, times(3)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void deviceProtocolPluggableClassNullTest() {
        Device device = createMockedDevice();
        when(device.getDeviceProtocolPluggableClass()).thenReturn(Optional.empty());
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        //asserts
        assertThat(offlineDevice.getDeviceProtocolPluggableClass()).isNull();
        verify(topologyService, times(1)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void getAllSlaveDevicesWithoutSlaveCapabilitiesTest() {
        long slaveId = 664513;
        Device master = createMockedDevice();
        Device slaveWithoutCapability = mock(Device.class);
        when(slaveWithoutCapability.getDeviceConfiguration()).thenReturn(mock(DeviceConfiguration.class));
        when(slaveWithoutCapability.getDeviceProtocolPluggableClass()).thenReturn(Optional.empty());
        when(slaveWithoutCapability.getId()).thenReturn(slaveId);
        when(slaveWithoutCapability.getLocation()).thenReturn(Optional.empty());
        when(slaveWithoutCapability.getUsagePoint()).thenReturn(Optional.empty());
        DeviceType deviceTypeSlave = mock(DeviceType.class);
        when(slaveWithoutCapability.getDeviceType()).thenReturn(deviceTypeSlave);
        when(slaveWithoutCapability.getDeviceProtocolProperties()).thenReturn(TypedProperties.empty());
        when(this.topologyService.findPhysicalConnectedDevices(master)).thenReturn(Collections.singletonList(slaveWithoutCapability));

        // business method
        OfflineDevice offlineDevice = new OfflineDeviceImpl(master, new DeviceOfflineFlags(DeviceOfflineFlags.SLAVE_DEVICES_FLAG), this.offlineDeviceServiceProvider);

        // assert
        assertThat(offlineDevice.getAllSlaveDevices()).isNotNull();
        assertThat(offlineDevice.getAllSlaveDevices()).isNotEmpty();
        assertThat(offlineDevice.getAllSlaveDevices().get(0).getId()).isEqualTo(slaveId);
        verify(topologyService, times(2)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void getAllSlaveDevicesWithSlaveCapabilitiesTest() {
        long slaveId = 664513;
        Device master = createMockedDevice();
        DeviceType deviceTypeSlave = mock(DeviceType.class);
        when(deviceTypeSlave.isLogicalSlave()).thenReturn(true);
        Device slaveWithCapability = createMockedDevice();
        when(slaveWithCapability.getId()).thenReturn(slaveId);
        when(slaveWithCapability.getDeviceType()).thenReturn(deviceTypeSlave);
        when(this.topologyService.findPhysicalConnectedDevices(master)).thenReturn(Collections.singletonList(slaveWithCapability));

        // business method
        OfflineDevice offlineDevice = new OfflineDeviceImpl(master, new DeviceOfflineFlags(DeviceOfflineFlags.SLAVE_DEVICES_FLAG), this.offlineDeviceServiceProvider);

        // assert
        assertThat(offlineDevice.getAllSlaveDevices()).isNotEmpty();
        assertThat(offlineDevice.getAllSlaveDevices().get(0).getId()).isEqualTo(slaveId);
        verify(topologyService, times(2)).findPhysicalConnectedDevices((Device) any());
    }

    @Test
    public void propertyOfDeviceOverrulesPropertiesOfDeviceProtocolTest() {
        Device device = createMockedDevice();
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, new DeviceOfflineFlags(), this.offlineDeviceServiceProvider);

        Object property = offlineDevice.getAllProperties().getProperty(DEVICE_TIMEZONE_PROPERTY);
        assertThat(property).isEqualTo(deviceTimeZone);
        verify(topologyService, times(0)).findPhysicalConnectedDevices((Device) any());
    }

    private DeviceMessageSpec getDeviceMessageSpec(DeviceMessageId deviceMessageId) {
        return getDeviceMessageSpec(deviceMessageId.dbValue());
    }

    private DeviceMessageSpec getDeviceMessageSpec(long id) {
        return this.deviceMessageSpecificationService.findMessageSpecById(id).orElseThrow(() -> new RuntimeException("Setup failure: could not find DeviceMessageSpec with id " + id));
    }

    @Test
    public void getSecurityPropertySetAttributeToKeyAccessorTypeMappingTest() {
        Device device = createMockedDevice();
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        SecurityAccessorType securityAccessorType1 = mock(SecurityAccessorType.class);
        SecurityAccessorType securityAccessorType2 = mock(SecurityAccessorType.class);
        ConfigurationSecurityProperty securityProperty1 = mock(ConfigurationSecurityProperty.class);
        ConfigurationSecurityProperty securityProperty2 = mock(ConfigurationSecurityProperty.class);
        when(securityAccessorType1.getName()).thenReturn("KeyAccessorType_1");
        when(securityAccessorType2.getName()).thenReturn("KeyAccessorType_2");
        when(securityProperty1.getName()).thenReturn("SecurityProperty1");
        when(securityProperty2.getName()).thenReturn("SecurityProperty2");
        when(securityProperty1.getSecurityAccessorType()).thenReturn(securityAccessorType1);
        when(securityProperty2.getSecurityAccessorType()).thenReturn(securityAccessorType2);

        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getName()).thenReturn("MySecuritySet");
        when(securityPropertySet.getConfigurationSecurityProperties()).thenReturn(Arrays.asList(securityProperty1, securityProperty2));
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        assertNotNull(offlineRtu.getSecurityPropertySetAttributeToKeyAccessorTypeMapping());
        assertNotNull(offlineRtu.getSecurityPropertySetAttributeToKeyAccessorTypeMapping().get("MySecuritySet"));
        assertThat(offlineRtu.getSecurityPropertySetAttributeToKeyAccessorTypeMapping().get("MySecuritySet").getProperty("SecurityProperty1")).isEqualTo("KeyAccessorType_1");
        assertThat(offlineRtu.getSecurityPropertySetAttributeToKeyAccessorTypeMapping().get("MySecuritySet").getProperty("SecurityProperty2")).isEqualTo("KeyAccessorType_2");
        verify(topologyService, times(1)).findPhysicalConnectedDevices((Device) any());
    }

}
