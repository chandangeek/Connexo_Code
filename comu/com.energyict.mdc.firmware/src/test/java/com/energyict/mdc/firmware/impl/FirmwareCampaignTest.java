/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignStatus;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.TaskService;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FirmwareCampaignTest extends PersistenceTest {

    private DeviceType getDeviceTypeMock() {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        Set<DeviceMessageId> supportedMessages = new HashSet<>(Arrays.asList(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE));
        when(deviceProtocol.getSupportedMessages()).thenReturn(supportedMessages);
        return deviceType;
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "deviceType", strict = false)
    public void testInitWithNullDeviceType() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        firmwareService.newFirmwareCampaign(null, mock(EndDeviceGroup.class)).save();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "deviceGroup", strict = false)
    public void testInitWithNullGroup() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), null);
        firmwareCampaign.setComWindow(mock(ComWindow.class));
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "comWindow", strict = false)
    public void testInitWithNullComWindow() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), mock(EndDeviceGroup.class));
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
    }

    private DeviceType createDeviceType() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);

        Set<DeviceMessageId> supportedMessages = new HashSet<>();
        Collections.addAll(supportedMessages,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE);
        when(deviceProtocol.getSupportedMessages()).thenReturn(supportedMessages);
        DeviceConfigurationService deviceConfigurationService = inMemoryPersistence.getInjector().getInstance(DeviceConfigurationService.class);
        DeviceType deviceType = deviceConfigurationService.newDeviceType("FirwareCampaignTest", deviceProtocolPluggableClass);
        return deviceType;
    }

    @Test
    @Transactional
    public void testUpdateWithNullGroup() {
        DeviceType deviceType = this.createDeviceType();
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareVersion firmwareVersion = firmwareService.newFirmwareVersion(deviceType, "1", FirmwareStatus.GHOST, FirmwareType.METER).create();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(deviceType, null);
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.setComWindow(mock(ComWindow.class));
        firmwareCampaign.addProperty(DeviceMessageConstants.firmwareUpdateFileAttributeName, String.valueOf(firmwareVersion.getId()));
        Save.UPDATE.validate(firmwareService.getDataModel(), firmwareCampaign);
        //assert no errors
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "name", strict = false)
    public void testNameValidation() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), mock(EndDeviceGroup.class));
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
    }

    @Test
    public void testDefaultStatus() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), mock(EndDeviceGroup.class));
        assertThat(firmwareCampaign.getStatus()).isEqualTo(FirmwareCampaignStatus.ONGOING);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "managementOption", strict = false)
    public void testManagementOptionValidation() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "firmwareType", strict = false)
    public void testFirmwareTypeValidation() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "properties." + DeviceMessageConstants.firmwareUpdateFileAttributeName , strict = true)
    public void testPropertiesValidation() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.setComWindow(mock(ComWindow.class));
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
    }

    @Test
    @Transactional
    public void testSuccessfulCreation() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        DeviceType deviceType = this.createDeviceType();
        FirmwareVersion firmwareVersion = firmwareService.newFirmwareVersion(deviceType, "1", FirmwareStatus.GHOST, FirmwareType.METER).create();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(deviceType, mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.setComWindow(mock(ComWindow.class));
        firmwareCampaign.addProperty(DeviceMessageConstants.firmwareUpdateFileAttributeName, String.valueOf(firmwareVersion.getId()));
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
        // assert no errors
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "properties." + DeviceMessageConstants.firmwareUpdateFileAttributeName , strict = true)
    public void testCreateWithNonExistingVersion() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(getDeviceTypeMock(), mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.addProperty(DeviceMessageConstants.firmwareUpdateFileAttributeName, String.valueOf(Integer.MAX_VALUE));
        firmwareCampaign.setComWindow(mock(ComWindow.class));
        Save.CREATE.validate(firmwareService.getDataModel(), firmwareCampaign);
        // assert no errors
    }

    @Test
    @Transactional
    public void testOngoingStatus() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        DeviceType deviceType = this.createDeviceType();
        FirmwareVersion firmwareVersion = firmwareService.newFirmwareVersion(deviceType, "1", FirmwareStatus.GHOST, FirmwareType.METER).create();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(deviceType, mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.setComWindow(mock(ComWindow.class));
        firmwareCampaign.addProperty(DeviceMessageConstants.firmwareUpdateFileAttributeName, String.valueOf(firmwareVersion.getId()));

        firmwareCampaign.save();

        FirmwareCampaign campaign = inMemoryPersistence.getFirmwareService().getFirmwareCampaignById(firmwareCampaign.getId()).get();
        assertThat(campaign.getStatus()).isEqualTo(FirmwareCampaignStatus.ONGOING);
    }

    @Test
    @Transactional
    public void testUpdateStatistics() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        DeviceType deviceType = this.createDeviceType();
        FirmwareVersion firmwareVersion = firmwareService.newFirmwareVersion(deviceType, "1", FirmwareStatus.GHOST, FirmwareType.METER).create();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(deviceType, mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.setComWindow(mock(ComWindow.class));
        firmwareCampaign.addProperty(DeviceMessageConstants.firmwareUpdateFileAttributeName, String.valueOf(firmwareVersion.getId()));

        firmwareCampaign.save();

        ((FirmwareCampaignImpl) firmwareCampaign).updateStatistic();

        FirmwareCampaignImpl campaignAfterUpdate = (FirmwareCampaignImpl) inMemoryPersistence.getFirmwareService().getFirmwareCampaignById(firmwareCampaign.getId()).get();
        assertThat(campaignAfterUpdate.getStatus()).isEqualTo(FirmwareCampaignStatus.COMPLETE);
    }

    @Test
    @Transactional
    public void testCancelStatistics() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        DeviceType deviceType = this.createDeviceType();
        FirmwareVersion firmwareVersion = firmwareService.newFirmwareVersion(deviceType, "1", FirmwareStatus.GHOST, FirmwareType.METER).create();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(deviceType, mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.setComWindow(mock(ComWindow.class));
        firmwareCampaign.addProperty(DeviceMessageConstants.firmwareUpdateFileAttributeName, String.valueOf(firmwareVersion.getId()));

        firmwareCampaign.save();

        ((FirmwareCampaignImpl) firmwareCampaign).cancel();

        FirmwareCampaignImpl campaignAfterUpdate = (FirmwareCampaignImpl) inMemoryPersistence.getFirmwareService().getFirmwareCampaignById(firmwareCampaign.getId()).get();
        assertThat(campaignAfterUpdate.getStatus()).isEqualTo(FirmwareCampaignStatus.CANCELLED);
    }


    @Test
    @Transactional
    public void testSimpleRemoval() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        DeviceType deviceType = this.createDeviceType();
        FirmwareVersion firmwareVersion = firmwareService.newFirmwareVersion(deviceType, "1", FirmwareStatus.GHOST, FirmwareType.METER).create();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(deviceType, mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.setComWindow(mock(ComWindow.class));
        firmwareCampaign.addProperty(DeviceMessageConstants.firmwareUpdateFileAttributeName, String.valueOf(firmwareVersion.getId()));
        firmwareCampaign.save();

        FirmwareCampaign campaign = inMemoryPersistence.getFirmwareService().getFirmwareCampaignById(firmwareCampaign.getId()).get();
        campaign.delete();
        // assert no errors
    }

    @Test
    @Transactional
    public void testRemovalOngoingCampaign() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        DeviceType deviceType = this.createDeviceType();
        FirmwareVersion firmwareVersion = firmwareService.newFirmwareVersion(deviceType, "1", FirmwareStatus.GHOST, FirmwareType.METER).create();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(deviceType, mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.setComWindow(mock(ComWindow.class));
        firmwareCampaign.addProperty(DeviceMessageConstants.firmwareUpdateFileAttributeName, String.valueOf(firmwareVersion.getId()));

        firmwareCampaign.save();

        ((FirmwareCampaignImpl) firmwareCampaign).updateStatistic();

        FirmwareCampaignImpl campaignAfterUpdate = (FirmwareCampaignImpl) inMemoryPersistence.getFirmwareService().getFirmwareCampaignById(firmwareCampaign.getId()).get();
        campaignAfterUpdate.delete();
    }

    @Test
    @Transactional
    public void testRemoveCanceledCampaign() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        DeviceType deviceType = this.createDeviceType();
        FirmwareVersion firmwareVersion = firmwareService.newFirmwareVersion(deviceType, "1", FirmwareStatus.GHOST, FirmwareType.METER).create();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(deviceType, mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.setComWindow(mock(ComWindow.class));
        firmwareCampaign.addProperty(DeviceMessageConstants.firmwareUpdateFileAttributeName, String.valueOf(firmwareVersion.getId()));

        firmwareCampaign.save();

        ((FirmwareCampaignImpl) firmwareCampaign).cancel();

        FirmwareCampaignImpl campaignAfterUpdate = (FirmwareCampaignImpl) inMemoryPersistence.getFirmwareService().getFirmwareCampaignById(firmwareCampaign.getId()).get();
        campaignAfterUpdate.delete();
    }
