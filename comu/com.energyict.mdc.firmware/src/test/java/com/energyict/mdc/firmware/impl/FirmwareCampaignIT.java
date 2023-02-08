/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.fsm.impl.StateTransitionTriggerEventTopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallStateChangeTopicHandler;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignProperty;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignCustomPropertySet;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignDomainExtension;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignItemServiceCallHandler;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignServiceCallHandler;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignServiceImpl;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FirmwareCampaignIT extends PersistenceTest {

    private static final String imageIdentifier = "imageIdentifier";
    private static CustomPropertySet<ServiceCall, FirmwareCampaignDomainExtension> serviceCallCPS;
    private static final Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private static ServiceCallService serviceCallService;
    private static ServiceCallHandler serviceCallHandler;
    private static FirmwareCampaignServiceImpl firmwareCampaignService;
    private static FirmwareServiceImpl firmwareService;

    private DeviceType getDeviceTypeMock() {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);

        List<DeviceMessageSpec> deviceMessageIds = new ArrayList<>();
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec0 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec0.getId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE.dbValue());
        deviceMessageIds.add(deviceMessageSpec0);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec1 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec1.getId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES.dbValue());
        deviceMessageIds.add(deviceMessageSpec1);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec2 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec2.getId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE.dbValue());
        deviceMessageIds.add(deviceMessageSpec2);

        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);
        return deviceType;
    }

    @BeforeClass
    public static void setUp() {
        TransactionService transactionService = inMemoryPersistence.getTransactionService();
        serviceCallService = inMemoryPersistence.get(ServiceCallService.class);
        firmwareCampaignService = inMemoryPersistence.getFirmwareService().getFirmwareCampaignService();
        firmwareService = inMemoryPersistence.getFirmwareService();
        serviceCallHandler = new FirmwareCampaignServiceCallHandler(firmwareService);
        transactionService.execute(() -> {
            EventServiceImpl eventService = inMemoryPersistence.get(EventServiceImpl.class);
            eventService.addTopicHandler(inMemoryPersistence.get(StateTransitionTriggerEventTopicHandler.class));
            eventService.addTopicHandler(inMemoryPersistence.get(ServiceCallStateChangeTopicHandler.class));
            PropertySpecService propertySpecService = inMemoryPersistence.get(PropertySpecService.class);
            serviceCallCPS = new FirmwareCampaignCustomPropertySet(thesaurus, propertySpecService, firmwareService);
            serviceCallHandler = new FirmwareCampaignServiceCallHandler(firmwareService);
            serviceCallService.addServiceCallHandler(serviceCallHandler, ImmutableMap.of("name", FirmwareCampaignServiceCallHandler.NAME));
            serviceCallService.addServiceCallHandler(serviceCallHandler, ImmutableMap.of("name", FirmwareCampaignItemServiceCallHandler.NAME));
            return null;
        });
    }

    @Test(expected = NoSuchElementException.class)
    @Transactional
    public void testInitWithNullDeviceType() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        firmwareService.getFirmwareCampaignService().newFirmwareCampaign("c1")
                .withDeviceType(null)
                .withFirmwareType(FirmwareType.METER)
                .withDeviceGroup("gr")
                .withManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)
                .withUploadTimeBoundaries(Instant.now(), Instant.now())
                .withUniqueFirmwareVersion(false)
                .create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "deviceGroup", strict = false)
    public void testInitWithNullGroup() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        firmwareService.getFirmwareCampaignService().newFirmwareCampaign("c1")
                .withDeviceType(mock(DeviceType.class))
                .withFirmwareType(FirmwareType.METER)
                .withDeviceGroup(null)
                .withManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)
                .withUploadTimeBoundaries(Instant.now(), Instant.now())
                .withUniqueFirmwareVersion(false)
                .create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "uploadPeriodStart", strict = false)
    public void testInitWithNullTimeBoundary() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        firmwareService.getFirmwareCampaignService().newFirmwareCampaign("c1")
                .withDeviceType(mock(DeviceType.class))
                .withFirmwareType(FirmwareType.METER)
                .withDeviceGroup("gt")
                .withManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)
                .withUploadTimeBoundaries(null, null)
                .withUniqueFirmwareVersion(false)
                .create();
    }

    private DeviceType createDeviceType() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);

        List<DeviceMessageSpec> deviceMessageIds = new ArrayList<>();
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec0 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec0.getId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE.dbValue());
        deviceMessageIds.add(deviceMessageSpec0);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec1 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec1.getId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES.dbValue());
        deviceMessageIds.add(deviceMessageSpec1);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec2 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec2.getId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE.dbValue());
        deviceMessageIds.add(deviceMessageSpec2);

        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);
        DeviceConfigurationService deviceConfigurationService = inMemoryPersistence.getInjector().getInstance(DeviceConfigurationService.class);
        return deviceConfigurationService.newDeviceType("FirwareCampaignTest", deviceProtocolPluggableClass);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "name", strict = false)
    public void testNameValidation() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        firmwareService.getFirmwareCampaignService().newFirmwareCampaign(null)
                .withDeviceType(mock(DeviceType.class))
                .withFirmwareType(FirmwareType.METER)
                .withDeviceGroup("gt")
                .withManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)
                .withUploadTimeBoundaries(Instant.now(), Instant.now())
                .withUniqueFirmwareVersion(false)
                .create();
    }

    @Test
    @Transactional
    public void testDefaultStatus() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        firmwareService.getFirmwareCampaignService().newFirmwareCampaign("camp1")
                .withDeviceType(this.createDeviceType())
                .withFirmwareType(FirmwareType.METER)
                .withDeviceGroup("gr")
                .withManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)
                .withUploadTimeBoundaries(Instant.now(), Instant.now())
                .withValidationTimeout(new TimeDuration(2, TimeDuration.TimeUnit.MINUTES))
                .withUniqueFirmwareVersion(false)
                .create();
        FirmwareCampaign firmwareCampaign = firmwareService.getFirmwareCampaignService().streamAllCampaigns().findFirst().get();
        assertThat(firmwareCampaign.getServiceCall().getState()).isEqualTo(DefaultState.ONGOING);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "managementOption", strict = false)
    public void testManagementOptionValidation() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        firmwareService.getFirmwareCampaignService().newFirmwareCampaign("camp1")
                .withDeviceType(mock(DeviceType.class))
                .withFirmwareType(FirmwareType.METER)
                .withDeviceGroup("gr")
                .withManagementOption(null)
                .withUploadTimeBoundaries(Instant.now(), Instant.now())
                .withUniqueFirmwareVersion(false)
                .create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "firmwareType", strict = false)
    public void testFirmwareTypeValidation() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        firmwareService.getFirmwareCampaignService().newFirmwareCampaign("camp1")
                .withDeviceType(this.createDeviceType())
                .withFirmwareType(null)
                .withDeviceGroup("gr")
                .withManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)
                .withUploadTimeBoundaries(Instant.now(), Instant.now())
                .withValidationTimeout(new TimeDuration(2, TimeDuration.TimeUnit.MINUTES))
                .withUniqueFirmwareVersion(false)
                .create();
    }

    @Test
    @Transactional
    public void testPropertiesValidation() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.toStringValue(any())).thenReturn("50");
        when(propertySpec.getName()).thenReturn("Spec");
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        FirmwareCampaign firmwareCampaign = firmwareService.getFirmwareCampaignService().newFirmwareCampaign("camp1")
                .withDeviceType(this.createDeviceType())
                .withFirmwareType(FirmwareType.METER)
                .withDeviceGroup("gr")
                .withManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)
                .withUploadTimeBoundaries(Instant.now(), Instant.now())
                .withValidationTimeout(new TimeDuration(2, TimeDuration.TimeUnit.MINUTES))
                .withUniqueFirmwareVersion(false)
                .addProperty(propertySpec, "50")
                .create();
        FirmwareCampaignProperty firmwareCampaignProperty = inMemoryPersistence.get(DataModel.class).mapper(FirmwareCampaignProperty.class).find().get(0);
        assertThat(firmwareCampaignProperty.getKey()).isEqualTo("Spec");
        assertThat(firmwareCampaignProperty.getValue()).isEqualTo("50");
    }

    @Test
    @Transactional
    public void testCancelCampaign() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.getFirmwareCampaignService().newFirmwareCampaign("camp1")
                .withDeviceType(this.createDeviceType())
                .withFirmwareType(FirmwareType.METER)
                .withDeviceGroup("gr")
                .withManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)
                .withUploadTimeBoundaries(Instant.now(), Instant.now())
                .withValidationTimeout(new TimeDuration(2, TimeDuration.TimeUnit.MINUTES))
                .withUniqueFirmwareVersion(false)
                .create();
        firmwareCampaign.cancel();
        assertThat(serviceCallService.getServiceCallFinder().find().get(0).getLogs().find().contains("campaign cancelled by user"));
    }


    @Test
    @Transactional
    public void testDeleteCampaign() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.getFirmwareCampaignService().newFirmwareCampaign("camp1")
                .withDeviceType(this.createDeviceType())
                .withFirmwareType(FirmwareType.METER)
                .withDeviceGroup("gr")
                .withManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)
                .withUploadTimeBoundaries(Instant.now(), Instant.now())
                .withValidationTimeout(new TimeDuration(2, TimeDuration.TimeUnit.MINUTES))
                .withUniqueFirmwareVersion(false)
                .create();
        firmwareCampaign.delete();
        // assert no errors
    }

    @Test
    @Transactional
    public void testRemoveCanceledCampaign() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareCampaign firmwareCampaign = firmwareService.getFirmwareCampaignService().newFirmwareCampaign("camp1")
                .withDeviceType(this.createDeviceType())
                .withFirmwareType(FirmwareType.METER)
                .withDeviceGroup("gr")
                .withManagementOption(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)
                .withUploadTimeBoundaries(Instant.now(), Instant.now())
                .withValidationTimeout(new TimeDuration(2, TimeDuration.TimeUnit.MINUTES))
                .withUniqueFirmwareVersion(false)
                .create();
        firmwareCampaign.cancel();
        firmwareCampaign.delete();
    }
}
