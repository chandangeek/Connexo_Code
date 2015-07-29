package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.TaskService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FirmwareCampaignTest extends PersistenceTest {

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new InMemoryPersistence();
        inMemoryPersistence.initializeDatabase("PersistenceTest.mdc.firmware", false, false);
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        CanFindByLongPrimaryKey firmwareFinder = mock(CanFindByLongPrimaryKey.class);
        when(firmwareFinder.factoryId()).thenReturn(FactoryIds.FIRMWAREVERSION);
        when(firmwareFinder.findByPrimaryKey(anyLong())).thenReturn(Optional.of(firmwareVersion));
        when(firmwareFinder.valueDomain()).thenReturn(FirmwareVersion.class);
        when(inMemoryPersistence.getFirmwareService().finders()).thenReturn(Collections.singletonList(firmwareFinder));
        inMemoryPersistence.getInjector().getInstance(PropertySpecService.class).addFactoryProvider(inMemoryPersistence.getFirmwareService());
    }

    private DeviceType getDeviceTypeMock(){
        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        Set<DeviceMessageId> supportedMessages = new HashSet<>(Arrays.asList(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE));
        when(deviceProtocol.getSupportedMessages()).thenReturn(supportedMessages);
        return deviceType;
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "deviceType", strict = false)
    public void testInitWithNullDeviceType(){
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        firmwareService.newFirmwareCampaign(null, mock(EndDeviceGroup.class)).save();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "deviceGroup", strict = false)
    public void testInitWithNullGroup(){
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), null);
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
    }

    @Test

    public void testUpdateWithNullGroup(){
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), null);
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.addProperty("FirmwareDeviceMessage.upgrade.firwareversion", "1");
        Save.UPDATE.validate(firmwareService.getDataModel(), firmwareCampaign);
        //assert mo errors
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "name", strict = false)
    public void testNameValidation(){
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), mock(EndDeviceGroup.class));
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
    }

    @Test
    public void testDefaultStatus(){
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), mock(EndDeviceGroup.class));
        assertThat(firmwareCampaign.getStatus()).isEqualTo(FirmwareCampaignStatus.ONGOING);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "managementOption", strict = false)
    public void testManagementOptionValidation(){
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "firmwareType", strict = false)
    public void testFirmwareTypeValidation(){
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "properties.FirmwareDeviceMessage.upgrade.firwareversion", strict = true)
    public void testPropertiesValidation(){
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
    }

    @Test
    public void testSuccessfulCreation(){
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.addProperty("FirmwareDeviceMessage.upgrade.firwareversion", "1");
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
        // assert no errors
    }

    @Test
    public void testCloneDeviceListForEmptyGroup(){
        DeviceService deviceService = spy(inMemoryPersistence.getInjector().getInstance(DeviceService.class));
        DataModel dataModel = spy(inMemoryPersistence.getFirmwareService().getDataModel());
        FirmwareCampaignImpl firmwareCampaign = new FirmwareCampaignImpl(
                dataModel,
                inMemoryPersistence.getInjector().getInstance(Clock.class),
                inMemoryPersistence.getFirmwareService(),
                inMemoryPersistence.getInjector().getInstance(DeviceMessageSpecificationService.class),
                inMemoryPersistence.getInjector().getInstance(EventService.class)
        );
        doNothing().when(dataModel).persist(firmwareCampaign);
        doNothing().when(dataModel).update(firmwareCampaign);
        doNothing().when(dataModel).remove(firmwareCampaign);
        EndDeviceGroup deviceGroup = mock(EndDeviceGroup.class);
        when(deviceGroup.getMembers(any(Instant.class))).thenReturn(Collections.emptyList());
        firmwareCampaign.init(getDeviceTypeMock(), deviceGroup);
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.addProperty("FirmwareDeviceMessage.upgrade.firwareversion", "1");
        doReturn(Optional.of(firmwareCampaign)).when(inMemoryPersistence.getFirmwareService()).getFirmwareCampaignById(1L);
        MeteringGroupsService meteringGroupsService = mock(MeteringGroupsService.class);
        when(meteringGroupsService.findEndDeviceGroup(1L)).thenReturn(Optional.of(deviceGroup));
        FirmwareCampaignHandlerContext context = new FirmwareCampaignHandlerContext(inMemoryPersistence.getFirmwareService(),
                meteringGroupsService,
                deviceService,
                inMemoryPersistence.getInjector().getInstance(Clock.class),
                inMemoryPersistence.getInjector().getInstance(EventService.class),
                inMemoryPersistence.getInjector().getInstance(TaskService.class));
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", 1L);
        properties.put("deviceGroupId", 1L);
        FirmwareCampaignHandler.Handler.FIRMWARE_CAMPAIGN_CREATED.handle(properties, context);
        assertThat(firmwareCampaign.getStatus()).isEqualTo(FirmwareCampaignStatus.CANCELLED);
    }

    public void testCloneDeviceListForGroup(){
        DeviceService deviceService = spy(inMemoryPersistence.getInjector().getInstance(DeviceService.class));
        DataModel dataModel = spy(inMemoryPersistence.getFirmwareService().getDataModel());
        FirmwareCampaignImpl firmwareCampaign = new FirmwareCampaignImpl(
                dataModel,
                inMemoryPersistence.getInjector().getInstance(Clock.class),
                inMemoryPersistence.getFirmwareService(),
                inMemoryPersistence.getInjector().getInstance(DeviceMessageSpecificationService.class),
                inMemoryPersistence.getInjector().getInstance(EventService.class)
        );
        doNothing().when(dataModel).persist(firmwareCampaign);
        doNothing().when(dataModel).update(firmwareCampaign);
        doNothing().when(dataModel).remove(firmwareCampaign);
        EndDeviceGroup deviceGroup = mock(EndDeviceGroup.class);
        Meter meter = mock(Meter.class);
        when(meter.getAmrId()).thenReturn("1");
        when(deviceGroup.getMembers(any(Instant.class))).thenReturn(Collections.singletonList(meter));
        Device device = mock(Device.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        DeviceType deviceType = getDeviceTypeMock();
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        doReturn(Optional.of(device)).when(deviceService).findDeviceById(1L);
        firmwareCampaign.init(getDeviceTypeMock(), deviceGroup);
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.addProperty("FirmwareDeviceMessage.upgrade.firwareversion", "1");
        doReturn(Optional.of(firmwareCampaign)).when(inMemoryPersistence.getFirmwareService()).getFirmwareCampaignById(1L);
        MeteringGroupsService meteringGroupsService = mock(MeteringGroupsService.class);
        when(meteringGroupsService.findEndDeviceGroup(1L)).thenReturn(Optional.of(deviceGroup));
        FirmwareCampaignHandlerContext context = new FirmwareCampaignHandlerContext(inMemoryPersistence.getFirmwareService(),
                meteringGroupsService,
                deviceService,
                inMemoryPersistence.getInjector().getInstance(Clock.class),
                inMemoryPersistence.getInjector().getInstance(EventService.class),
                inMemoryPersistence.getInjector().getInstance(TaskService.class));
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", 1L);
        properties.put("deviceGroupId", 1L);
        FirmwareCampaignHandler.Handler.FIRMWARE_CAMPAIGN_CREATED.handle(properties, context);
        assertThat(firmwareCampaign.getStatus()).isEqualTo(FirmwareCampaignStatus.ONGOING);
        verify(dataModel).getInstance(DeviceInFirmwareCampaignImpl.class);
    }
}