/*
    @Test
    @Transactional
    public void testRemovalOngoingCampaignWithDevices() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        DeviceType deviceType = this.createDeviceType();
        FirmwareVersion firmwareVersion = firmwareService.newFirmwareVersion(deviceType, "1", FirmwareStatus.GHOST, FirmwareType.METER).create();
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(deviceType, mock(EndDeviceGroup.class));
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.setComWindow(mock(ComWindow.class));
        firmwareCampaign.addProperty(DeviceMessageConstants.firmwareUpdateFileAttributeName, String.valueOf(firmwareVersion.getId()));

        firmwareCampaign.save();

        ((FirmwareCampaignImpl) firmwareCampaign).updateStatistic();

        DeviceImpl device = mock(DeviceImpl.class);
        when(device.getId()).thenReturn(123L);

        DeviceInFirmwareCampaignImpl deviceInFirmwareCampaign = inMemoryPersistence.getFirmwareService().getDataModel().getInstance(DeviceInFirmwareCampaignImpl.class);
        deviceInFirmwareCampaign.init(firmwareCampaign, device );
        deviceInFirmwareCampaign.setStatus(FirmwareManagementDeviceStatus.ACTIVATION_PENDING);
        inMemoryPersistence.getFirmwareService().getDataModel().persist(deviceInFirmwareCampaign);
        deviceInFirmwareCampaign.startFirmwareProcess();

        FirmwareCampaignImpl campaignAfterUpdate = (FirmwareCampaignImpl) inMemoryPersistence.getFirmwareService().getFirmwareCampaignById(firmwareCampaign.getId()).get();
        campaignAfterUpdate.delete();
    }

 */

    @Test
    @Transactional
    public void testCloneDeviceListForEmptyGroup() {
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
        DeviceType deviceType = this.createDeviceType();
        FirmwareVersion firmwareVersion = inMemoryPersistence.getFirmwareService().newFirmwareVersion(deviceType, "1", FirmwareStatus.GHOST, FirmwareType.METER).create();
        firmwareCampaign.init(deviceType, deviceGroup);
        firmwareCampaign.setName("firmware campaign 1");
        firmwareCampaign.setManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        firmwareCampaign.setFirmwareType(FirmwareType.METER);
        firmwareCampaign.setComWindow(mock(ComWindow.class));
        firmwareCampaign.addProperty(DeviceMessageConstants.firmwareUpdateFileAttributeName, String.valueOf(firmwareVersion.getId()));
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

    public void testCloneDeviceListForGroup() {
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
        firmwareCampaign.addProperty(DeviceMessageConstants.firmwareUpdateFileAttributeName, "1");
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
