/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.rest.impl.campaign.FirmwareCampaignInfo;
import com.energyict.mdc.firmware.rest.impl.campaign.FirmwareCampaignInfoFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FirmwareCampaignFactoryTest {

    private static FirmwareService firmwareService = mock(FirmwareService.class);
    private static Clock clock = mock(Clock.class);
    private static DeviceConfigurationService deviceConfigurationService = mock(DeviceConfigurationService.class);
    private static FirmwareCampaignInfoFactory firmwareCampaignInfoFactory;
    private static Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private static DeviceMessageSpecificationService deviceMessageSpecificationService = mock(DeviceMessageSpecificationService.class);
    private static ResourceHelper resourceHelper = mock(ResourceHelper.class);
    private static FirmwareVersionInfoFactory firmwareVersionInfoFactory = mock(FirmwareVersionInfoFactory.class);
    private static FirmwareMessageInfoFactory firmwareMessageInfoFactory = mock(FirmwareMessageInfoFactory.class);

    private static ExceptionFactory exceptionFactory = mock(ExceptionFactory.class);

    @BeforeClass
    public static void setUp() {
        firmwareCampaignInfoFactory = new FirmwareCampaignInfoFactory(thesaurus, deviceConfigurationService,
                deviceMessageSpecificationService, resourceHelper, firmwareVersionInfoFactory, firmwareMessageInfoFactory,
                exceptionFactory, clock, firmwareService);
    }

    @Test
    public void fromTest() {
        FirmwareCampaign firmwareCampaign = createMockCampaign();
        FirmwareCampaignInfo firmwareCampaignInfo = firmwareCampaignInfoFactory.from(firmwareCampaign);
        assertEquals(3L, firmwareCampaignInfo.id);
        assertEquals(4L, firmwareCampaignInfo.version);
        assertEquals(2, firmwareCampaignInfo.validationTimeout.count);
        assertEquals("TestCampaign", firmwareCampaignInfo.name);
        assertEquals("activate", firmwareCampaignInfo.managementOption.localizedValue);
        assertEquals("TestGroup", firmwareCampaignInfo.deviceGroup);
        assertEquals(Instant.ofEpochSecond(100), firmwareCampaignInfo.timeBoundaryStart);
        assertEquals(Instant.ofEpochSecond(200), firmwareCampaignInfo.timeBoundaryEnd);
        assertEquals("TestDeviceType", firmwareCampaignInfo.deviceType.localizedValue);
    }

    @Test
    public void getOverviewTest() {
        FirmwareCampaign firmwareCampaign = createMockCampaign();
        FirmwareCampaignInfo firmwareCampaignInfo = firmwareCampaignInfoFactory.getOverviewCampaignInfo(firmwareCampaign);
        assertEquals(3L,firmwareCampaignInfo.id);
        assertEquals(4L,firmwareCampaignInfo.version);
        assertEquals(2, firmwareCampaignInfo.validationTimeout.count);
        assertEquals("TestCampaign", firmwareCampaignInfo.name);
        assertEquals("activate", firmwareCampaignInfo.managementOption.localizedValue);
        assertEquals("TestGroup", firmwareCampaignInfo.deviceGroup);
        assertEquals(Instant.ofEpochSecond(100), firmwareCampaignInfo.timeBoundaryStart);
        assertEquals(Instant.ofEpochSecond(200), firmwareCampaignInfo.timeBoundaryEnd);
        assertEquals("TestDeviceType", firmwareCampaignInfo.deviceType.localizedValue);
        assertEquals(Instant.ofEpochSecond(111), firmwareCampaignInfo.startedOn);
        assertNull(firmwareCampaignInfo.finishedOn);
        assertEquals("Ongoing", firmwareCampaignInfo.status.name);
    }


    private FirmwareCampaign createMockCampaign() {
        FirmwareCampaign firmwareCampaign = mock(FirmwareCampaign.class);
        ServiceCall serviceCall = mock(ServiceCall.class);
        when(firmwareCampaign.getServiceCall()).thenReturn(serviceCall);
        when(serviceCall.getCreationTime()).thenReturn(Instant.ofEpochSecond(111));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("TestDeviceType");
        FirmwareType firmwareType = FirmwareType.METER;
        DeviceMessageSpec deviceMessageSpec = mock(DeviceMessageSpec.class);
        when(firmwareCampaign.getFirmwareType()).thenReturn(firmwareType);
        when(firmwareCampaign.getName()).thenReturn("TestCampaign");
        when(firmwareCampaign.getDeviceType()).thenReturn(deviceType);
        when(firmwareCampaign.getDeviceGroup()).thenReturn("TestGroup");
        when(firmwareCampaign.getUploadPeriodStart()).thenReturn(Instant.ofEpochSecond(100));
        when(firmwareCampaign.getUploadPeriodEnd()).thenReturn(Instant.ofEpochSecond(200));
        when(firmwareCampaign.getFirmwareManagementOption()).thenReturn(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        when(firmwareCampaign.getActivationDate()).thenReturn(Instant.ofEpochSecond(100));
        when(firmwareCampaign.getValidationTimeout()).thenReturn(new TimeDuration(2, TimeDuration.TimeUnit.MINUTES));
        when(firmwareCampaign.getId()).thenReturn(3L);
        when(firmwareCampaign.getVersion()).thenReturn(4L);
        when(firmwareCampaign.getFirmwareMessageSpec()).thenReturn(Optional.ofNullable(deviceMessageSpec));
        when(firmwareCampaign.getServiceCall()).thenReturn(serviceCall);
        when(firmwareCampaign.getStartedOn()).thenReturn(Instant.ofEpochSecond(111));
        return firmwareCampaign;
    }
}