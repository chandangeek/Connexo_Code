/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.LocalEvent;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareService;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceTypeDeletionEventHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-14 (14:44)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceTypeDeletionEventHandlerTest {

    @Mock
    private FirmwareService firmwareService;
    @Mock
    private LocalEvent event;
    @Mock
    private DeviceType deviceType;

    @Before
    public void initializeMocks() {
        when(this.event.getSource()).thenReturn(this.deviceType);
    }

    @Test
    public void topicMatcherDoesNotReturnEmpty() {
        assertThat(this.getInstance().getTopicMatcher()).isNotEmpty();
    }

    @Test
    public void deleteDeviceTypeWithoutOptions() {
        when(this.firmwareService.findFirmwareManagementOptions(this.deviceType)).thenReturn(Optional.empty());
        when(this.firmwareService.findFirmwareCampaigns(this.deviceType)).thenReturn(Collections.emptyList());

        // Business method
        this.getInstance().handle(this.event);

        // Asserts
        verify(this.firmwareService).findFirmwareManagementOptions(this.deviceType);
    }

    @Test
    public void deleteDeviceTypeWithOptions() {
        FirmwareManagementOptions options = mock(FirmwareManagementOptions.class);
        when(this.firmwareService.findFirmwareManagementOptions(this.deviceType)).thenReturn(Optional.of(options));
        when(this.firmwareService.findFirmwareCampaigns(this.deviceType)).thenReturn(Collections.emptyList());

        // Business method
        this.getInstance().handle(this.event);

        // Asserts
        verify(options).delete();
    }

    @Test
    public void deleteDeviceTypeWithoutCampaigns() {
        when(this.firmwareService.findFirmwareManagementOptions(this.deviceType)).thenReturn(Optional.empty());
        when(this.firmwareService.findFirmwareCampaigns(this.deviceType)).thenReturn(Collections.emptyList());

        // Business method
        this.getInstance().handle(this.event);

        // Asserts
        verify(this.firmwareService).findFirmwareCampaigns(this.deviceType);
    }

    @Test
    public void deleteDeviceTypeWithCampaigns() {
        FirmwareCampaign campaign = mock(FirmwareCampaign.class);
        when(this.firmwareService.findFirmwareManagementOptions(this.deviceType)).thenReturn(Optional.empty());
        when(this.firmwareService.findFirmwareCampaigns(this.deviceType)).thenReturn(Collections.singletonList(campaign));

        // Business method
        this.getInstance().handle(this.event);

        // Asserts
        verify(campaign).delete();
    }

    private DeviceTypeDeletionEventHandler getInstance() {
        return new DeviceTypeDeletionEventHandler(this.firmwareService);
    }

}