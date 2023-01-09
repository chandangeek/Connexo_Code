/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.rest.impl.campaign.DeviceInFirmwareCampaignInfo;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInFirmwareCampaignInfoTest {

    @Mock
    Thesaurus thesaurus;
    @Mock
    private DeviceInFirmwareCampaign deviceInFirmwareCampaign;
    @Mock
    private ServiceCall firmwareCampaign;
    @Mock
    private ServiceCall item;
    @Mock
    private Device device;

    private Instant startedOn = Instant.ofEpochMilli(459859514);
    private Instant finishedOn = Instant.ofEpochMilli(1462451511);

    @Before
    public void initMock() {
        when(thesaurus.getString(anyString(), anyString())).thenReturn("someTranslatedFirmwareManagementDeviceStatus");

        when(device.getName()).thenReturn("NameOfDevice");
        when(device.getId()).thenReturn(6598L);
        when(deviceInFirmwareCampaign.getDevice()).thenReturn(device);
        when(deviceInFirmwareCampaign.getParent()).thenReturn(firmwareCampaign);
        when(item.getCreationTime()).thenReturn(startedOn);
        when(item.getLastModificationTime()).thenReturn(finishedOn);
        when(deviceInFirmwareCampaign.getServiceCall()).thenReturn(item);

    }

    @Test
    public void testDeviceInFirmwareCampaignInfoConstructor() {
        DeviceInFirmwareCampaignInfo info = new DeviceInFirmwareCampaignInfo(1L, new IdWithNameInfo(device.getId(), device.getName()), new IdWithNameInfo(DefaultState.PENDING, "Pending"), startedOn, finishedOn);

        assertThat(info.id).isEqualTo(1L);
        assertThat(info.device.name).isEqualTo("NameOfDevice");
        assertThat(info.device.id).isEqualTo(device.getId());
        assertThat(info.status.name).isEqualTo("Pending");
        assertThat(info.startedOn).isEqualTo(startedOn);
        assertThat(info.finishedOn).isEqualTo(finishedOn);
    }

}
